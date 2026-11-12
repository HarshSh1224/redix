package command;

import resp.RespValue;
import store.KeyValueStore;

import java.util.concurrent.TimeUnit;

public class CommandHandler {
    private final KeyValueStore store;

    public CommandHandler(KeyValueStore store) {
        this.store = store;
    }

    public RespValue handleCommand(RedixCmd command) {
        return switch (command.command()) {
            case "PING" -> new RespValue.RespString("PONG");
            case "ECHO" -> {
                if (command.args().length != 1) {
                    yield new RespValue.RespError("ERR wrong number of arguments for 'echo' command");
                }
                yield new RespValue.RespBulkString(command.args()[0]);
            }
            case "SET" -> handleSet(command);
            case "GET" -> handleGet(command);
            case "TTL" -> handleTTL(command);
            default -> new RespValue.RespError("ERR unknown command");
        };
    }

    private RespValue handleGet(RedixCmd command) {
        var args = command.args();

        if (args.length != 1) {
            return new RespValue.RespError("ERR wrong number of arguments for 'get' command");
        }

        String key = args[0];
        String value = store.get(key);

        return value == null ? new RespValue.RespNullBulkString() : new RespValue.RespString(value);
    }

    private RespValue handleTTL(RedixCmd command) {
        var args = command.args();

        if (args.length != 1) {
            return new RespValue.RespError("ERR wrong number of arguments for 'ttl' command");
        }

        String key = args[0];
        return new RespValue.RespInteger(store.ttl(key));
    }

    private RespValue handleSet(RedixCmd command) {
        var args = command.args();

        if (args.length <= 1 || args.length > 4)
            return new RespValue.RespError("ERR wrong number of arguments for 'set' command");

        if (args.length == 3) {
            return new RespValue.RespError("ERR syntax error");
        }

        Long expiry = null;

        if (args.length == 4) {
            if (!args[2].equalsIgnoreCase("EX") && !args[2].equalsIgnoreCase("PX")) {
                return new RespValue.RespError("ERR syntax error");
            } else {
                try {
                    expiry = Long.parseLong(args[3]);
                    if (args[2].equals("EX")) expiry *= 1000;

                    if (expiry <= 0) return new RespValue.RespError("ERR invalid expire time in 'set' command");

                    expiry = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(expiry);
                } catch (NumberFormatException e) {
                    return new RespValue.RespError("ERR value is not an integer or out of range");
                }
            }
        }

        String key = args[0];
        String val = args[1];

        store.set(key, val, expiry);

        return new RespValue.RespString("OK");
    }
}
