package com.example.jms_message_processing.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RetryServiceImpl implements RetryService {
    private final static Logger logger = LoggerFactory.getLogger(RetryServiceImpl.class);

    @Override
    public String retry() {
        int random = new Random().nextInt(4);
        if (true) {
            logger.info("-interface----> sorry, you've got number {}", random);
            throw new CustomRetryException("Throw custom exception");
        }
        logger.info("-interface----> sorry, your lucky number is {}", random);
        return "hello \uD83D\uDE43";
    }

    @Override
    public String recover(Throwable throwable) {
        logger.info("Default Retry servive test");
        return "Error Class :: " + throwable.getClass().getName();
    }
}
