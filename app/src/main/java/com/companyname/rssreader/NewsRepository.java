package com.companyname.rssreader;

import java.util.Comparator;

import io.reactivex.Observable;

public final class NewsRepository {

    private static class TimeComparator implements Comparator<RSS2Item> {
        @Override
        public int compare(RSS2Item o1, RSS2Item o2) {
            return 0;
        }
    }

    public Observable<News> retrieveNewsSince(long timestamp, int count)
    {
        final Observable<RSS2Item> habrObservable = habr.retrieveItemsSince(timestamp, count);
        final Observable<RSS2Item> meduzaObservable = habr.retrieveItemsSince(timestamp, count);

        return Observable
                .merge(habrObservable, meduzaObservable)
                .toSortedList(new TimeComparator())
                .flatMapObservable(Observable::fromIterable)
                .take(count)
                .map(item ->
                {
                    return new News(item.title, item.description, "", "", 0);
                });
    }

    private final RSS2DataSource habr =
            new RSS2DataSource("https://habr.com/rss/hubs/all/");
    private final RSS2DataSource meduza =
            new RSS2DataSource("https://meduza.io/rss/podcasts/meduza-v-kurse");
}
