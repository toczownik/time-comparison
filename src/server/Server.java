package server;

import server.tcp.TCPListener;
import server.udp.UDPListener;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class Server {

    public static void main(String[] args) {
        System.out.println("Choose port");
        Scanner scanner = new Scanner(System.in);
        int port = scanner.nextInt();
        try {

            TCPListener tcpListener = new TCPListener(port);
            UDPListener udpListener = new UDPListener(port);
            FutureTask<String> tcpTask = new FutureTask<>(tcpListener, "Finished TCP");
            FutureTask<String> udpTask = new FutureTask<>(udpListener, "Finished UDP");
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(tcpTask);
            executor.submit(udpTask);
            boolean running = true;
            while (running) {
                String input = scanner.nextLine();
                if (input.equals("exit")) {
                    running = false;
                    udpListener.setRunning(false);
                    tcpListener.setRunning(false);
                }
                if (input.equals("sus tcp")) {
                    tcpListener.stop();
                }
                if (input.equals("run tcp")) {
                    tcpListener.start();
                }
                if (input.equals("sus udp")) {
                    udpListener.stop();
                }
                if (input.equals("run udp")) {
                    udpListener.start();
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't create a server on port " + port);
        }
    }
}
