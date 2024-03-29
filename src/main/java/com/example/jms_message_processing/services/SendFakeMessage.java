package com.example.jms_message_processing.services;

import com.example.jms_message_processing.model.FakeMessage;
import com.example.jms_message_processing.retry.CustomRetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class SendFakeMessage {
    private final static Logger logger = LoggerFactory.getLogger(SendFakeMessage.class);

    private final JmsTemplate jmsTemplate;

    @Value("${ibm.mq.queues[0]}")
    private String destinationQueueOne;
    @Value("${ibm.mq.queues[1]}")
    private String destinationQueueTwo;
    @Value("${ibm.mq.queues[2]}")
    private String destinationQueueThree;

    public SendFakeMessage(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendFakeBinaryMessage() {
        jmsTemplate.convertAndSend(destinationQueueOne, new Byte[0], message -> {
            message.setBooleanProperty("zero_bytes_message", true);
            message.setJMSMessageID("some_unique_id");
            message.setJMSCorrelationID("1234");
            return message;
        });
    }

    public void createAndSendFakeMessage(String destinationQueue, Object msg, String groupId, Integer groupSeq, boolean isLastInGroup) {
        jmsTemplate.convertAndSend(destinationQueue, msg, message -> {
            message.setStringProperty("JMSXGroupID", groupId);
            message.setIntProperty("JMSXGroupSeq", groupSeq);
            if (isLastInGroup) {
                message.setBooleanProperty("JMS_IBM_Last_Msg_In_Group", true);
            }
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
//                logger.info("adding message " + i + " of group " + group);
                hashSet.add(new FakeMessage(group + ", msg " + i, group, i));

                // send messages sequentially, in order of creation
                /*createAndSendFakeMessage("message " + i + ", of group " + group, group, i);*/
            }
        }

        // send messages in random order
        for (FakeMessage fakeMessage : hashSet) {
//            logger.info("sending message {} of group {}", fakeMessage.getGroupSeq(), fakeMessage.getGroupId());
            createAndSendFakeMessage("DEV.QUEUE.1", fakeMessage.getMessage(), fakeMessage.getGroupId(), fakeMessage.getGroupSeq(), fakeMessage.getGroupSeq().equals(messageParts));
        }
    }

    public void sendRealFakeMessages() throws IOException {
        logger.info("sending real fake messages to {}", destinationQueueOne);
        String rnd = UUID.randomUUID().toString();

//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_01.headers")), "msg_01", 1, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_01.xml")), "msg_01", 2, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_01.zero")), "msg_01", 3, true);

//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_02.headers")), "msg_02", 1, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_02.xml")), "msg_02", 2, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_02.zero")), "msg_02", 3, true);

//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_03.headers")), "msg_03", 1, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_03.xml")), "msg_03", 2, false);
//        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_03.zero")), "msg_03", 3, true);

        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_04.headers")), "msg_04_" + rnd, 1, false);
        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_04a.xml")), "msg_04_" + rnd, 2, false);
        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_04b.xml")), "msg_04_" + rnd, 3, false);
        createAndSendFakeMessage(destinationQueueOne, Files.readAllBytes(Paths.get("src/main/resources/garbageFiles/msg_04.zero")), "msg_04_" + rnd, 4, true);

        logger.info("sent real fake messages to DEV.QUEUE.2");
    }

 /*   @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(3000))*/
    @Retryable(backoff = @Backoff(multiplier = 6, delay = 1000L, maxDelay = 1000 * 3), maxAttempts = 5)
    public String retryExample() {
        int random = new Random().nextInt(4);
        if (true) {
            logger.info("-+----> sorry, you've got number {}", random);
            throw new CustomRetryException("some fake exception");
        }
        logger.info("-+----> sorry, your lucky number is {}", random);
        return "hello \uD83D\uDE43";
    }

    @Recover
    public String recover(Throwable throwable) {
        logger.info("Default Retry servive test");
        return "Error Class :: " + throwable.getClass().getName();
    }
}
