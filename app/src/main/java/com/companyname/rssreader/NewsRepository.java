package com.companyname.rssreader;

import android.text.Html;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public final class NewsRepository {
    public NewsRepository(@NonNull FeedDataSource habr, @NonNull FeedDataSource meduza) {
        this.habr = habr;
        this.meduza = meduza;
    }

    public Single<List<Logo>> retrieveLogos() {
        final Single<Logo> habrSingle = habr.getLogo();
        final Single<Logo> meduzaSingle = meduza.getLogo();
        return Observable.merge(habrSingle.toObservable(), meduzaSingle.toObservable())
                         .toList().subscribeOn(Schedulers.io());
    }

    public Single<List<News>> retrieveNewsSince(long timestamp, int count)
    {
        final Observable<FeedEntry> habrObservable =
                habr.retrieveEntriesSince(timestamp, count);
        final Observable<FeedEntry> meduzaObservable =
                meduza.retrieveEntriesSince(timestamp, count);

        return Observable
                .merge(habrObservable, meduzaObservable)
                .retry()
                .toSortedList(new TimeComparator())
                .flatMapObservable(Observable::fromIterable)
                .take(count)
                .observeOn(Schedulers.computation())
                .map(item ->
                {
                    final int MAX_CHARACTER_COUNT = 128;
                    final String imageURL = extractImageURL(item.description);
                    final String description = eliminateHTMLTags(item.description);
                    final int endIndex = Math.min(description.length(), MAX_CHARACTER_COUNT);
                    return new News(item.title, description.substring(0, endIndex),
                                    imageURL, item.sourceHost, item.timestamp);
                })
                .toList();
    }

    private String extractImageURL(String description) {
        final Pattern pattern = Pattern.compile("src=\"(.*?)\"");
        final Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            final int capturingGroup = 1;
            return matcher.group(capturingGroup);
        }
        return "";
    }

    private String eliminateHTMLTags(String text) {
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
