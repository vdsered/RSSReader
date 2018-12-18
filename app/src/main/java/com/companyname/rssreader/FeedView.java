package com.companyname.rssreader;

import java.util.List;

public interface FeedView {
    void showNews(List<News> news);
    void showErrorMessage();
    void setDefaultNewsImages(List<Logo> logos);
    void showLoading();
    void hideLoading();
}
