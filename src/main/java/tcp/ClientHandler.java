package tcp;

import java.io.IOException;
import java.io.InputStream;

public class ClientHandler implements Runnable{
    private final TCPClient client;

    public ClientHandler(TCPClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = client.getInputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String input = new String(buffer, 0, bytesRead).trim();
                System.out.println(input);
                client.getOutputStream().write("+PONG\r\n".getBytes());
                client.getOutputStream().flush();
            }
        } catch (IOException e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }
}
