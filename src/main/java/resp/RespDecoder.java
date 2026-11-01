package resp;

import command.RedixCmd;
import config.AppConfig;
import tcp.TCPClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespDecoder {

    public List<String> readTokens(TCPClient client) throws RuntimeException {
        InputStream inputStream = null;
        try {
            inputStream = client.getInputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String input = new String(buffer, 0, bytesRead).trim();
                System.out.println(input);

                if(input.isEmpty()) {
                    continue;
                }

                OutputStream outputStream = client.getOutputStream();
                outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("IO Exception while reading inputStream: " + e.getMessage());
        }
    }

    private RespValue decode(String data) throws IOException{
        return switch (data.charAt(0)) {
            case '+' -> decodeString(data);
            case ':' -> decodeInteger(data);
            case '$' -> decodeBulkString(data);
            case '*' -> decodeArray(data);
            case '-' -> decodeString(data);
            default -> throw new IOException("unknown RESP prefix: " + data.charAt(0));
        };
    }

    private RespValue.RespString decodeString(String data) throws IOException {
        int length = data.length();

        if(length > AppConfig.getMaxLineLength()) {
            throw new IOException("Invalid string length: " + length);
        }

        StringBuilder result = new StringBuilder();

        for(int i = 1; i < length; i++){
            if(data.charAt(i) == '\r') {
                if(i == length - 1 || data.charAt(i+1) != '\n') throw new IOException("Read failed! Invalid terminating CRLF found");
                break;
            }
            result.append(data.charAt(i));
        }
        return new RespValue.RespString(result.toString());
    }

    private RespValue.RespInteger decodeInteger(String data) throws IOException {
        int length = data.length();

        long result = 0;

        for(int i = 1; i < length; i++){
            if(data.charAt(i) == '\r') {
                if(i == length - 1 || data.charAt(i+1) != '\n') throw new IOException("Read failed! Invalid terminating CRLF found");
                break;
            }
            result += result * 10 + (data.charAt(i) - '0');
        }
        return new RespValue.RespInteger(result);
    }

    private RespValue decodeBulkString(String data) throws IOException {
        int length = data.length();
        int strLen = 0;

        int i = 0;

        while(data.charAt(++i) >= '0' && data.charAt(i) <= '9'){
            strLen += strLen * 10 + (data.charAt(i) - '0');
        }

        if(data.charAt(i) != '\r' || i >= length-1 || data.charAt(++i) != '\n') throw new IOException("Read failed! Invalid terminating CRLF found");
        else i++;

        if(strLen > AppConfig.getMaxBulkLength()) {
            throw new IOException("Invalid bulk string length: " + length);
        }

        StringBuilder result = new StringBuilder();

        while (strLen-- > 0 ) {
            result.append(data.charAt(i++));
        }
        if(data.charAt(i) != '\r' || i >= length-1 || data.charAt(++i) != '\n') throw new IOException("Read failed! Invalid terminating CRLF found");
        else return new RespValue.RespString(result.toString());
    }

    private RespValue decodeArray(String data) {
        // TODO: Implement
        return new RespValue.RespArray(new ArrayList<>());
    }

}
