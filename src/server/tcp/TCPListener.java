package server.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TCPListener implements Runnable {

    private final ServerSocket serverSocket;
    private final TCPReceiver receiver;
    private boolean running;
    private boolean stopped;
    private final Object lock;
    private final static class Lock {}

    public TCPListener(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        receiver = new TCPReceiver();
        running = true;
        stopped = false;
        lock = new Lock();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void stop() {
        receiver.stop();
        stopped = true;
    }

    public void start() {
        synchronized (lock) {
            receiver.start();
            stopped = false;
            lock.notify();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                synchronized (lock) {
                    while (stopped) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Socket socket = serverSocket.accept();
                if (receiver.isBusy()) {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write("BUSY".getBytes(StandardCharsets.UTF_8));
                } else {
                    receiver.setSocket(socket);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    FutureTask<String> receiverTask = new FutureTask<>(receiver, "Thread TCP: done");
                    executor.submit(receiverTask);
                }
            } catch (IOException e) {
                System.out.println("Couldn't connect to a TCP client");
            }
        }
    }
}
