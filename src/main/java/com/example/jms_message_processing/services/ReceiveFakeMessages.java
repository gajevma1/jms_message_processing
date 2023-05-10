package com.example.jms_message_processing.services;

import com.example.jms_message_processing.errorHandling.MessageGroupProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.HashMap;


@Service
public class ReceiveFakeMessages {
    private final static Logger logger = LoggerFactory.getLogger(ReceiveFakeMessages.class);

    @Value("${ibm.mq.queueName}")
    private String queueName;

    private final HashMap<Integer, Object> messageGroup = new HashMap<>();

    @JmsListener(destination = "${ibm.mq.queueName}", selector = "JMSXGroupSeq = 1")
    public void receiveFirstMessageOfGroup(Message message, Session session) throws Exception {
        logger.info("FIRST MESSAGE RECEIVED");
//        logger.info("msg: {}", message.getBody(String.class));
        logger.info("msg: {}", message);
        messageGroup.put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));
        try {
            if (receiveSecondToLastMessageOfGroup(message.getStringProperty("JMSXGroupID"), session)) {
                session.commit();
                logger.info("==================================== message for group: {} {", message.getStringProperty("JMSXGroupID"));
                messageGroup.forEach((key, value) -> logger.info("key: {}, value: {}", key, value));
                logger.info("} ====================================\n");
                messageGroup.clear();
            } else {
                logger.warn("rolling back the session!");
                session.rollback();
                throw new MessageGroupProcessingException(" --- well, shit!");
            }
        } catch (Exception e) {
            logger.error("something went wrong! \uD83D\uDE42");
            e.printStackTrace();
            // this should send the messages to DLQ, but let's exit for now...
            if (message.getIntProperty("JMSXDeliveryCount") > 3) {
                System.exit(1);
            }
        }

    }

    public boolean receiveSecondToLastMessageOfGroup(String groupId, Session session) {

        MessageConsumer consumer = null;
        try {
            Destination destination = session.createQueue("queue:///" + queueName);
            consumer = session.createConsumer(destination, "JMSXGroupID = '" + groupId + "'");

            while (true) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    // process the message
                    logger.info("SUB MESSAGE RECEIVED");
                    logger.info("subMsg: {}", message.getBody(String.class));
                    messageGroup.put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));

                    // let's crash things while processing some_fake_group_2
                    if (message.getStringProperty("JMSXGroupID").equals("some_fake_group_2")) {
                            throw new Exception("some fake error occurred :)");
                    }
                } else {
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        } finally {
            if (consumer != null) {
                try {
                    logger.info("... closing the consumer for sub-messages ... ...");
                    consumer.close();
                } catch (JMSException jmsException) {
                    // Log exception
                    jmsException.printStackTrace();
                }
            }
        }
        return true;
    }
}
