import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Hello Harsh");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            System.out.println("Waiting for connection...");
            clientSocket = serverSocket.accept();
            System.out.println("Connected to client!");

            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[1024];

            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                clientSocket.getOutputStream().write("Hello from server".getBytes());
            }
        } catch (IOException e) {
            System.out.println("IO Exception occurred in the server: " + e.getMessage());
        } finally {
            if(clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("IO Exception while closing the connection: " + e.getMessage());
                }
            }
        }
    }
}
