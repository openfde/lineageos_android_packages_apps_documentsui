package com.android.documentsui;

public class MessageEvent {
    private String method ;
    private String message;

    public MessageEvent(String method ,String message) {
        this.method = method;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getMethod() {
        return method;
    }
}
