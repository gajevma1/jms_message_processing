package com.example.jms_message_processing.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FakeMessageFetchingService {
    private final static Logger logger = LoggerFactory.getLogger(FakeMessageFetchingService.class);
    private FakeMessageProcessingService fakeMessageProcessingService;

    public FakeMessageFetchingService(FakeMessageProcessingService fakeMessageProcessingService) {
        this.fakeMessageProcessingService = fakeMessageProcessingService;
    }

    public void fakeFetchMessage(String msg, boolean fail) {
        logger.info("01 - pretending to fetch a message");
        logger.info("02 - call fake message processing service");
        String processedMsg = null;
        try {
            processedMsg = fakeMessageProcessingService.fakeProcessMessage(msg, fail);
        } catch (Exception exception) {
            logger.error("got an exception while trying to fake process a message: {}", exception.getMessage());
        }
        logger.info("03 - return fake results: {}", processedMsg);
    }
}
