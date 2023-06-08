package com.example.jms_message_processing.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class FakeMessageProcessingService {
    private final static Logger logger = LoggerFactory.getLogger(FakeMessageProcessingService.class);

    @Retryable(backoff = @Backoff(multiplierExpression = "${retry.multiplier}", delayExpression = "${retry.delay}", maxDelayExpression = "${retry.maxDelay}"), maxAttemptsExpression = "${retry.maxAttempts}")
    public String fakeProcessMessage(String msg, boolean fail) {
        String response = msg + "_response";
        if (fail) {
            logger.info("throwing fake message processing exception!");
            throw new CustomRetryException("fake message processing exception");
        }
        logger.info("successfully fake processed the message");
        return response;
    }

    @Recover String recover(String response) {
        return response + "_response_from_recovery";
    }
}
