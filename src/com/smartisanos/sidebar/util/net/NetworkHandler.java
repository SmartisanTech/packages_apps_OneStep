package com.smartisanos.sidebar.util.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.smartisanos.sidebar.SidebarApplication;
import com.smartisanos.sidebar.util.BookmarkManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NetworkHandler {
    private static final LOG log = LOG.getInstance(NetworkHandler.class);

    private static final HandlerThread sWorkerThread = new HandlerThread("NetworkHandler");

    static {
        sWorkerThread.start();
    }

    public static final int ACTION_LOAD_BOOKMARK_TITLE = 1001;

    public static void postTask(int action, List params) {
        Message msg = mWorker.obtainMessage();
        msg.what = action;
        msg.obj = params;
        mWorker.sendMessage(msg);
    }

    private static final Handler mWorker = new Handler(sWorkerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            List params = (List) msg.obj;
            handleAction(action, params);
        }
    };

    private static void handleAction(int action, List params) {
        switch (action) {
            case ACTION_LOAD_BOOKMARK_TITLE : {
                handleACTION_LOAD_BOOKMARK_TITLE(params);
                break;
            }
            default:
                break;
        }
    }

    private static void handleACTION_LOAD_BOOKMARK_TITLE(List params) {
        if (!Utils.isNetworkConnected(SidebarApplication.getInstance())) {
            log.error("isNetworkConnected false, ["+SidebarApplication.getInstance()+"]");
            return;
        }
        if (params == null || params.size() == 0) {
            return;
        }
        BookmarkManager.BookmarkItem item = null;
        String uri = null;
        long id = -1;
        try {
            item = (BookmarkManager.BookmarkItem) params.get(0);
            if (item == null) {
                return;
            }
            uri = item.content_uri;
            id = item.id;
        } catch (Exception e) {}
        if (uri == null || id == -1) {
            return;
        }
        uri = uri.trim();
        URL url = null;
        try {
            url = new URL(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (url == null) {
            return;
        }
        int buffer_size = 5000;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            String attr_range = "Accept-Ranges";
            String attr_range_value = "bytes=0-" + buffer_size;
            if (uri.startsWith("https:")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty(attr_range, attr_range_value);
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[1024];
                int count = -1;
                while ((count = in.read(buffer)) > 0) {
                    baos.write(buffer, 0, count);
                }
                in.close();
                connection.disconnect();
            } else if (uri.startsWith("http:")) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty(attr_range, attr_range_value);
                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[1024];
                int count = -1;
                while ((count = in.read(buffer)) > 0) {
                    baos.write(buffer, 0, count);
                }
                in.close();
                connection.disconnect();
            } else {
                log.error("unknown type uri ["+uri+"]");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = new String(baos.toByteArray());
        content = content.trim();
        if (content.length() == 0) {
            log.error("handleACTION_LOAD_BOOKMARK_TITLE empty title !");
            return;
        }
        String title = Utils.parseTitle(content);
        if (title != null) {
            item.title = title;
            log.error("parseTitle ["+item.title+"], content length ["+content.length()+"]");
            BookmarkManager.getInstance(SidebarApplication.getInstance()).updateBookmark(item);
        }
    }
}