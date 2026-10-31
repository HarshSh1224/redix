import config.AppConfig;
import tcp.ClientHandler;
import tcp.TCPClient;
import tcp.TCPConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Redix!");
//
        try (TCPConnection tcpConnection = new TCPConnection();
             ExecutorService executor =
                 Executors.newFixedThreadPool(
                     AppConfig.getMaxConnections()
                 )
        ) {
            while (true) {
                TCPClient client = tcpConnection.getNextClient();
                executor.submit(new ClientHandler(client));
            }
        }
    }
}
