package com.companyname.rssreader;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import static com.companyname.rssreader.MixedFeedActivity.FEED_PAGE_SIZE;

final class NewsShortageListener extends RecyclerView.OnScrollListener {
    public NewsShortageListener(MixedFeedActivity mixedFeedActivity,
                                LinearLayoutManager feedLayoutManager,
                                FeedPresenter feedPresenter, NewsAdapter newsAdapter) {
        this.mixedFeedActivity = mixedFeedActivity;
        this.feedLayoutManager = feedLayoutManager;
        this.feedPresenter = feedPresenter;
        this.newsAdapter = newsAdapter;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = feedLayoutManager.getChildCount();
        int totalItemCount = feedLayoutManager.getItemCount();
        int firstVisibleItemPosition = feedLayoutManager.findFirstVisibleItemPosition();

        if (mixedFeedActivity.isLoading)
            return;

        final boolean atLeastOneIsVisible =
                firstVisibleItemPosition >= 0;
        final boolean latestItemIsReached =
                (visibleItemCount + firstVisibleItemPosition) >= totalItemCount;
        if (latestItemIsReached &&
            atLeastOneIsVisible &&
            totalItemCount >= FEED_PAGE_SIZE) {
            feedPresenter
                    .onNeedMoreNews(FEED_PAGE_SIZE, newsAdapter.getEarliestTimestamp());
        }
    }

    private MixedFeedActivity mixedFeedActivity;
    private final LinearLayoutManager feedLayoutManager;

    private final FeedPresenter feedPresenter;

    private NewsAdapter newsAdapter;
}
