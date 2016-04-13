package im.point.torgash.daspoint.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.network.Commentator;
import im.point.torgash.daspoint.network.Recommender;

/**
 * Created by Boss on 02.04.2016.
 */
public class CommentSection extends RelativeLayout {
    ImageButton qCommentButton;
    ImageButton qRecommendButton;
    EditText etQCommentText;
    TextView tvPostId, tvPostText;
    ImageButton ibClose;
    private String commentId;
    private String postId;
    OnActivityInteractListener mOnActivityInteractListener;
    Context mContext;

    public CommentSection(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_zone, this, true);

    }

    public CommentSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_zone, this, true);
    }


    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        qCommentButton = (ImageButton) this.findViewById(R.id.qcomment_button);
        qRecommendButton = (ImageButton) this.findViewById(R.id.qrecommend_button);
        etQCommentText = (EditText) this.findViewById(R.id.qcomment_text);
        tvPostId = (TextView) this.findViewById(R.id.czId);
        tvPostText = (TextView) this.findViewById(R.id.czQuote);
        ibClose = (ImageButton) this.findViewById(R.id.czClose);

        qCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (etQCommentText.getText().toString().equals("")) {
                    mOnActivityInteractListener.onErrorShow("Не надо пустоты");
                    return;
                }


                mOnActivityInteractListener.onErrorShow("Posting...");
                qCommentButton.setEnabled(false);
                qRecommendButton.setEnabled(false);
                Log.d("DP", "Commenting (comment_id=" + commentId);
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etQCommentText.getWindowToken(), 0);
                etQCommentText.setEnabled(false);
                CommonRequestCallback callback = new CommonRequestCallback() {
                    @Override
                    public void onSuccess(String info) {
                        etQCommentText.setText("");
                        qCommentButton.setEnabled(true);
                        qRecommendButton.setEnabled(true);
                        etQCommentText.setEnabled(true);
                        mOnActivityInteractListener.onErrorShow(info);
                    }

                    @Override
                    public void onError(String error) {
                        qCommentButton.setEnabled(true);
                        qRecommendButton.setEnabled(true);
                        etQCommentText.setEnabled(true);
                        mOnActivityInteractListener.onErrorShow(error);
                    }
                };
                if (null != postId) {

                    if (null == commentId) {

                        new Commentator(postId.startsWith("#")? postId.substring(1) : postId, etQCommentText.getText().toString(), callback).postComment();
                    } else {
                        new Commentator(postId.startsWith("#")? postId.substring(1): postId, commentId, etQCommentText.getText().toString(), callback).postComment();
                    }
                }

            }
        });

        qRecommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                mOnActivityInteractListener.onErrorShow("Recommending...");
                qCommentButton.setEnabled(false);
                qRecommendButton.setEnabled(false);
                Log.d("DP", "Recommending (comment_id=" + commentId);
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etQCommentText.getWindowToken(), 0);
                etQCommentText.setEnabled(false);
                CommonRequestCallback callback = new CommonRequestCallback() {
                    @Override
                    public void onSuccess(String info) {
                        etQCommentText.setText("");
                        qCommentButton.setEnabled(true);
                        qRecommendButton.setEnabled(true);
                        etQCommentText.setEnabled(true);
                        mOnActivityInteractListener.onErrorShow(info);
                    }

                    @Override
                    public void onError(String error) {
                        qCommentButton.setEnabled(true);
                        qRecommendButton.setEnabled(true);
                        etQCommentText.setEnabled(true);
                        mOnActivityInteractListener.onErrorShow(error);
                    }
                };
                if (null != postId) {

                    if (null == commentId) {
                        new Recommender(postId.startsWith("#")? postId.substring(1) : postId, etQCommentText.getText().toString(), callback).postComment();
                    } else {
                        new Recommender(postId.startsWith("#")? postId.substring(1) : postId, commentId, etQCommentText.getText().toString(), callback).postComment();
                    }
                }
            }
        });
        ibClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(GONE);
                setPostId(null);
                setCommentId(null);
                tvPostId.setText("");
                tvPostText.setText("");

            }
        });
    }



    public void setPostId(String postId) {
        this.postId = postId;
        if(null != this.postId){

            this.tvPostId.setText(this.postId);
        }else{
            this.tvPostId.setText("");
        }
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
        if(null != this.commentId){

            this.tvPostId.setText("#" + tvPostId.getText().toString() + "/" + this.commentId);
        }
    }

    public void setOnActivityInteractListener(OnActivityInteractListener listener) {
        mOnActivityInteractListener = listener;
    }

    public void setQuote(String quote) {
        this.tvPostText.setText(quote);
    }
}
