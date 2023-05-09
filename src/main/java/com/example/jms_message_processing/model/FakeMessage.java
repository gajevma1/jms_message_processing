package com.example.jms_message_processing.model;

public class FakeMessage {
    private final String message;
    private final String groupId;
    private final Integer groupSeq;

    public FakeMessage(String message, String groupId, Integer groupSeq) {
        this.message = message;
        this.groupId = groupId;
        this.groupSeq = groupSeq;
    }

    public String getMessage() {
        return message;
    }

    public String getGroupId() {
        return groupId;
    }

    public Integer getGroupSeq() {
        return groupSeq;
    }
}
