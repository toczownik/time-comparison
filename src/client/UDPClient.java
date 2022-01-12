package client;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class UDPClient implements Runnable {

    private final InetAddress address;
    private boolean running;
    private final DatagramSocket socket;
    private byte[] buffer;
    private final int port;
    private boolean stopped;
    private final Object lock;
    private final static class Lock {}

    public UDPClient(String host, int port, int bufferSize) throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        address = InetAddress.getByName(host);
        buffer = new byte[bufferSize];
        this.port = port;
        stopped = false;
        lock = new Lock();
    }

    public void start() {
        synchronized (lock) {
            stopped = false;
            lock.notify();
        }
    }

    public void stop() {
        stopped = true;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        running = true;
        byte[] metaData = ("SIZE:" + buffer.length).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(metaData, metaData.length, address, port);
        try {
            socket.send(packet);
            Random random = new Random();
            while (running) {
                synchronized (lock) {
                    while(stopped) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                random.nextBytes(buffer);
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            metaData = "FINE".getBytes(StandardCharsets.UTF_8);
            packet = new DatagramPacket(metaData, metaData.length, address, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            System.out.println("Couldn't send UDP packet");
        }
    }
}
