package command;

import resp.RespValue;

public class CommandHandler {
    public RespValue handleCommand(RedixCmd command) {
        return switch (command.command()) {
            case "PING" -> new RespValue.RespString("PONG");
            case "ECHO" -> {
                if (command.args().length != 1) {
                    yield new RespValue.RespError("ERR wrong number of arguments for 'echo' command");
                }
                yield new RespValue.RespBulkString(command.args()[0]);
            }
            case "SET" -> new RespValue.RespString("OK");
            default -> new RespValue.RespString("PING");
        };
    }
}
