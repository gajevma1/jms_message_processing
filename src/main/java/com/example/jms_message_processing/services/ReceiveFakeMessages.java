package com.example.jms_message_processing.services;

import com.example.jms_message_processing.model.MessageGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;


//@Service
public class ReceiveFakeMessages {
    private final static Logger logger = LoggerFactory.getLogger(ReceiveFakeMessages.class);

    @Value("${ibm.mq.queues[0]}")
    private String queueName;

    private final MessageGroup messageGroup = new MessageGroup();

    @JmsListener(destination = "${ibm.mq.queues[0]}", selector = "JMSXGroupSeq = 1")
    public void receiveFirstMessageOfGroup(Message message, Session session) throws Exception {
        logger.info("FIRST MESSAGE RECEIVED");
//        logger.info("msg: {}", message.getBody(String.class));
        logger.info("msg: {}", message);
        messageGroup.getMessageGroup().put(message.getIntProperty("JMSXGroupSeq"), message);
        try {
            receiveSecondToLastMessageOfGroup(message.getStringProperty("JMSXGroupID"), session);
            if (!messageGroup.isFailed()) {
                session.commit();
                logger.info("===================================================================");
                logger.info("message for group: {} {", message.getStringProperty("JMSXGroupID"));
                for (Map.Entry<Integer, Object> entry : messageGroup.getMessageGroup().entrySet()) {
                    Integer key = entry.getKey();
                    Message value = (Message) entry.getValue();
                    if (!value.getBooleanProperty("JMS_IBM_Last_Msg_In_Group")) {
                        logger.info("key: {}, value: {}", key, new String(value.getBody(byte[].class), StandardCharsets.UTF_8));
                    }
                }
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

    private void receiveSecondToLastMessageOfGroup(String groupId, Session session) throws JMSException {

        Destination destination = session.createQueue("queue:///" + queueName);
        try (MessageConsumer consumer = session.createConsumer(destination, "JMSXGroupID = '" + groupId + "'")) {
            Message lastMessage = null;

            while (true) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    // process the message
                    logger.info("SUB MESSAGE RECEIVED");
                    if (message.getBody(byte[].class) != null) {
                        logger.info("subMsg: {}", new String(message.getBody(byte[].class), StandardCharsets.UTF_8));
                    }
                    messageGroup.getMessageGroup().put(message.getIntProperty("JMSXGroupSeq"), message);
                    lastMessage = message;
                } else {
                    // let's crash things while processing some_fake_group_2
                    break;
                }
            }
            if (lastMessage != null && lastMessage.getStringProperty("JMSXGroupID").equals("some_fake_group_2")) {
                throw new Exception("some fake error occurred :)");
            }
        } catch (Exception exception) {
            messageGroup.setFailed(true);
            exception.printStackTrace();
        }
    }
}
