package com.companyname.rssreader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;

import static android.util.Xml.newPullParser;
/*
    TODO
    The implementation is blocking. We need two or more threads to read from more than one different
    sources at the same time. In the current case it's okay, because we have only
    two sources(not that many). However, should Retrofit/Java NIO be used or something like that?
 */
/*
    TODO
    The implementation re-reads the source from the beginning each time it is required to load
    more entries. We could possibly to use offsets to skip the data that are already on board.
    The offset may be sort of a position where another page starts.
 */
public final class RSS2DataSource implements FeedDataSource {

    public RSS2DataSource(@NonNull String link) {
        this.link = link;
        this.host = getHost(link);
    }

    @Override
    public Single<Logo> getLogo() {
        return Single.create(emitter -> {
            URL url;
            InputStream inputStream = null;
            try
            {
                url = new URL(link);
                inputStream = url.openStream();

                final XmlPullParser parser = newPullParser();
                final String inputEncoding = null;
                parser.setInput(inputStream, inputEncoding);

                String elementName;
                while (parser.next() != XmlPullParser.END_DOCUMENT)
                {
                    elementName = parser.getName();
                    if(elementName == null)
                        continue;

                    if(elementName.equalsIgnoreCase("image"))
                        break;
                }
                String logoUrl = "";
                while (parser.next() != XmlPullParser.END_DOCUMENT)
                {
                    elementName = parser.getName();
                    if(elementName == null)
                        continue;

                    if(elementName.equalsIgnoreCase("url")) {
                        parser.next();
                        logoUrl = parser.getText();
                        break;
                    }
                }

                emitter.onSuccess(new Logo(logoUrl, host));
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
            }
        });
    }

    @Override
    public Observable<FeedEntry> retrieveEntriesSince(long timestamp, int count) {
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

                final List<FeedEntry> items = retrieveEntriesSince(parser, count, timestamp);
                for(final FeedEntry item : items)
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
    private List<FeedEntry> retrieveEntriesSince(XmlPullParser parser, int count, long timestamp)
            throws IOException, XmlPullParserException, ParseException {
        final ArrayList<FeedEntry> items = new ArrayList<>(count);

        skipUntilFirstItem(parser);

        boolean isParsingItemSubelement = true;
        String title = null;
        String description = null;
        String pubDate = null;
        String elementName = null;
        do {
            int eventType = parser.getEventType();

            if(eventType == XmlPullParser.END_TAG) {
                if(elementName != null && elementName.equalsIgnoreCase("item")) {
                    isParsingItemSubelement = false;
                }
                continue;
            }

            if (eventType == XmlPullParser.START_TAG) {
                elementName = parser.getName();
                if(elementName != null && elementName.equalsIgnoreCase("item")) {
                    isParsingItemSubelement = true;
                }
                continue;
            }
            String text;
            if (eventType == XmlPullParser.TEXT) {
                text = parser.getText();

                text = text.trim();

                if(text.isEmpty())
                    continue;

                if(elementName.equalsIgnoreCase("title")) {
                    title = text;
                }
                else if (elementName.equalsIgnoreCase("description")) {
                    description = text;
                }
                else if (elementName.equalsIgnoreCase("pubDate")) {
                    pubDate = text;
                }
            }

            if(!isParsingItemSubelement)
                continue;

            if (title != null && pubDate != null && description != null) {

                final long publicationTimestamp = parsePubDate(pubDate);

                /*
                    TODO
                    We need another way to ignore entries that we have already downloaded.
                    For instance, we can use <guid>.
                 */
                if(timestamp <= publicationTimestamp)
                    continue;
                if(count < items.size() + 1)
                    return items;

                final FeedEntry item = new FeedEntry(description, title, publicationTimestamp, host);
                items.add(item);


                title = null;
                pubDate = null;
                description = null;
                isParsingItemSubelement = false;
            }
        } while(parser.next() != XmlPullParser.END_DOCUMENT);
        return items;
    }

    private String getHost(String link) {
        return link.split("://")[1].split(":")[0].split("/")[0];
    }

    private void skipDocumentStart(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.nextTag();
    }

    private void skipUntilFirstItem(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        skipDocumentStart(parser);
        String elementName;
        while (parser.next() != XmlPullParser.END_DOCUMENT)
        {
            elementName = parser.getName();
            if(elementName == null)
                continue;

            if(elementName.equalsIgnoreCase("item"))
                break;
        }
    }

    /*
        NOTE: A pubDate element of RSS 2.0 contains a date represented by RFC 822 by definition.

        See https://validator.w3.org/feed/docs/rss2.html
     */
    private long parsePubDate(String pubDate) throws ParseException {
        return format.parse(pubDate).getTime();
    }
    private final String host;
    private final SimpleDateFormat format =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private final String link;
}
