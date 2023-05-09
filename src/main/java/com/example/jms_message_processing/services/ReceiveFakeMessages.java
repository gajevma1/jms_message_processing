package com.example.jms_message_processing.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.HashMap;


@Service
public class ReceiveFakeMessages {
    private final static Logger logger = LoggerFactory.getLogger(ReceiveFakeMessages.class);

    private final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.queueName}")
    private String queueName;

    private final HashMap<Integer, Object> messageGroup = new HashMap<>();

    public ReceiveFakeMessages(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;

    }


    @JmsListener(destination = "${ibm.mq.queueName}", selector = "JMSXGroupSeq = 1")
    public void receiveFirstMessageOfGroup(Message message, Session session) throws Exception {
        logger.info("FIRST MESSAGE RECEIVED");
        logger.info("msg: {}", message.getBody(String.class));
        messageGroup.put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));
        try {
            if (receiveSecondToLastMessageOfGroup(message.getStringProperty("JMSXGroupID"))) {
                session.commit();
                logger.info("==================================== message for group: {} {", message.getStringProperty("JMSXGroupID"));
                messageGroup.forEach((key, value) -> logger.info("key: {}, value: {}", key, value));
                logger.info("} ====================================\n");
                messageGroup.clear();
            } else {
                logger.warn("rolling back the session!");
                session.rollback();
            }
        } catch (Exception e) {
            logger.error("something went wrong! \uD83D\uDE42");
            e.printStackTrace();
        }

    }

    public boolean receiveSecondToLastMessageOfGroup(String groupId) {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            connection = jmsTemplate.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Destination destination = session.createQueue("queue:///" + queueName);
            consumer = session.createConsumer(destination, "JMSXGroupID = '" + groupId + "'");

            connection.start();

            while (true) {
                Message message = consumer.receive(1000);
                if (message != null) {
                    // process the message
                    logger.info("SUB MESSAGE RECEIVED");
                    logger.info("msg: {}", message.getBody(String.class));
                    messageGroup.put(message.getIntProperty("JMSXGroupSeq"), message.getBody(String.class));

                    // let's crash things while processing some_fake_group_2
                    if (message.getStringProperty("JMSXGroupID").equals("some_fake_group_2")) {
                            throw new Exception("some fake error occurred :)");
                    }
                } else {
                    break;
                }
            }
            session.commit();
        } catch (Exception exception) {
            if (session != null) {
                try {
                    session.rollback();
                    messageGroup.clear();
                } catch (JMSException jmsException) {
                    jmsException.printStackTrace();
                }
            }
            exception.printStackTrace();
            return false;
        } finally {
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (JMSException jmsException) {
                    // Log exception
                    jmsException.printStackTrace();
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException jmsException) {
                    // Log exception
                    jmsException.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException jmsException) {
                    // Log exception
                    jmsException.printStackTrace();
                }
            }
        }
        return true;
    }
}
