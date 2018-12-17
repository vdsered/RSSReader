package com.companyname.rssreader;

public final class FeedEntry {
    public final String description;
    public final String title;
    public final long timestamp;

    public FeedEntry(String description, String title, long timestamp) {
        this.description = description;
        this.title = title;
        this.timestamp = timestamp;
    }
}
