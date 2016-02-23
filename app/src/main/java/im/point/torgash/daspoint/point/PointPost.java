package im.point.torgash.daspoint.point;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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

public class PointPost {
    ArrayList<Map<String, String>> postContents;
    //listeners
    OnLinksDetectedListener mOnLinksDetectedListener;

    //message - basic

    public int uid;
    public boolean subscribed;
    public boolean editable;

    //is it recommended?
    boolean recommended;
    public boolean isRecommended = false;
    //recommendation section
    public String recText;
    public String recCommentId;
    public String recAuthorLogin;
    public int recAuthorId;
    public String recAuthorAvatar;
    public String recAuthorName;
    public String authorAka = "";

    //post section

    public String[] tags;
    public int commentsCount;
    //post author details
    public String authorLogin;
    public int authorId;
    public String authorAvatar;
    public String authorName;

    public String postText;
    Date postCreated;
    public String postCreatedString;
    public String postType;
    public String postId;
    boolean isPrivate;
    public String messageLink;
    public String commentId;

    public PointPost(JSONObject postObject) {
        try {
            JSONObject postDetails = postObject.getJSONObject("post");
            JSONObject postAuthor = postDetails.getJSONObject("author");

            if (postObject.has("rec")) {
                isRecommended = true;
            }
            uid = Integer.valueOf((postObject.get("uid")).toString());
            subscribed = Boolean.valueOf((postObject.get("subscribed")).toString());
            editable = Boolean.valueOf((postObject.get("editable")).toString());
            recommended = Boolean.valueOf((postObject.get("recommended")).toString());
            if (postObject.has("comment_id") && !postObject.get("comment_id").toString().equals("null")) {
                Log.d("DP", "Comment_id: " + postObject.getString("comment_id") + " " + postObject.get("comment_id").toString());
                commentId = postObject.getString("comment_id");
            } else commentId = "null";
            if (isRecommended) {
                JSONObject postRecommendationSection = postObject.getJSONObject("rec");
                recText = postRecommendationSection.get("text").toString();
                if (recText.equals("null")) {
                    recText = null;
                }
                recCommentId = postRecommendationSection.get("comment_id").toString();
                JSONObject recAuthor = postRecommendationSection.getJSONObject("author");
                recAuthorLogin = recAuthor.get("login").toString();
                recAuthorLogin = "@" + recAuthorLogin;
                recAuthorAvatar = recAuthor.get("avatar").toString();
                recAuthorName = recAuthor.get("name").toString();
            } else {
                recAuthorLogin = "";
                recText = "";
                recAuthorAvatar = "";
                recAuthorName = "";
            }
            //tags section
            JSONArray postTagsArray = postDetails.getJSONArray("tags");

            if (postTagsArray != null) {
                tags = new String[postTagsArray.length()];
                for (int i = 0; i < postTagsArray.length(); i++) {
                    tags[i] = postTagsArray.get(i).toString();

                }
            }

            commentsCount = Integer.valueOf((postDetails.get("comments_count")).toString());
            //author section
            authorLogin = postAuthor.get("login").toString();
            authorAvatar = postAuthor.get("avatar").toString();
            authorName = postAuthor.get("name").toString();

            if (!(authorName.equals(authorLogin)) && authorName.length() > 0) {
                authorAka = " aka ";

            } else {
                authorName = "";
                authorAka = "";
            }


            postText = postDetails.get("text").toString();

            postType = postDetails.get("type").toString();

            postId = postDetails.get("id").toString();
            messageLink = "http://point.im/" + postId;


            isPrivate = Boolean.valueOf(postDetails.get("private").toString());

            //теперь займемся временем
            String time = postDetails.get("created").toString();
            time = time.substring(0, time.length() - 3);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date qdate = new Date();
            try {
                qdate = sdf.parse(time);
            } catch (ParseException e) {
                Log.d("JSON", "Date parsing exception.", e);
            }
            SimpleDateFormat sdfout = new SimpleDateFormat("yyyy-MM-dd' 'HH.mm.ss");
            postCreated = qdate;
            postCreatedString = sdfout.format(qdate);


        } catch (Exception e) {
            Log.d("JSON", "Failed to parse JSON to PointPost: " + e);
        }

    }

    @Override
    public String toString() {
        return "@" + authorLogin + ": \n" + postText + "\n" + "comments: " + commentsCount;
    }

    public void setOnLinksDetectedListener(OnLinksDetectedListener onLinksDetectedListener) {
        mOnLinksDetectedListener = onLinksDetectedListener;
    }

    public void searchAndDetectLinks(final OnLinksDetectedListener onLinksDetectedListener) {
        if (null != postContents) {
            Log.d("DP", "Sendng back an existing post content");
            onLinksDetectedListener.onLinksDetected(postContents);
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
                postContents = new ArrayList<>();
                Pattern pattern = Patterns.WEB_URL;
                Matcher matcher = pattern.matcher(postText);
                String begin = postText;

                while (matcher.find()) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    String url = matcher.group();
                    int index = matcher.start();
                    String start;
                    try{
                        start = begin.substring(0, index);
                    }catch (Exception e) {
                        Log.d("DP", "Error while parsing string: \n" + begin);
                        e.printStackTrace();

                        break;
                    }
                    Log.d("DP", "PostContents start: " + start);
                    Log.d("DP", "URL in between: " + url);
                    StringBuffer s = new StringBuffer();
                    matcher.appendTail(s);
                    begin = s.toString().substring(matcher.end());
                    Log.d("DP", "PostContents end: " + begin);
                    try {
                        tempContentMap.put("text", start);
                        tempContentMap.put("mime", "text");
                        postContents.add(tempContentMap);


                    } catch (Exception e) {
                        begin = null;
                    }
                    Map<String, String> tempContentMapURL = new HashMap<>();


                    String mime = ImageSearchHelper.checkImageLink(url);
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

                        } else if (mime.contains("image")) {
                            //make an Image view
                            tempContentMapURL.put("mime", "image");
                            tempContentMapURL.put("text", url);
                        }

                    } else {
                        tempContentMapURL.put("mime", "null");
                        tempContentMapURL.put("text", url);
                    }
                    postContents.add(tempContentMapURL);

                    if (begin == null || begin.equals("")) {
                        break;
                    }
                }
                if (begin != null && !begin.equals("")) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    tempContentMap.put("text", begin);
                    tempContentMap.put("mime", "text");
                    postContents.add(tempContentMap);
                }
                Message msg = h.obtainMessage(1, postContents);
                h.sendMessage(msg);
            }
        });
        t.start();

    }
}
