package com.companyname.rssreader;

public final class FeedEntry {
    public final String description;
    public final String title;
    public final long timestamp;
    public final String sourceHost;
    public FeedEntry(String description, String title, long timestamp, String sourceHost) {
        this.description = description;
        this.title = title;
        this.timestamp = timestamp;
        this.sourceHost = sourceHost;
    }
}
