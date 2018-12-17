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
        loadMoreNews(initialFeedEntryCount, System.currentTimeMillis());
    }

    public void clear() {
        view = null;
        disposable.dispose();
        disposable = null;
    }

    public void onNeedMoreNews(int count, long timestamp) {
        if(disposable.isDisposed())
            disposable.dispose();
        loadMoreNews(count, timestamp);
    }

    private void loadMoreNews(int count, long timestamp) {
        view.showLoading();
        disposable = newsRepository
                .retrieveNewsSince(timestamp, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(news -> view.showNews(news),
                        error -> {
                            Log.e(Project.Tag, error.getMessage());
                            view.showErrorMessage();
                        }, () -> view.hideLoading());
    }

    private NewsRepository newsRepository;
    private Disposable disposable;

    private FeedView view;
}
