package com.example.cytocheck;

public class RequestInfo {
    /* This class is used to hold the requests for a provider account and information based on the
    *  processed response from the server. */
    private int messageID;
    private String message;
    private String messageType;
    private int senderID;

    public RequestInfo(int id, String message, String messageType, int senderID) {
        this.messageID = id;
        this.message = message;
        this.messageType = messageType;
        this.senderID = senderID;
    }

    public int getId() {
        return messageID;
    }

    public String getMessage() {
        return message;
    }
    public String getMessageType() {return messageType;}
    public int getSenderID() {return  senderID;}
}