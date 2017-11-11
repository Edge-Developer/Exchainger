package com.exchainger.exchainger.Model;

import java.util.Date;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/16/2017.
 */


public class ChatMessage {
    private String text;
    private String userName;
    private long time;
    private String photoUrl;

    public ChatMessage() {
    }

    public ChatMessage(String messageText, String userName, String photoUrl) {
        this.text = messageText;
        this.userName = userName;
        time = new Date().getTime();
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public long getTime() {
        return time;
    }
}