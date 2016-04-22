package im.point.torgash.daspoint.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;

import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.network.Commentator;
import im.point.torgash.daspoint.network.Recommender;

/**
 * Created by Boss on 02.04.2016.
 */
public class PostListCommentButton extends RelativeLayout {
    ImageView ivCommentIcon;
    TextView tvCommentCount;

    Context mContext;

    public PostListCommentButton(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_comments, this, true);

    }

    public PostListCommentButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_comments, this, true);
    }


    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivCommentIcon = (ImageView) this.findViewById(R.id.commentButtonIcon);
        tvCommentCount = (TextView) this.findViewById(R.id.commentButtonText);
    }


    public void setCommentCount(int count) {
        tvCommentCount.setText(String.valueOf(count));
    }
}
