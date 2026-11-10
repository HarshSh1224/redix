package resp;

public class RespEncoder {
    public String encodeResp(RespValue respValue) {
        return switch (respValue) {

            case RespValue.RespString s -> "+%s\r\n".formatted(s.value());

            case RespValue.RespBulkString s -> "$%d\r\n%s\r\n".formatted(s.value().length(), s.value());

            case RespValue.RespInteger i -> ":%d\r\n".formatted(i.value());

            case RespValue.RespArray a -> {
                StringBuilder result = new StringBuilder();
                for(var val: a.value()) {
                    result.append(encodeResp(val));
                }
                yield "*%d\r\n%s\r\n".formatted(a.value().size(), result.toString());
            }

            case RespValue.RespError e -> "-%s\r\n".formatted(e.message());

            case RespValue.RespNullArray ignored -> "*-1\r\n";

            case RespValue.RespNullBulkString ignored -> "$-1\r\n";
        };
    }
}
