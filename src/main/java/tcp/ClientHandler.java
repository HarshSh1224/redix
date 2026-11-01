package tcp;

import resp.RespDecoder;
import java.util.List;

public class ClientHandler implements Runnable{
    private final TCPClient client;
    private final RespDecoder respDecoder;

    public ClientHandler(TCPClient client) {
        this.client = client;
        this.respDecoder = new RespDecoder();
    }

    @Override
    public void run() {
        try (TCPClient ignored = client) {
            System.out.println(
                Thread.currentThread().getName()
            );

            while (true) {
                List<String> tokens = respDecoder.readTokens(client);
            }
        } catch (RuntimeException e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }
}
