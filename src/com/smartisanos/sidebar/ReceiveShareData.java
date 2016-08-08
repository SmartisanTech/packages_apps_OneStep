package com.smartisanos.sidebar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.smartisanos.sidebar.util.BookmarkManager;
import com.smartisanos.sidebar.util.LOG;

public class ReceiveShareData extends Activity {
    private static final LOG log = LOG.getInstance(ReceiveShareData.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return ;
        }
        String type = intent.getType();
        if (type == null) {
            return;
        }
        boolean isActionMatched = false;
        log.error("action ["+action+"], type ["+type+"]");
        String callingPkg = getCallingPackage();
        if (Intent.ACTION_SEND.equals(action)) {
            isActionMatched = true;
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            isActionMatched = true;
        }
        if (isActionMatched) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            log.error(sharedText);
            BookmarkManager.BookmarkItem item = parse(sharedText, callingPkg);
            if (item != null) {
                BookmarkManager.getInstance(this).addBookmark(item);
            } else {
                log.error("save bookmark failed by data parse err");
            }
        }
        finish();
    }

    private BookmarkManager.BookmarkItem parse(String text, String source) {
        BookmarkManager.BookmarkItem item = null;
        if (text == null) {
            return item;
        }
        try {
            Spannable.Factory instance = Spannable.Factory.getInstance();
            Spannable sp = instance.newSpannable(text);
            Linkify.addLinks(sp, Linkify.WEB_URLS);
            URLSpan[] urls = sp.getSpans(0, sp.length(), URLSpan.class);
            if (urls != null && urls.length > 0) {
                item = new BookmarkManager.BookmarkItem();
                item.fullText = text;
                URLSpan url = urls[0];
                if (url != null) {
                    int start = sp.getSpanStart(url);
                    int end = sp.getSpanEnd(url);
                    String urlStr = url.getURL();
                    item.content_uri = urlStr;
                    String title = text.substring(0, start);
                    if (title == null || title.trim().length() == 0) {
                        //hoops
                        title = text.substring(end);
                    }
                    item.title = title;
                    item.source = source;
                    item.time = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            item = null;
            e.printStackTrace();
        }
        if (item != null) {
            //verify title
            if (item.title == null || item.title.trim().length() == 0) {
                item = null;
            }
        }
        return item;
    }
}