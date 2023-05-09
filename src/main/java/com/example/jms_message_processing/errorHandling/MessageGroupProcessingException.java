package com.example.jms_message_processing.errorHandling;

public class MessageGroupProcessingException extends Exception{

    public MessageGroupProcessingException() {
    }

    public MessageGroupProcessingException(String message) {
        super(message);
    }

    public MessageGroupProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageGroupProcessingException(Throwable cause) {
        super(cause);
    }

    public MessageGroupProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
