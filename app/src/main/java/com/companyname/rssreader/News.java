package com.companyname.rssreader;

public final class News {
    public final String title;
    public final String description;
    public final String imageURL;
    public final String source;
    public final long timestamp;
    public News(String title, String description, String imageURL, String source, long timestamp) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.source = source;
        this.timestamp = timestamp;
    }
}
