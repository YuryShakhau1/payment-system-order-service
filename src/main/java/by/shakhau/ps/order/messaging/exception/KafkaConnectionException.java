package by.shakhau.ps.order.messaging.exception;

public class KafkaConnectionException extends RuntimeException {
    public KafkaConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
