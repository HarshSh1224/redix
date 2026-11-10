package command;

import resp.RespValue;

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

    public static RedixCmd from(RespValue.RespArray input) {
        List<RespValue> array = input.value();

        if (array.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }

        if (!(array.getFirst() instanceof RespValue.RespBulkString(String command))) {
            throw new IllegalArgumentException("Elements must be bulk string");
        }

        String[] args = new String[array.size() - 1];

        for (int i = 1; i < array.size(); i++) {
            if (!(array.get(i) instanceof RespValue.RespBulkString(String arg))) {
                throw new IllegalArgumentException("Elements must be bulk string");
            }
            args[i - 1] = arg;
        }
        return new RedixCmd(command, args);
    }
}