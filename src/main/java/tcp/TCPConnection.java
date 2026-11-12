package tcp;

import config.AppConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnection implements AutoCloseable {
    private final ServerSocket serverSocket;
    private int id = 0;

    public TCPConnection() {
        try {
            serverSocket = new ServerSocket(AppConfig.getServerPort());
        } catch (IOException e) {
            throw new RuntimeException("Error creating server connection: " + e.getMessage());
        }
    }

    public TCPClient getNextClient() {
        try {
            System.out.println("Waiting for a new connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connection Established!");
            return new TCPClient(clientSocket, id++);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}
