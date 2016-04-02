package im.point.torgash.daspoint.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import im.point.torgash.daspoint.R;

/**
 * Created by Boss on 02.04.2016.
 */
public class CommentSection extends View{

    public CommentSection(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_zone, this, true);
    }

    public void setPostId(String postId) {

    }

    public void setCommentId(String commentId) {

    }

}
