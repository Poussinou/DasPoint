package im.point.torgash.daspoint.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.point.torgash.daspoint.listeners.OnLinksDetectedListener;

/**
 * Created by Boss on 29.02.2016.
 */
public class LinksDetector {
    public static Map<String, ArrayList<Map<String, String>>> commentCache;
    static ArrayList<Map<String, String>> commentContents;

    static String mCommentText;

    public static void searchAndDetectLinks(String commentText, final OnLinksDetectedListener onLinksDetectedListener) {
        commentContents = new ArrayList<>();
        if (commentCache == null) {
            commentCache = new HashMap<>();
        }
        mCommentText = commentText;
        if (commentCache.containsKey(mCommentText)) {
            commentContents = commentCache.get(commentText);
            if (null != commentContents) {

                Log.d("DP", "Sendng back an existing comment_black content");
                onLinksDetectedListener.onLinksDetected(commentContents);
                return;
            }

        }

        final Handler h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        Log.d("DP", "sending message from searcher");
                        if (null != onLinksDetectedListener) {
                            onLinksDetectedListener.onLinksDetected((ArrayList<Map<String, String>>) msg.obj);
                        }
                        break;

                }
            }
        };
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                commentContents = new ArrayList<>();
                Pattern pattern = Patterns.WEB_URL;
                Matcher matcher = pattern.matcher(mCommentText);
                String begin = mCommentText;
                String finish = mCommentText;
                int previousIndex = 0;
                while (matcher.find()) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    String url = matcher.group();
                    boolean urlStartsWithHttp = url.startsWith("http");
                    int index = matcher.start();
                    String start;
                    try {
                        start = begin.substring(previousIndex, index);
                    } catch (Exception e) {
                        Log.d("DP", "Error while parsing string: \n" + begin);
                        e.printStackTrace();

                        break;
                    }
                    Log.d("DP", "PostContents start: " + start);
                    Log.d("DP", "URL in between: " + url);


                    if (!urlStartsWithHttp) start = start + url;
                    if (urlStartsWithHttp) {
                        try {
                            tempContentMap.put("text", start);
                            tempContentMap.put("mime", "text");
                            commentContents.add(tempContentMap);


                        } catch (Exception e) {

                        }
                    }
                    Map<String, String> tempContentMapURL = new HashMap<>();

                    if (urlStartsWithHttp) previousIndex = matcher.end();
                    finish = begin.substring(previousIndex);


                    String mime = ImageSearchHelper.checkImageLink(url);
                    Log.d("DP", "Found mime: " + mime);
                    if (mime != null) {
                        if (mime.contains("text")) {
                            //make a Webpage view
                            tempContentMapURL.put("mime", "webpage");
                            String title;
                            try {
                                Document doc = Jsoup.connect(url).get();
                                title = doc.title().trim();
                                Log.d("DP", "Webpage title parsed: \n" + title);

                            } catch (IOException e) {
                                e.printStackTrace();
                                title = url;
                            }

                            tempContentMapURL.put("text", title);
                            tempContentMapURL.put("url", url);

                        } else if (mime.contains("image")) {
                            //make an Image view
                            tempContentMapURL.put("mime", "image");
                            tempContentMapURL.put("text", url);

                        } else {
                            tempContentMapURL.put("mime", "webpage");
                            tempContentMapURL.put("text", url);
                            tempContentMapURL.put("url", url);
                        }

                    } else {
                        tempContentMapURL.put("mime", "webpage");
                        tempContentMapURL.put("text", url);
                        tempContentMapURL.put("url", url);
                    }
                    if (url.startsWith("http")) commentContents.add(tempContentMapURL);

                    if (begin == null || begin.equals("")) {
                        break;
                    }
                }
                if (begin != null && !begin.equals("")) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    tempContentMap.put("text", finish);
                    tempContentMap.put("mime", "text");
                    commentContents.add(tempContentMap);
                }
                Message msg = h.obtainMessage(1, commentContents);
                h.sendMessage(msg);
            }
        });
        t.start();

    }
}
