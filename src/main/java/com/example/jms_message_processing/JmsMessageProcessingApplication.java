package com.example.jms_message_processing;

import com.example.jms_message_processing.services.SendFakeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableJms
public class JmsMessageProcessingApplication implements ApplicationRunner {
    private final static Logger logger = LoggerFactory.getLogger(JmsMessageProcessingApplication.class);

    @Autowired
    SendFakeMessage sendFakeMessage;

    public static void main(String[] args) {
        SpringApplication.run(JmsMessageProcessingApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        logger.info("gajevma1, attempting ot send some messages...");
//        sendFakeMessage.sendFakeBinaryMessage();
//        sendFakeMessage.sendFakeMessageGroup(4,3);
//        sendFakeMessage.sendRealFakeMessages();
    }
}

