package org.example;

public class URLInfo {
    public String originalURL;
    public int clickLimit;
    public long expiryTime;
    public int clicks = 0;  // Количество переходов
    public String creatorUUID;


    public URLInfo(String originalURL, int clickLimit, long expiryTime, String creatorUUID) {
        this.originalURL = originalURL;
        this.clickLimit = clickLimit;
        this.expiryTime = expiryTime;
        this.creatorUUID = creatorUUID;
    }
}