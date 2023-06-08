package com.example.jms_message_processing.rest;

import com.example.jms_message_processing.retry.FakeMessageFetchingService;
import com.example.jms_message_processing.retry.RetryServiceImpl;
import com.example.jms_message_processing.services.SendFakeMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class FakeMessageSenderController {

    SendFakeMessage sendFakeMessage;
    RetryServiceImpl retryService;
    FakeMessageFetchingService fakeMessageFetchingService;

    public FakeMessageSenderController(SendFakeMessage sendFakeMessage, RetryServiceImpl retryService, FakeMessageFetchingService fakeMessageFetchingService) {
        this.sendFakeMessage = sendFakeMessage;
        this.retryService = retryService;
        this.fakeMessageFetchingService = fakeMessageFetchingService;
    }

    @GetMapping("/srfm")
    public String sendSomeRealFakeMessages() throws IOException {
        sendFakeMessage.sendRealFakeMessages();
        return "sent real fake messages to DEV.QUEUE.2\nYou can check the queue <a href=\"https://localhost:9443/ibmmq/console/#/manage/qmgr/QM1/queue/local/DEV.QUEUE.2/view\" target=\"_blank\">here</a>";
    }

    @GetMapping("/r")
    public String retryExample() {
        return sendFakeMessage.retryExample();
    }

    @GetMapping("/r2")
    public String retryExampleTwo() {
        return retryService.retry();
    }

    @GetMapping("/r3/{msg}&{fail}")
    public void fakeMessageFetching(@PathVariable(value = "msg") String msg, @PathVariable(value = "fail") boolean fail) {
        fakeMessageFetchingService.fakeFetchMessage(msg, fail);
    }
}
