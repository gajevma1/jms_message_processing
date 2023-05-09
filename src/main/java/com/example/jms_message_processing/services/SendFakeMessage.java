package com.example.jms_message_processing.services;

import com.example.jms_message_processing.model.FakeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class SendFakeMessage {
    private final static Logger logger = LoggerFactory.getLogger(SendFakeMessage.class);

    private final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.queueName}")
    private String destinationQueue;

    public SendFakeMessage(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendFakeBinaryMessage() {
        jmsTemplate.convertAndSend(destinationQueue, new Byte[0], message -> {
            message.setBooleanProperty("zero_bytes_message", true);
            message.setJMSMessageID("some_unique_id");
            message.setJMSCorrelationID("1234");
            return message;
        });
    }

    public void createAndSendFakeMessage(String msg, String groupId, Integer groupSeq) {
        jmsTemplate.convertAndSend(destinationQueue, msg, message -> {
            message.setStringProperty("JMSXGroupID", groupId);
            message.setIntProperty("JMSXGroupSeq", groupSeq);
            return message;
        });
    }

    public List<String> createFakeGroups(Integer amount) {
        List<String> groups = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            groups.add("some_fake_group_" + i);
        }
        return groups;
    }

    public void sendFakeMessageGroup(Integer messageGroups, Integer messageParts) {
        HashSet<FakeMessage> hashSet = new HashSet<>(messageGroups * messageParts);
        List<String> groups = createFakeGroups(messageGroups);
        for (String group : groups) {
            for (int i = 1; i <= messageParts; i++) {
                logger.info("adding message " + i + " of group " + group);
                hashSet.add(new FakeMessage("msg " + i + ", gr " + group, group, i));

                // send messages sequentially, in order of creation
                /*createAndSendFakeMessage("message " + i + ", of group " + group, group, i);*/
            }
        }

        // send messages in random order
        for (FakeMessage fakeMessage : hashSet) {
            logger.info("sending message {} of group {}", fakeMessage.getGroupSeq(), fakeMessage.getGroupId());
            createAndSendFakeMessage(fakeMessage.getMessage(), fakeMessage.getGroupId(), fakeMessage.getGroupSeq());
        }
    }
}
