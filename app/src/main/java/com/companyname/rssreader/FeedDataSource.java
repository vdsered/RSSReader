package com.companyname.rssreader;


import io.reactivex.Observable;

public interface FeedDataSource {
    Observable<FeedEntry> retrieveEntriesSince(long timestamp, int count);
}
