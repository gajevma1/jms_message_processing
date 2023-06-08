package com.example.jms_message_processing.retry;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

public interface RetryService {

    @Retryable(value = {CustomRetryException.class},
            maxAttempts = 3,
            backoff = @Backoff(1000))
    public String retry() throws CustomRetryException;

    @Recover
    public String recover(Throwable throwable);
}
