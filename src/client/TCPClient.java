package client;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TCPClient implements Runnable {

    private final OutputStream output;
    private int bufferSize;
    private boolean running;
    private final Socket socket;
    private boolean stopped;
    private final Object lock;
    private final static class Lock {}

    public TCPClient(String host, int port, int bufferSize, boolean nagel) throws IOException {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(nagel);
        output = socket.getOutputStream();
        this.bufferSize = bufferSize;
        running = true;
        stopped = false;
        lock = new Lock();
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
        try {
            byte[] buffer = new byte[bufferSize];
            output.write(("SIZE:" + bufferSize).getBytes(StandardCharsets.UTF_8));
            Random random = new Random();

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
                random.nextBytes(buffer);
                output.write(buffer);
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer = new byte[bufferSize - 4];
            random.nextBytes(buffer);
            output.write(("FINE" + buffer).getBytes(StandardCharsets.UTF_8));
            output.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Output Stream error");
            e.printStackTrace();
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
