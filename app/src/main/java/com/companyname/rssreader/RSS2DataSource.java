package com.companyname.rssreader;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.util.Xml.newPullParser;

public final class RSS2DataSource {
    private final String link;
    public RSS2DataSource(String link) {
        this.link = link;
    }

    public Observable<RSS2Item> retrieveItemsSince(long timestamp, int count) {
        return Observable.create(emitter -> {
            URL url;
            InputStream inputStream = null;
            try
            {
                url = new URL(link);
                inputStream = url.openStream();

                final XmlPullParser parser = newPullParser();
                final String inputEncoding = null;
                parser.setInput(inputStream, inputEncoding);

                final List<RSS2Item> items = readItemElements(parser, count, timestamp);
                for(final RSS2Item item : items)
                    emitter.onNext(item);
            }
            catch (Exception ex)
            {
                emitter.onError(ex);
            }
            finally
            {
                if (inputStream != null) {
                    inputStream.close();
                }
                emitter.onComplete();
            }
        });
    }

    /*
        NOTE: All elements of an item are optional, however at least one of title or
        description must be present. The fact is ignored in the implementation. All of the elements,
        that the method uses, are considered to be required elements.

        See https://validator.w3.org/feed/docs/rss2.html
     */
    private List<RSS2Item> readItemElements(XmlPullParser parser, int count, long timestamp)
            throws IOException, XmlPullParserException {
        final ArrayList<RSS2Item> items = new ArrayList<>(count);

        parser.nextTag();
        boolean isItemElement = false;
        String title = null;
        String description = null;
        String pubDate = null;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            int eventType = parser.getEventType();

            String name = parser.getName();
            if(name == null)
                continue;

            if(eventType == XmlPullParser.END_TAG) {
                if(name.equalsIgnoreCase("item")) {
                    isItemElement = false;
                }
                continue;
            }

            if (eventType == XmlPullParser.START_TAG) {
                if(name.equalsIgnoreCase("item")) {
                    isItemElement = true;
                }
                continue;
            }

            String text = "";
            if (parser.next() == XmlPullParser.TEXT) {
                text = parser.getText();
                parser.nextTag();
            }

            if (name.equalsIgnoreCase("title")) {
                title = text;
            }
            else if (name.equalsIgnoreCase("description")) {
                description = text;
            }
            else if (name.equalsIgnoreCase("pubDate")) {
                pubDate = text;
            }

            if(!isItemElement)
                continue;

            if (title != null && pubDate != null && description != null) {
                final RSS2Item item = new RSS2Item(description, title, pubDate);
                items.add(item);

                title = null;
                pubDate = null;
                description = null;
                isItemElement = false;
            }
        }
        return items;
    }
}
