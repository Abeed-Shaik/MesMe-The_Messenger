package com.abeed.mesme.Models;

public class Message {
    private String messageId, message, senderId, recieverId;
    private long timestamp;
    private boolean isseen = false;
    private int quotePos = -1;
    private long quoteMsgPos;
    private String qoutename = "";
    private String qoute = "";



    public Message() {
    }

    public Message(String message, String senderId, String recieverId, long timestamp, int quotePos, long quoteMsgPos, String qoutename, String qoute) {
        this.message = message;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.timestamp = timestamp;
        this.quotePos = quotePos;
        this.quoteMsgPos = quoteMsgPos;
        this.qoutename = qoutename;
        this.qoute = qoute;

    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecieverId() {
        return recieverId;
    }

    public void setRecieverId(String recieverId) {
        this.recieverId = recieverId;
    }

    public Boolean getIsseen() {
        return isseen;
    }

    public void setIsseen(Boolean isseen) {
        this.isseen = isseen;
    }

    public int getQuotePos() {
        return quotePos;
    }

    public void setQuotePos(int quotePos) {
        this.quotePos = quotePos;
    }

    public long getQuoteMsgPos() {
        return quoteMsgPos;
    }

    public void setQuoteMsgPos(Long quoteMsgPos) {
        this.quoteMsgPos = quoteMsgPos;
    }

    public String getQoutename() {
        return qoutename;
    }

    public void setQoutename(String qoutename) {
        this.qoutename = qoutename;
    }

    public String getQoute() {
        return qoute;
    }

    public void setQoute(String qoute) {
        this.qoute = qoute;
    }

}
