package resp;

import java.util.List;

public sealed interface RespValue {

    record RespString(String value) implements RespValue{}

    record RespInteger(long value) implements RespValue{}

    record RespError(String message) implements RespValue{}

    record RespBulkString(String value) implements RespValue{}

    record RespArray(List<RespValue> value) implements RespValue{
        public RespArray(List<RespValue> value) {
            this.value = List.copyOf(value);
        }
    }

    record RespNullBulkString() implements RespValue{}

    record RespNullArray() implements RespValue{}
}
