package com.alakamandawalk.popcorn.model;

public class MessageData {

    String message, messageId, messageTime, storyId, storyName, authorId;

    public MessageData() {
    }

    public MessageData(String message, String messageId, String messageTime, String storyId, String storyName, String authorId) {
        this.message = message;
        this.messageId = messageId;
        this.messageTime = messageTime;
        this.storyId = storyId;
        this.storyName = storyName;
        this.authorId = authorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
