package im.point.torgash.daspoint.point;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.point.torgash.daspoint.listeners.OnLinksDetectedListener;
import im.point.torgash.daspoint.utils.ImageSearchHelper;

public class Comment {
    public String authorName;
    public String authorAvatar;
    public String text;
    public Date created;
    public String createdString;
    public String id;
    public String post_id;
    public String to_comment_id;
    public String[] files;
    ArrayList<Map<String, String>> commentContents;
    public boolean is_rec;

    public Comment(JSONObject jsonComment) {
        try {
            String time = jsonComment.get("created").toString();
            time = time.substring(0, time.length() - 3);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date qdate = new Date();
            try {
                qdate = sdf.parse(time);
            } catch (ParseException e) {
                Log.d("JSON", "Date parsing exception.", e);
            }
            SimpleDateFormat sdfout = new SimpleDateFormat("HH:mm' 'MMM dd, yyyy");
            created = qdate;
            createdString = sdfout.format(qdate);

            text = jsonComment.getString("text");
            JSONObject jsonAuthorSection = jsonComment.getJSONObject("author");
            authorName = jsonAuthorSection.getString("login");
            authorAvatar = jsonAuthorSection.getString("avatar");
            to_comment_id = jsonComment.get("to_comment_id").toString();
            id = jsonComment.get("id").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void searchAndDetectLinks(final OnLinksDetectedListener onLinksDetectedListener) {
        if (null != commentContents) {
            Log.d("DP", "Sendng back an existing post content");
            onLinksDetectedListener.onLinksDetected(commentContents);
            return;
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
                Matcher matcher = pattern.matcher(text);
                String begin = text;
                String finish = text;
                int previousIndex = 0;
                while (matcher.find()) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    String url = matcher.group();
                    boolean urlStartsWithHttp = url.startsWith("http");
                    int index = matcher.start();
                    String start;
                    try{
                        start = begin.substring(previousIndex, index);
                    }catch (Exception e) {
                        Log.d("DP", "Error while parsing string: \n" + begin);
                        e.printStackTrace();

                        break;
                    }
                    Log.d("DP", "PostContents start: " + start);
                    Log.d("DP", "URL in between: " + url);


                    if(!urlStartsWithHttp) start = start + url;
                    if(urlStartsWithHttp) {
                        try {
                            tempContentMap.put("text", start);
                            tempContentMap.put("mime", "text");
                            commentContents.add(tempContentMap);


                        } catch (Exception e) {

                        }
                    }
                    Map<String, String> tempContentMapURL = new HashMap<>();

                    if(urlStartsWithHttp) previousIndex = matcher.end();
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
                    if(url.startsWith("http")) commentContents.add(tempContentMapURL);

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
