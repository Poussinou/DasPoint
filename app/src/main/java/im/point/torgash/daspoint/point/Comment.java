package im.point.torgash.daspoint.point;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
