package com.companyname.rssreader;

import android.text.Html;

import java.util.Comparator;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public final class NewsRepository {
    public NewsRepository(@NonNull FeedDataSource habr, @NonNull FeedDataSource meduza) {
        this.habr = habr;
        this.meduza = meduza;
    }

    public Observable<News> retrieveNewsSince(long timestamp, int count)
    {
        final Observable<FeedEntry> habrObservable =
                habr.retrieveEntriesSince(timestamp, count);
        final Observable<FeedEntry> meduzaObservable =
                meduza.retrieveEntriesSince(timestamp, count);

        return Observable
                .merge(habrObservable, meduzaObservable)
                .toSortedList(new TimeComparator())
                .flatMapObservable(Observable::fromIterable)
                .take(count)
                .observeOn(Schedulers.computation())
                .map(item ->
                {
                    final int MAX_CHARACTER_COUNT = 128;
                    String description = eliminateHTMLTags(item.description);
                    final int endIndex = Math.min(description.length(), MAX_CHARACTER_COUNT);
                    return new News(item.title, description.substring(0, endIndex),
                            "", "", item.timestamp);
                });
    }

    private String eliminateHTMLTags(String text)
    {
        /*
            TODO
            Should it remove HTML tags via regex?
         */
        return Html.fromHtml(text).toString();
    }

    private static class TimeComparator implements Comparator<FeedEntry> {
        @Override
        public int compare(FeedEntry left, FeedEntry right) {
            return Long.signum(right.timestamp - left.timestamp);
        }
    }

    private final FeedDataSource habr;
    private final FeedDataSource meduza;
}
