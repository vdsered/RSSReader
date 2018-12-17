package com.companyname.rssreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;

import io.reactivex.disposables.Disposable;

public final class MixedFeedActivity extends AppCompatActivity implements FeedView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mixed_feed_layout);
        feed = findViewById(R.id.feed);
        loading = findViewById(R.id.loading);
        newsAdapter = new NewsAdapter(this);
        feed.setAdapter(newsAdapter);
        final LinearLayoutManager feedLayoutManager =
                new LinearLayoutManager(this);
        feed.setLayoutManager(feedLayoutManager);
        newsRepository =
                new NewsRepository(new RSS2DataSource("https://meduza.io/rss/podcasts/meduza-v-kurse"),
                                   new RSS2DataSource("https://habr.com/rss/hubs/all/"));
        feedPresenter = new FeedPresenter(newsRepository, this);

        feed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = feedLayoutManager.getChildCount();
                int totalItemCount = feedLayoutManager.getItemCount();
                int firstVisibleItemPosition = feedLayoutManager.findFirstVisibleItemPosition();

                if (isLoading)
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
        });

        feedPresenter.initialize(FEED_PAGE_SIZE);
    }

    @Override
    public void showLoading() {
        isLoading = true;
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        isLoading = false;
        loading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNews(News news) {
        newsAdapter.addNews(Arrays.asList(news));
    }

    @Override
    public void showErrorMessage() {
        Toast.makeText(this, R.string.feed_update_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedPresenter.clear();
    }

    private static final int FEED_PAGE_SIZE = 8;
    private boolean isLoading;

    private ProgressBar loading;

    private FeedPresenter feedPresenter;

    private NewsAdapter newsAdapter;
    private RecyclerView feed;

    private Disposable disposable;
    private NewsRepository newsRepository;
}
