package lewocz.graphics.exception;

import java.io.IOException;

public class InvalidFormatException extends IOException {
    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}