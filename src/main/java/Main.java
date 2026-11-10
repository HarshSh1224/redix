import config.AppConfig;
import store.InMemoryKeyValueStore;
import store.KeyValueStore;
import tcp.ClientHandler;
import tcp.TCPClient;
import tcp.TCPConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Redix!");
        KeyValueStore store = new InMemoryKeyValueStore();

        try (
            ExecutorService executor =
                Executors.newFixedThreadPool(
                    AppConfig.getMaxConnections()
                );
            TCPConnection tcpConnection = new TCPConnection()
        ) {
            while (true) {
                TCPClient client = tcpConnection.getNextClient();
                executor.submit(new ClientHandler(client, store));
            }
        }
    }
}
