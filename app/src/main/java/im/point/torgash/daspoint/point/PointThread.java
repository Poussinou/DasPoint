package im.point.torgash.daspoint.point;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by atikhonov on 28.04.2014.
 */
public class PointThread extends PointPost {


    public List<Comment> comments;

    public boolean recommended; //is comming from server?
    public boolean editable; // is comming from server?

    public PointThread(JSONObject threadObject)  {
        super(threadObject);
        try {
            JSONArray commentsArray = threadObject.getJSONArray("comments");
            comments = new ArrayList<>();
            for (int i = 0; i < commentsArray.length(); i++){
                JSONObject commentObject = commentsArray.getJSONObject(i);
                JSONObject commentAuthor = commentObject.getJSONObject("author");
                Comment comment = new Comment();
                comment.author_name = commentAuthor.get("login").toString();

                String time = commentObject.get("created").toString();
                time = time.substring(0, time.length() - 3);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                Date qdate = new Date();
                try {
                    qdate = sdf.parse(time);
                } catch (ParseException e) {
                    Log.d("JSON", "Date parsing exception.", e);
                }
                SimpleDateFormat sdfout = new SimpleDateFormat("HH:mm' 'MMM dd, yyyy");
                comment.created = qdate;
                comment.created_string = sdfout.format(qdate);
                comment.id = commentObject.get("id").toString();
                comment.is_rec = commentObject.getBoolean("is_rec");
                comment.text = commentObject.getString("text");
                comment.to_comment_id = commentObject.get("to_comment_id").toString();
                if (commentObject.has("files")) {
                    JSONArray postFiles = commentObject.getJSONArray("files");

                    if (postFiles != null) {
                        files = new String[postFiles.length()];
                        for (int j = 0; j < postFiles.length(); j++) {
                            files[j] = postFiles.get(j).toString();
                            comment.text = comment.text + "\n" + files[j];
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
