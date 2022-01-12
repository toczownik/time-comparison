package server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UDPListener implements Runnable {
    private DatagramSocket socket;
    private boolean running;
    private final Object lock;
    private final static class Lock {}
    private boolean stopped;

    public UDPListener(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("Couldn't create a UDP socket");
        }
        stopped = false;
        lock = new Lock();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void stop() {
        stopped = true;
    }

    public void start() {
        synchronized (lock) {
            stopped = false;
            lock.notify();
        }
    }

    @Override
    public void run() {
        running = true;

        try {
            byte[] message = new byte[100];
            DatagramPacket packet = new DatagramPacket(message, message.length);
            socket.receive(packet);
            int msgSize = 0;
            byte[] metaMessage = packet.getData();
            int length = metaMessage.length;
            for (int i = 5; i < length; i++) {
                if (metaMessage[i] == 0) break;
                msgSize *= 10;
                msgSize += metaMessage[i] - '0';
            }
            message = new byte[msgSize];
            long start = System.nanoTime();
            float totalSize = 0;
            while (running) {
                synchronized (lock) {
                    while (stopped) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                packet = new DatagramPacket(message, message.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                long stop = System.nanoTime();
                long time = (stop - start) / 1000000;
                int dataSize = 0;
                for (int i = 5; i < msgSize; i++) {
                    if (data[i] == 0) break;
                    dataSize++;
                }
                totalSize += packet.getLength();
                System.out.println("Thread UDP: received " + totalSize + "b in time " + time
                        + "ms; transmission speed: " + (totalSize / time * 1000) + "b/s");
                if (Arrays.toString(packet.getData()).equals("FINE")) {
                    running = false;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't receive UDP package");
        }
    }
}
