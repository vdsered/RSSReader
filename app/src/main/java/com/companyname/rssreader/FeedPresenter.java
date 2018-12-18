package com.companyname.rssreader;

import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class FeedPresenter {

    public FeedPresenter(NewsRepository newsRepository, FeedView view) {
        this.newsRepository = newsRepository;
        this.view = view;
    }

    public void initialize(int initialFeedEntryCount) {
        loadingDisposable = newsRepository
                .retrieveLogos()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(logos -> view.setDefaultNewsImages(logos),
                        error -> view.showErrorMessage());
        loadMoreNews(initialFeedEntryCount, System.currentTimeMillis());
    }

    public void clear() {
        view = null;
        loadingDisposable.dispose();
    }
    public void onNeedMoreNews(int count, long timestamp) {
        loadingDisposable.dispose();
        loadMoreNews(count, timestamp);
    }

    private void loadMoreNews(int count, long timestamp) {
        view.showLoading();
        loadingDisposable = newsRepository
                .retrieveNewsSince(timestamp, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(news -> {
                            view.hideLoading();
                            view.showNews(news);
                        },
                        error -> {
                            Log.e(Project.Tag, error.getMessage());
                            view.showErrorMessage();
                        });
    }


    private NewsRepository newsRepository;
    private Disposable loadingDisposable;

    private FeedView view;
}
