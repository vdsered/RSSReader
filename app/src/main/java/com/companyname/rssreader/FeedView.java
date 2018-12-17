package com.companyname.rssreader;

public interface FeedView {
    void showNews(News news);
    void showErrorMessage();
    void showLoading();
    void hideLoading();
}
