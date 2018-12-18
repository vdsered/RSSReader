package com.companyname.rssreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

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
        final LinearLayoutManager feedLayoutManager = new LinearLayoutManager(this);
        feed.setLayoutManager(feedLayoutManager);
        final DividerItemDecoration decoration =
                new DividerItemDecoration(feed.getContext(), DividerItemDecoration.VERTICAL);
        feed.addItemDecoration(decoration);
        final RSS2DataSource habr =
                new RSS2DataSource("https://habr.com/rss/hubs/all/");
        final RSS2DataSource meduza =
                new RSS2DataSource("https://meduza.io/rss/podcasts/meduza-v-kurse");
        newsRepository = new NewsRepository(meduza, habr);
        feedPresenter = new FeedPresenter(newsRepository, this);

        final NewsShortageListener shortageListener =
                new NewsShortageListener(this, feedLayoutManager,
                                        feedPresenter, newsAdapter);
        feed.addOnScrollListener(shortageListener);

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
    public void showNews(List<News> news) {
        newsAdapter.addNews(news);
    }

    @Override
    public void showErrorMessage() {
        Toast.makeText(this, R.string.feed_update_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setDefaultNewsImages(List<Logo> logos) {
        newsAdapter.setLogos(logos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedPresenter.clear();
    }

    static final int FEED_PAGE_SIZE = 8;
    boolean isLoading;

    private ProgressBar loading;

    private FeedPresenter feedPresenter;

    private NewsAdapter newsAdapter;
    private RecyclerView feed;

    private Disposable disposable;
    private NewsRepository newsRepository;

}
