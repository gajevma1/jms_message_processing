package com.example.jms_message_processing.rest;

import com.example.jms_message_processing.services.SendFakeMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class FakeMessageSenderController {

    SendFakeMessage sendFakeMessage;

    public FakeMessageSenderController(SendFakeMessage sendFakeMessage) {
        this.sendFakeMessage = sendFakeMessage;
    }

    @GetMapping("/srfm")
    public String sendSomeRealFakeMessages() throws IOException {
        sendFakeMessage.sendRealFakeMessages();
        return "sent real fake messages to DEV.QUEUE.2\nYou can check the queue <a href=\"https://localhost:9443/ibmmq/console/#/manage/qmgr/QM1/queue/local/DEV.QUEUE.2/view\" target=\"_blank\">here</a>";
    }
}
