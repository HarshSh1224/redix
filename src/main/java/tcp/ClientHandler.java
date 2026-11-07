package tcp;

import resp.RespDecoder;
import resp.RespValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final TCPClient client;
    private final RespDecoder respDecoder;

    public ClientHandler(TCPClient client) {
        this.client = client;
        this.respDecoder = new RespDecoder();
    }

    @Override
    public void run() {
        try (TCPClient ignored = client) {
            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();

            byte[] buffer = new byte[1024];
            StringBuilder inputBuffer = new StringBuilder();

            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                inputBuffer.append(
                    new String(buffer, 0, bytesRead, StandardCharsets.UTF_8)
                );

                int readIndex = 0;

                while (readIndex < inputBuffer.length()) {
                    try {
                        RespDecoder.DecodeResult result =
                            respDecoder.decodeOne(inputBuffer.toString(), readIndex);

                        RespValue value = result.value();
                        readIndex = result.index();

                        // Execute command here
                        System.out.println(value);

                        outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (IOException e) {
                        break;
                    }
                }

                // Discard successfully decoded data.
                if (readIndex > 0) {
                    inputBuffer.delete(0, readIndex);
                }
            }

        } catch (IOException | RuntimeException e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }
}