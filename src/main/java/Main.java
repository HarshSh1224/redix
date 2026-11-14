import config.AppConfig;
import store.InMemoryKeyValueStore;
import store.KeyValueStore;
import tcp.ClientHandler;
import tcp.TCPClient;
import tcp.TCPConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Redix!");
        KeyValueStore store = new InMemoryKeyValueStore();

        try (
            ExecutorService executor =
                Executors.newFixedThreadPool(
                    AppConfig.getMaxConnections()
                );
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            TCPConnection tcpConnection = new TCPConnection()
        ) {
            // Cache cleanup cron job
            scheduler.scheduleWithFixedDelay(
                store::runActiveExpiration,
                0,
                AppConfig.getActiveExpireIntervalMs(),
                TimeUnit.MILLISECONDS
            );

            // Listen and handle clients
            while (true) {
                TCPClient client = tcpConnection.getNextClient();
                executor.submit(new ClientHandler(client, store));
            }
        }
    }
}
