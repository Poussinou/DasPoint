package im.point.torgash.daspoint.point;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
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

public class ThreadHeaderPost {

    /*
    JSON THREAD EXAMPLE:

    {
     "post": {
          "pinned": false,
          "files": [
               "http://i.point.im/m/A/Alinaki/05/f1/9187/P_20160316_135030.jpg"
          ],
          "tags": [
               "life"
          ],
          "comments_count": 1,
          "author": {
               "login": "Alinaki",
               "id": 156,
               "avatar": "alinaki.jpg?r=4136",
               "name": "Alinaki"
          },
          "text": "Человек-пуэр (пуэрмэн). Способности: сливается с землёй. Оружие: диски прессованного пуэра. Психологические отклонения: заваривает пуэрчик на крови врагов и пьёт из блюдечка.",
          "created": "2016-03-16T14:32:33.112544",
          "type": "post",
          "id": "ipgak",
          "private": false
     },
     "comments": [
          {
               "created": "2016-03-16T15:30:32.745371",
               "text": "норм блин",
               "author": {
                    "login": "pallascat",
                    "id": 537,
                    "avatar": "pallascat.jpg?r=4205",
                    "name": "manul"
               },
               "post_id": "ipgak",
               "to_comment_id": null,
               "is_rec": false,
               "id": 1
          }
     ]
}
     */


    ArrayList<Map<String, String>> postContents;
    //listeners
    OnLinksDetectedListener mOnLinksDetectedListener;

    //message - basic



    //is it recommended?

    //post section

    public String[] tags;
    public int commentsCount;
    //post author details
    public String authorLogin;

    public String authorAvatar;
    public String authorName;

    public String postText;
    Date postCreated;
    public String postCreatedString;

    public String postId;

    public String messageLink;

    public String[] files;
    public ThreadHeaderPost(JSONObject postObject) {
        try {

            JSONObject postAuthor = postObject.getJSONObject("author");


            //tags section
            JSONArray postTagsArray = postObject.getJSONArray("tags");

            if (postTagsArray != null) {
                tags = new String[postTagsArray.length()];
                for (int i = 0; i < postTagsArray.length(); i++) {
                    tags[i] = postTagsArray.get(i).toString();

                }
            }

            commentsCount = Integer.valueOf((postObject.get("comments_count")).toString());
            //author section
            authorLogin = postAuthor.get("login").toString();
            authorAvatar = postAuthor.get("avatar").toString();
            authorName = postAuthor.get("name").toString();



            postText = postObject.get("text").toString();
            if (postObject.has("files")) {
                JSONArray postFiles = postObject.getJSONArray("files");

                if (postFiles != null) {
                    files = new String[postFiles.length()];
                    for (int i = 0; i < postFiles.length(); i++) {
                        files[i] = postFiles.get(i).toString();
                        postText = postText + "\n" + files[i];
                    }
                }
            }

            postId = postObject.get("id").toString();
            messageLink = "http://point.im/" + postId;


            //теперь займемся временем
            String time = postObject.get("created").toString();
            time = time.substring(0, time.length() - 3);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date qdate = new Date();
            try {
                qdate = sdf.parse(time);
            } catch (ParseException e) {
                Log.d("JSON", "Date parsing exception.", e);
            }
            SimpleDateFormat sdfout = new SimpleDateFormat("HH:mm' 'MMM dd, yyyy");
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
            public void handleMessage(Message msg) {
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
                String finish = postText;
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
                            postContents.add(tempContentMap);


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
                    if(url.startsWith("http")) postContents.add(tempContentMapURL);

                    if (begin == null || begin.equals("")) {
                        break;
                    }
                }
                if (begin != null && !begin.equals("")) {
                    Map<String, String> tempContentMap = new HashMap<>();
                    tempContentMap.put("text", finish);
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
