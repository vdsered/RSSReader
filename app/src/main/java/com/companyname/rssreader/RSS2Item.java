package com.companyname.rssreader;

public final class RSS2Item {
    public final String description;
    public final String title;
    public final String pubDate;

    public RSS2Item(String description, String title, String pubDate) {
        this.description = description;
        this.title = title;
        this.pubDate = pubDate;
    }
}
