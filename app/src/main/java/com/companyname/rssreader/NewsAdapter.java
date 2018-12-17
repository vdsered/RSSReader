package com.companyname.rssreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    NewsAdapter(@NonNull Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        final boolean attachToRoot = false;
        final View feedItemView =
                inflater.inflate(R.layout.feed_item_layout, viewGroup, attachToRoot);
        return new NewsViewHolder(feedItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder newsViewHolder, int position) {
        final News news = items.get(position);

        newsViewHolder.title.setText(news.title);
        newsViewHolder.description.setText(news.description);
    }

    public void addNews(List<News> news)
    {
        final int startPosition = items.size();
        final int count = news.size();
        items.addAll(startPosition, news);
        notifyItemRangeInserted(startPosition, count);
    }

    public long getEarliestTimestamp()
    {
        return items.get(items.size() - 1).timestamp;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView description;
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }

    private LayoutInflater inflater;
    private final List<News> items = new ArrayList<>();
}
