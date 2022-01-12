package client;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Client {
    public static void main(String[] args) {
        System.out.println("Choose host");
        Scanner scanner = new Scanner(System.in);
        String host = scanner.nextLine();
        System.out.println("Choose port");
        int port = scanner.nextInt();
        System.out.println("Choose buffer size");
        int bufferSize = scanner.nextInt();
        System.out.println("Do you want to use Nagel's algorithm? y/n");
        String nagelChoice = scanner.nextLine();
        boolean nagel;
        if (nagelChoice.equals("n")) {
            nagel = false;
        } else {
            nagel = true;
        }
        try {
            TCPClient tcpClient = new TCPClient(host, port, bufferSize, nagel);
            UDPClient udpClient = new UDPClient(host, port, bufferSize);
            ExecutorService executor = Executors.newFixedThreadPool(2);
            FutureTask<String> tcpTask = new FutureTask<>(tcpClient, "TCP Finished");
            FutureTask<String> udpTask = new FutureTask<>(udpClient, "UDP Finished");
            executor.submit(tcpTask);
            executor.submit(udpTask);
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("exit")) {
                    tcpClient.setRunning(false);
                    udpClient.setRunning(false);
                    break;
                }
                if (input.equals("sus tcp")) {
                    tcpClient.stop();
                }
                if (input.equals("run tcp")) {
                    tcpClient.start();
                }
                if (input.equals("sus udp")) {
                    udpClient.stop();
                }
                if (input.equals("run udp")) {
                    udpClient.start();
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Couldn't connect to " + host);
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("Couldn't create a UDP socket");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Couldn't connect to the port " + port + " on " + host);
            e.printStackTrace();
        }
    }
}
