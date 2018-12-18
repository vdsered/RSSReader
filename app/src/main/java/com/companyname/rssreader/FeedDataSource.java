package com.companyname.rssreader;


import io.reactivex.Observable;
import io.reactivex.Single;

public interface FeedDataSource {
    Single<Logo> getLogo();

    Observable<FeedEntry> retrieveEntriesSince(long timestamp, int count);

}
