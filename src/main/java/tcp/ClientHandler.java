package tcp;

import command.CommandHandler;
import command.RedixCmd;
import resp.RespDecoder;
import resp.RespEncoder;
import resp.RespValue;
import store.KeyValueStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private final TCPClient client;
    private final RespDecoder respDecoder;
    private final RespEncoder respEncoder;
    private final CommandHandler commandHandler;

    public ClientHandler(TCPClient client, KeyValueStore store) {
        this.client = client;
        this.respEncoder = new RespEncoder();
        this.respDecoder = new RespDecoder();
        this.commandHandler = new CommandHandler(store);
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
                        RedixCmd command = RedixCmd.from(value);
                        RespValue response = commandHandler.handleCommand(command);

                        outputStream.write(respEncoder.encodeResp(response).getBytes(StandardCharsets.UTF_8));
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