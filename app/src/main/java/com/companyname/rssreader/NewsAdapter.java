package com.companyname.rssreader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void setLogos(List<Logo> logos) {
        for(final Logo logo : logos) {
            sourceLogos.put(logo.host, logo.logoURL);
        }
        int position = 0;
        for(final News news : items) {

            if(news.imageURL.isEmpty()) {
                notifyItemChanged(position);
            }
            ++position;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder newsViewHolder, final int position) {
        final News news = items.get(position);

        newsViewHolder.title.setText(news.title);
        newsViewHolder.description.setText(news.description);

        newsViewHolder.description.setVisibility(View.GONE);

        newsViewHolder.itemView.setOnClickListener(v -> {
            final int adapterPosition = newsViewHolder.getAdapterPosition();
            expansionStates.set(adapterPosition, !expansionStates.get(adapterPosition));
            notifyItemChanged(adapterPosition);
            Log.d(Project.Tag,
                    "News was clicked. The title is \"" + news.title +
                            " \". The adapter position is " + adapterPosition);
        });

        final boolean expansionState = expansionStates.get(position);

        newsViewHolder
                .description
                .setVisibility(expansionState ? View.VISIBLE : View.GONE);

        final Drawable drawable = null;
        newsViewHolder.image.setImageDrawable(drawable);

        final String imageUrl;
        if(news.imageURL.isEmpty())
           imageUrl = sourceLogos.get(news.source);
        else
            imageUrl = news.imageURL;
        Glide.with(newsViewHolder.image.getContext())
                .load(imageUrl)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(newsViewHolder.image);

        newsViewHolder.source.setText(news.source);
        final String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", news.timestamp).toString();
        newsViewHolder.date.setText(date);
    }

    public void addNews(List<News> news)
    {
        final int startPosition = items.size();
        final int count = news.size();
        items.addAll(startPosition, news);
        expansionStates.addAll(startPosition, Collections.nCopies(count, false));
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
        public final TextView source;
        public final TextView title;
        public final TextView description;
        public final TextView date;
        public final ImageView image;
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            source = itemView.findViewById(R.id.source);
            date = itemView.findViewById(R.id.date);
            description = itemView.findViewById(R.id.description);
        }
    }

    private Map<String, String> sourceLogos = new HashMap<>();
    private LayoutInflater inflater;
    private final List<News> items = new ArrayList<>();
    private final List<Boolean> expansionStates = new ArrayList<>();
}
