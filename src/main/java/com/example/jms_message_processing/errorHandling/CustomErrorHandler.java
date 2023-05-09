package com.example.jms_message_processing.errorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
public class CustomErrorHandler implements ErrorHandler {

    private final static Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
        logger.error("some shit went down! \uD83D\uDE43");
    }
}
