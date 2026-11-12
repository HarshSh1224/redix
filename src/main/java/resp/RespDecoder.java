package resp;

import config.AppConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RespDecoder {

    public record DecodeResult(RespValue value, int index) {
    }

    public DecodeResult decodeOne(String data, int startIndex) throws IOException {
        if (startIndex >= data.length()) {
            throw new IOException("Error parsing input: startIndex out of bounds: " + startIndex + " out of: " + data.length());
        }
        return switch (data.charAt(startIndex)) {
            case '+' -> decodeString(data, startIndex);
            case ':' -> decodeInteger(data, startIndex);
            case '$' -> decodeBulkString(data, startIndex);
            case '*' -> decodeArray(data, startIndex);
            case '-' -> decodeError(data, startIndex);
            default -> throw new IOException("unknown RESP prefix: " + data.charAt(startIndex));
        };
    }

    private DecodeResult decodeString(String data, int startIndex) throws IOException {

        StringBuilder result = new StringBuilder();

        for (int i = startIndex + 1; i < data.length(); i++) {
            if (i - startIndex == AppConfig.getMaxLineLength())
                throw new IOException("Input exceeds line length limit");

            if (data.charAt(i) == '\r') {
                if (i == data.length() - 1 || data.charAt(i + 1) != '\n')
                    throw new IOException("Read failed! Invalid terminating CRLF found");
                return new DecodeResult(new RespValue.RespString(result.toString()), i + 1);
            } else {
                result.append(data.charAt(i));
            }
        }
        throw new IOException("Read failed! Terminating CRLF not found!");
    }

    private DecodeResult decodeInteger(String data, int startIndex) throws IOException {
        try {
            int length = data.length();

            long result = 0;
            boolean isNegative = data.charAt(startIndex + 1) == '-';

            for (int i = startIndex + (isNegative ? 2 : 1); i < length; i++) {
                // TODO: ADD LIMIT CHECK
                if (data.charAt(i) == '\r') {
                    if (i == length - 1 || data.charAt(i + 1) != '\n')
                        throw new IOException("Read failed! Invalid terminating CRLF found");
                    return new DecodeResult(new RespValue.RespInteger(isNegative ? -result : result), i + 1);
                }
                result = result * 10 + (data.charAt(i) - '0');
            }
            throw new IOException("Read failed! Terminating CRLF not found!");
        } catch (RuntimeException e) {
            throw new IOException("Failed to parse integer value: " + e.getMessage());
        }
    }

    private DecodeResult decodeBulkString(String data, int startIndex) throws IOException {
        int length = data.length();
        var sizeDecode = decodeInteger(data, startIndex);

        long strLen = ((RespValue.RespInteger) sizeDecode.value).value();
        startIndex = sizeDecode.index + 1;

        if (strLen < 0) {
            if (!data.startsWith("\r\n", startIndex))
                throw new IOException("ERR No terminating CRLF found!");
            return new DecodeResult(new RespValue.RespNullBulkString(), startIndex + 1);
        }

        if (startIndex + strLen + 1 >= data.length()) {
            throw new IOException("Incomplete bulk string");
        }

        if (strLen > AppConfig.getMaxBulkLength()) {
            throw new IOException("Invalid bulk string length: " + length);
        }

        StringBuilder result = new StringBuilder();

        int i = startIndex;
        while (strLen-- > 0) {
            result.append(data.charAt(i++));
        }
        if (data.charAt(i) != '\r' || i >= length - 1 || data.charAt(++i) != '\n')
            throw new IOException("Read failed! Invalid terminating CRLF found");
        else return new DecodeResult(new RespValue.RespBulkString(result.toString()), i);
    }

    private DecodeResult decodeArray(String data, int startIndex) throws IOException {
        var sizeDecode = decodeInteger(data, startIndex);
        long size = ((RespValue.RespInteger) sizeDecode.value).value();
        startIndex = sizeDecode.index + 1;

        if (size < 0) {
            if (!data.startsWith("\r\n", startIndex))
                throw new IOException("ERR No terminating CRLF found!");
            return new DecodeResult(new RespValue.RespNullArray(), startIndex + 1);
        }

        if (size > AppConfig.getMaxArrayLength()) {
            throw new IOException("Input exceeds array length limit");
        }

        List<RespValue> result = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            var decodeItem = decodeOne(data, startIndex);
            result.add(decodeItem.value);
            startIndex = decodeItem.index + 1;
        }
        return new DecodeResult(new RespValue.RespArray(result), startIndex - 1);
    }

    private DecodeResult decodeError(String data, int startIndex) throws IOException {
        var stringDecode = decodeString(data, startIndex);
        return new DecodeResult(new RespValue.RespError(((RespValue.RespString) stringDecode.value).value()), stringDecode.index);
    }
}

// *1\r\n$4\r\nECHO\r\n*2\r\n$4\r\nECHO\r\n$4\r\nPING\r\n