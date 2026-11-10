package command;

import resp.RespValue;

import java.util.Arrays;
import java.util.List;

public record RedixCmd(
    String command,
    String[] args
) {
    public RedixCmd {
        command = command.toUpperCase();
        args = args.clone();
    }

    @Override
    public String[] args() {
        return args.clone();
    }

    public static RedixCmd from(RespValue input) {
        if (!(input instanceof RespValue.RespArray(List<RespValue> values))) {
            throw new IllegalArgumentException("Expected RESP Array");
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }

        if (!(values.getFirst() instanceof RespValue.RespBulkString(String command))) {
            throw new IllegalArgumentException("Elements must be bulk string");
        }

        String[] args = new String[values.size() - 1];

        for (int i = 1; i < values.size(); i++) {
            if (!(values.get(i) instanceof RespValue.RespBulkString(String arg))) {
                throw new IllegalArgumentException("Elements must be bulk string");
            }
            args[i - 1] = arg;
        }
        return new RedixCmd(command, args);
    }

    @Override
    public String toString() {
        return "RedixCmd[command=%s, args=%s]".formatted(command, Arrays.toString(args));
    }
}