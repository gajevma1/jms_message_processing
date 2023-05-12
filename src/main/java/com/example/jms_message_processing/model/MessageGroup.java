package com.example.jms_message_processing.model;

import java.util.HashMap;

public class MessageGroup {
    private boolean failed;
    private HashMap<Integer, Object> messageGroup;

    public MessageGroup() {
        this.failed = false;
        this.messageGroup = new HashMap<>();
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public HashMap<Integer, Object> getMessageGroup() {
        return messageGroup;
    }

    public void resetMessageGroup() {
        this.messageGroup = new HashMap<>();
        this.failed = false;
    }
}
