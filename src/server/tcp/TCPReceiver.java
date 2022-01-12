package server.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TCPReceiver implements Runnable {

    private boolean busy;
    private Socket socket;
    private boolean running;
    private boolean stopped;
    private final Object lock;
    private final static class Lock {}

    public TCPReceiver() {
        busy = false;
        running = true;
        lock = new Lock();
    }

    public boolean isBusy() {
        return busy;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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
        busy = true;
        running = true;
        if (socket != null) {
            try {

                InputStream input = socket.getInputStream();
                DataInputStream inputStream = new DataInputStream(input);
                int msgSize = 0;
                byte[] infoMsg = new byte[10];
                int infoMsgSize = inputStream.read(infoMsg);
                for (int i = 5; i < infoMsgSize; i++) {
                    msgSize *= 10;
                    msgSize += infoMsg[i] - '0';
                }
                byte[] msg = new byte[msgSize];
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
                    int realSize = inputStream.read(msg, 0, msgSize);
                    if (new String(Arrays.copyOfRange(msg, 0, 4)).equals("FINE") || socket.isClosed()) {
                        running = false;
                        socket.close();
                    }
                    long stop = System.nanoTime();
                    long time = (stop - start) / 1000000;
                    totalSize += realSize;
                    System.out.println("Thread TCP: received " + totalSize + "b in time " + time +
                            "ms; transmission speed: " + (totalSize / time *1000) + "b/s");
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error listening to TCP message");
            }
            busy = false;
        } else {
            busy = false;
            System.out.println("TCP socket is not defined");
        }
    }
}
