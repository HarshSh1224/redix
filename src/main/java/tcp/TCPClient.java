package tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient implements AutoCloseable{
    private final Socket clientSocket;
    private final int id;

    public TCPClient(Socket socket, int id) {
        this.clientSocket = socket;
        this.id = id;
    }

    public InputStream getInputStream() throws IOException {
        return clientSocket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    public int getId() {
        return id;
    }

    @Override
    public void close() {
        if(clientSocket != null) {
            try {
                clientSocket.close();
                System.out.println("Connection " + id + " closed.");
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
