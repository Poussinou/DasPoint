package im.point.torgash.daspoint;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PointRecent {
    //message - basic
    int uid;
    boolean subscribed;
    boolean editable;

    //is it recommended?
    boolean recommended;
    boolean isRecommended = false;
    //recommendation section
    String recText;
    int recCommentId;
    String recAuthorLogin;
    int recAuthorId;
    String recAuthorAvatar;
    String recAuthorName;
    String authorAka = "";
    //post section

    String[] tags;
    int commentsCount;
    //post author details
    String authorLogin;
    int authorId;
    String authorAvatar;
    String authorName;

    String postText;
    Date postCreated;
    String postCreatedString;
    String postType;
    String postId;
    boolean isPrivate;
    String messageLink;

    PointRecent(JSONObject postObject) {
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
            if (isRecommended) {
                JSONObject postRecommendationSection = postObject.getJSONObject("rec");
                recText = postRecommendationSection.get("text").toString();
                if (recText.equals("null")) {
                    recText = "";
                }
//				if(postRecommendationSection.get("comment_id") == null){
//					recCommentId = 0;
//				}else recCommentId = Integer.valueOf((postRecommendationSection.get("comment_id")).toString());
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

            authorLogin = "@" + authorLogin;
            postText = postDetails.get("text").toString();

            postType = postDetails.get("type").toString();

            postId = postDetails.get("id").toString();
            messageLink = "http://point.im/" + postId;
            postId = "#" + postId;

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
            Log.d("JSON", "Failed to parse JSON to PointRecent: " + e);
        }

    }
    @Override
    public String toString(){
        return "@" + authorLogin + ": \n" + postText + "\n" + "comments: " + commentsCount;
    }

}
