package com.example.jms_message_processing.services;

import com.example.jms_message_processing.errorHandling.MessageGroupProcessingException;
import com.example.jms_message_processing.model.MessageGroup;
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

    private MessageGroup messageGroup = new MessageGroup();

    @JmsListener(destination = "${ibm.mq.queueName}", selector = "JMSXGroupSeq = 1")
    public void receiveFirstMessageOfGroup(Message message, Session session) throws Exception {
        logger.info("FIRST MESSAGE RECEIVED");
//        logger.info("msg: {}", message.getBody(String.class));
        logger.info("msg: {}", message);
        messageGroup.getMessageGroup().put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));
        try {
            receiveSecondToLastMessageOfGroup(message.getStringProperty("JMSXGroupID"), session);
            if (!messageGroup.isFailed()) {
                session.commit();
                logger.info("===================================================================");
                logger.info("message for group: {} {", message.getStringProperty("JMSXGroupID"));
                messageGroup.getMessageGroup().forEach((key, value) -> logger.info("key: {}, value: {}", key, value));
                logger.info("}");
                logger.info("===================================================================");
            } else {
                logger.warn("rolling back the session!");
                session.rollback();
            }
            messageGroup.resetMessageGroup();
        } catch (Exception e) {
            logger.error("something went wrong! \uD83D\uDE42");
            e.printStackTrace();
            // this should send the messages to DLQ, but let's exit for now...
            if (message.getIntProperty("JMSXDeliveryCount") > 3) {
                System.exit(1);
            }
        }

    }

    public void receiveSecondToLastMessageOfGroup(String groupId, Session session) {

        MessageConsumer consumer = null;
        try {
            Destination destination = session.createQueue("queue:///" + queueName);
            consumer = session.createConsumer(destination, "JMSXGroupID = '" + groupId + "'");
            Message lastMessage = null;

            while (true) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    // process the message
                    logger.info("SUB MESSAGE RECEIVED");
                    logger.info("subMsg: {}", message.getBody(String.class));
                    messageGroup.getMessageGroup().put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));
                    lastMessage = message;
                } else {
                    // let's crash things while processing some_fake_group_2
                    if (lastMessage.getStringProperty("JMSXGroupID").equals("some_fake_group_2")) {
                        messageGroup.setFailed(true);
                        throw new Exception("some fake error occurred :)");
                    }
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
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
    }
}
