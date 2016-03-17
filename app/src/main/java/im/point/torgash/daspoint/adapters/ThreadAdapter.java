package im.point.torgash.daspoint.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.point.torgash.daspoint.ImageViewFullscreenActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnErrorShowInSnackbarListener;
import im.point.torgash.daspoint.listeners.OnLinksDetectedListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.network.Commentator;
import im.point.torgash.daspoint.network.Recommender;
import im.point.torgash.daspoint.point.Comment;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PointThread;
import im.point.torgash.daspoint.point.PostList;
import im.point.torgash.daspoint.point.ThreadHeaderPost;


public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ThreadHeaderPost mPost;
    private List<Comment> mComments;
    private Context mContext;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = -1;
    boolean qRecSectionVisible = false;
    int commentsToDisplay = 10;
    private PointThread mThread = null;
    //    private ImageSearchTask mTask;

    private OnPostClickListener mOnPostClickListener = null;

    OnErrorShowInSnackbarListener mOnErrorShowInSnackbarListener;

    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnPostClickListener != null)
                mOnPostClickListener.onTagClicked(view, ((TextView) view).getText().toString());
        }
    };
    private boolean mHasHeader = false;

    @Override
    public long getItemId(int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOOTER:
                return -1;
            case TYPE_HEADER:
                return 0;
            default:
                if (mHasHeader)
                    return Integer.valueOf(mComments.get(position - 1).id);
                else
                    return Integer.valueOf(mComments.get(position).id);
        }
    }

    public ThreadAdapter(Context context) {
        super();
        setHasStableIds(true);
        mContext = context;
    }

    protected void setHasHeader(boolean hasHeader) {
        mHasHeader = hasHeader;
    }

    public List<Comment> getCommentsList() {
        return mComments;
    }

    public void setData(Context context, PointThread thread) {
        mThread = thread;
        mPost = thread.post;
        mComments = thread.comments;
//        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
//            mTask.cancel(true);
//        }
//        mTask = new ImageSearchTask(context);
//        mTask.execute(mPostList);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mComments.size() + (mHasHeader ? 1 : 0))
            return TYPE_FOOTER;
        if (position == 0 && mHasHeader)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup viewGroup) {
        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_footer, viewGroup, false);
        return new FooterHolder(v);
    }

    public RecyclerView.ViewHolder onCreateHeaderViewHolder(final ViewGroup viewGroup) {
        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.thread_header, viewGroup, false);
        return new HeaderViewHolder(v);
    }

    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup) {

        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.post_list_item, viewGroup, false);
        final CommentViewHolder holder = new CommentViewHolder(v);
//        holder.webLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, (Uri) view.getTag());
//                holder.itemView.getContext().startActivity(Intent.createChooser(browserIntent, holder.itemView.getContext().getString(R.string.title_choose_app)));
//            }
//        });
//        holder.avatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String user = (String) view.getTag();
//                if (!TextUtils.isEmpty(user)) {
//                    Intent intent = new Intent(view.getContext(), UserViewActivity.class);
//                    intent.putExtra(UserViewActivity.EXTRA_USER, user);
//                    ActivityCompat.startActivity((Activity) view.getContext(), intent, null);
//                }
//            }
//        });
//        holder.favourite.setOnClickListener(new BookmarkToggleListener());
//        holder.recomender_avatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String user = (String) view.getTag();
//                if (!TextUtils.isEmpty(user)) {
//                    Intent intent = new Intent(view.getContext(), UserViewActivity.class);
//                    intent.putExtra(UserViewActivity.EXTRA_USER, user);
//                    ActivityCompat.startActivity((Activity) view.getContext(), intent, null);
//                }
//            }
//        });
//        v.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mOnPostClickListener != null) {
//                    mOnPostClickListener.onPostClicked(v, view.getTag(R.id.post_id).toString());
//                }
//            }
//        });
        return holder;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewTYpe) {
        if (viewTYpe == TYPE_FOOTER) {
            return onCreateFooterViewHolder(viewGroup);
        } else if (viewTYpe == TYPE_HEADER) {
            return onCreateHeaderViewHolder(viewGroup);
        } else {
            return onCreateItemViewHolder(viewGroup);
        }
    }

    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        FooterHolder footerHolder = (FooterHolder) holder;

    }

    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        final LayoutInflater li = (LayoutInflater) headerHolder.itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Change it to my layout
        //holder.imageList.setImageUrls(post.post.text.images, post.post.files);
        headerHolder.llPostContent.removeAllViews();
        View tv = li.inflate(R.layout.text_view, null);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerHolder.llPostContent.addView(tv);
        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
        tView.setText(mPost.postText);

        OnLinksDetectedListener linksDetectedListener = new OnLinksDetectedListener() {
            @Override
            public void onLinksDetected(ArrayList<Map<String,String>> postContents) {
                headerHolder.llPostContent.removeAllViews();
                for(Map<String, String> m : postContents) {
                    String mime = m.get("mime");
                    if(mime.equals("image")){
                        View iv = li.inflate(R.layout.post_image_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        headerHolder.llPostContent.addView(iv);
                        ImageView ivPostImageView = (ImageView)iv.findViewById(R.id.post_image_view);
                        ImageLoader.getInstance().displayImage(m.get("text"), ivPostImageView);

                        final String url = m.get("text");
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!url.contains(".gif")) {
                                    Intent intent = new Intent(mContext, ImageViewFullscreenActivity.class);
                                    intent.putExtra("url", url);
                                    mOnErrorShowInSnackbarListener.onIntentStart(intent);

                                }
                                else {
                                    mOnErrorShowInSnackbarListener.onErrorShow("Поддержка GIF еще не запилена.");
                                }
                            }
                        });
                        Log.d("DP", "Created imageview");
                    }
                    if(mime.equals("text") && !m.get("text").trim().equals("")) {
                        View tv = li.inflate(R.layout.text_view, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        headerHolder.llPostContent.addView(tv);
                        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
                        tView.setText(m.get("text").trim());
                        Log.d("DP", "Created textview with text: \n " + m.get("text"));
                    }
                    if(mime.equals("webpage")){
                        View tv = li.inflate(R.layout.webpage_link, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        headerHolder.llPostContent.addView(tv);
                        TextView tView = (TextView) tv.findViewById(R.id.tv_webpage_title);
                        tView.setText(m.get("text"));
                        Log.d("DP", "Created webpage view with text: \n " + m.get("text"));
                        Button btnLink = (Button) tv.findViewById(R.id.webpage_button);
                        final String url = m.get("url");
                        btnLink.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnErrorShowInSnackbarListener.onIntentStart(intent);
                            }
                        });
                    }

                }
            }
        };
        mPost.searchAndDetectLinks(linksDetectedListener);

        headerHolder.author.setText("@" + mPost.authorLogin);

        headerHolder.itemView.setTag(R.id.post_id, mPost.postId);
        Log.d("DP", "Invading viewholder with post \n" + mPost);
        ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + mPost.authorAvatar, headerHolder.avatar);

        headerHolder.date.setText(mPost.postCreatedString);

        headerHolder.post_id.setTag(mPost.postId);
//        holder.webLink.setTag(post.messageLink);
//        holder.favourite.setChecked(post.bookmarked);
//        holder.favourite.setTag(post.post.id);


        headerHolder.btnShowAll.setText("Show all (" + String.valueOf(mPost.commentsCount) + ")");
        // holder.comments.setVisibility(View.GONE);

        headerHolder.tags.removeAllViews();
        if (!mPost.commentId.equals("null") || mPost.tags == null || mPost.tags.length == 0) {
           headerHolder.tags.setVisibility(View.GONE);
        } else {
            headerHolder.tags.setVisibility(View.VISIBLE);

            int n = 0;
            for (String tag : mPost.tags) {
                final TextView v = (TextView) li.inflate(R.layout.tag, headerHolder.tags, false);
                v.setText(tag);
                headerHolder.tags.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                v.setOnClickListener(mOnTagClickListener);
            }
        }
        ;

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)

        qRecSectionVisible = false;
        final ImageButton qCommentButton = (ImageButton) headerHolder.qCommentSection.findViewById(R.id.qcomment_button);
        final ImageButton qRecommendButton = (ImageButton) headerHolder.qCommentSection.findViewById(R.id.qrecommend_button);
        final EditText etQCommentText = (EditText) headerHolder.qCommentSection.findViewById(R.id.qcomment_text);
        final Button btnQcommentToggle = (Button) headerHolder.qCommentSection.findViewById(R.id.qcomment_toggle);
        headerHolder.qCommentSection.setVisibility(View.GONE);
        btnQcommentToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                headerHolder.qCommentSection.setVisibility(qRecSectionVisible? View.GONE: View.VISIBLE);
                qRecSectionVisible = qRecSectionVisible? false: true;
                if(qRecSectionVisible){
                    qCommentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (etQCommentText.getText().toString().equals("")) {
                                mOnErrorShowInSnackbarListener.onErrorShow("Не надо пустоты");
                                return;
                            }


                            mOnErrorShowInSnackbarListener.onErrorShow("Posting...");
                            qCommentButton.setEnabled(false);
                            qRecommendButton.setEnabled(false);
                            Log.d("DP", "Commenting (comment_id=" + mPost.commentId);
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
                                    mOnErrorShowInSnackbarListener.onErrorShow(info);
                                }

                                @Override
                                public void onError(String error) {
                                    qCommentButton.setEnabled(true);
                                    qRecommendButton.setEnabled(true);
                                    etQCommentText.setEnabled(true);
                                    mOnErrorShowInSnackbarListener.onErrorShow(error);
                                }
                            };
                            new Commentator(mPost.postId, etQCommentText.getText().toString(), callback).postComment();


                        }
                    });

                    qRecommendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            mOnErrorShowInSnackbarListener.onErrorShow("Recommending...");
                            qCommentButton.setEnabled(false);
                            qRecommendButton.setEnabled(false);

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
                                    mOnErrorShowInSnackbarListener.onErrorShow(info);
                                }

                                @Override
                                public void onError(String error) {
                                    qCommentButton.setEnabled(true);
                                    qRecommendButton.setEnabled(true);
                                    etQCommentText.setEnabled(true);
                                    mOnErrorShowInSnackbarListener.onErrorShow(error);
                                }
                            };
                            new Recommender(mPost.postId, etQCommentText.getText().toString(), callback).postComment();


                        }
                    });
                }
            }
        });








    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        int type = getItemViewType(i);
        if (type == TYPE_FOOTER) {
            onBindFooterViewHolder(holder);
        } else if (type == TYPE_HEADER) {
            onBindHeaderViewHolder(holder);
        } else {
            onBindItemViewHolder((CommentViewHolder) holder, mHasHeader ? (i - 1) : i);
        }
    }

    public void onBindItemViewHolder(final CommentViewHolder holder, int i) {
        final LayoutInflater li = (LayoutInflater) holder.itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Comment comment = mComments.get(i);
        //Change it to my layout
        //holder.imageList.setImageUrls(thread.thread.text.images, thread.thread.files);
        holder.llCommentContent.removeAllViews();
        View tv = li.inflate(R.layout.text_view, null);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.llCommentContent.addView(tv);
        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
        tView.setText(comment.text);

        OnLinksDetectedListener linksDetectedListener = new OnLinksDetectedListener() {
            @Override
            public void onLinksDetected(ArrayList<Map<String, String>> commentContents) {
                holder.llCommentContent.removeAllViews();
                for (Map<String, String> m : commentContents) {
                    String mime = m.get("mime");
                    if (mime.equals("image")) {
                        View iv = li.inflate(R.layout.post_image_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llCommentContent.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ImageLoader.getInstance().displayImage(m.get("text"), ivPostImageView);

                        final String url = m.get("text");
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!url.contains(".gif")) {
                                    Intent intent = new Intent(mContext, ImageViewFullscreenActivity.class);
                                    intent.putExtra("url", url);
                                    mOnErrorShowInSnackbarListener.onIntentStart(intent);

                                } else {
                                    mOnErrorShowInSnackbarListener.onErrorShow("Поддержка GIF еще не запилена.");
                                }
                            }
                        });
                        Log.d("DP", "Created imageview");
                    }
                    if (mime.equals("text") && !m.get("text").trim().equals("")) {
                        View tv = li.inflate(R.layout.text_view, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llCommentContent.addView(tv);
                        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
                        tView.setText(m.get("text").trim());
                        Log.d("DP", "Created textview with text: \n " + m.get("text"));
                    }
                    if (mime.equals("webpage")) {
                        View tv = li.inflate(R.layout.webpage_link, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llCommentContent.addView(tv);
                        TextView tView = (TextView) tv.findViewById(R.id.tv_webpage_title);
                        tView.setText(m.get("text"));
                        Log.d("DP", "Created webpage view with text: \n " + m.get("text"));
                        Button btnLink = (Button) tv.findViewById(R.id.webpage_button);
                        final String url = m.get("url");
                        btnLink.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnErrorShowInSnackbarListener.onIntentStart(intent);
                            }
                        });
                    }

                }
            }
        };
        Comment.searchAndDetectLinks(linksDetectedListener);

        holder.author.setText("@" + mThread.authorLogin);

        holder.itemView.setTag(R.id.post_id, mThread.postId);
        Log.d("DP", "Invading viewholder with thread \n" + mThread);
        ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + mThread.authorAvatar, holder.avatar);

        holder.date.setText(mThread.postCreatedString);
        holder.post_id.setTag(mThread.postId);

        qRecSectionVisible = false;
        final ImageButton qCommentButton = (ImageButton) holder.qRecSection.findViewById(R.id.qcomment_button);
        final ImageButton qRecommendButton = (ImageButton) holder.qRecSection.findViewById(R.id.qrecommend_button);
        final EditText etQCommentText = (EditText) holder.qRecSection.findViewById(R.id.qcomment_text);
        final Button btnQcommentToggle = (Button) holder.mView.findViewById(R.id.qcomment_toggle);
        holder.qRecSection.setVisibility(View.GONE);
        btnQcommentToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.qRecSection.setVisibility(qRecSectionVisible ? View.GONE : View.VISIBLE);
                qRecSectionVisible = qRecSectionVisible ? false : true;
                if (qRecSectionVisible) {
                    qCommentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (etQCommentText.getText().toString().equals("")) {
                                mOnErrorShowInSnackbarListener.onErrorShow("Не надо пустоты");
                                return;
                            }


                            mOnErrorShowInSnackbarListener.onErrorShow("Posting...");
                            qCommentButton.setEnabled(false);
                            qRecommendButton.setEnabled(false);
                            Log.d("DP", "Commenting (comment_id=" + mThread.commentId);
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
                                    mOnErrorShowInSnackbarListener.onErrorShow(info);
                                }

                                @Override
                                public void onError(String error) {
                                    qCommentButton.setEnabled(true);
                                    qRecommendButton.setEnabled(true);
                                    etQCommentText.setEnabled(true);
                                    mOnErrorShowInSnackbarListener.onErrorShow(error);
                                }
                            };
                            if (mThread.commentId.equals("null")) {

                                new Commentator(mThread.postId, etQCommentText.getText().toString(), callback).postComment();
                            } else {
                                new Commentator(mThread.postId, mThread.commentId, etQCommentText.getText().toString(), callback).postComment();
                            }

                        }
                    });

                    qRecommendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            mOnErrorShowInSnackbarListener.onErrorShow("Recommending...");
                            qCommentButton.setEnabled(false);
                            qRecommendButton.setEnabled(false);
                            Log.d("DP", "Recommending (comment_id=" + mThread.commentId);
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
                                    mOnErrorShowInSnackbarListener.onErrorShow(info);
                                }

                                @Override
                                public void onError(String error) {
                                    qCommentButton.setEnabled(true);
                                    qRecommendButton.setEnabled(true);
                                    etQCommentText.setEnabled(true);
                                    mOnErrorShowInSnackbarListener.onErrorShow(error);
                                }
                            };
                            if (mThread.commentId.equals("null")) {

                                new Recommender(mThread.postId, etQCommentText.getText().toString(), callback).postComment();
                            } else {
                                new Recommender(mThread.postId, mThread.commentId, etQCommentText.getText().toString(), callback).postComment();
                            }

                        }
                    });
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mPostList == null) return 0;
        else return mPostList.posts.size() + 1;
    }

    public void setOnLoadMoreRequestListener(OnLoadMoreRequestListener onLoadMoreRequestListener) {
        mOnLoadMoreRequestListener = onLoadMoreRequestListener;
    }

    public void setOnPostClickListener(OnPostClickListener onPostClickListener) {
        mOnPostClickListener = onPostClickListener;
    }

    public void setOnPostListUpdateListener(OnPostListUpdateListener onPostListUpdateListener) {
    }

    public interface OnLoadMoreRequestListener {
        boolean onLoadMoreRequested();//return false if cannot load more
    }

    public interface OnPostClickListener {
        void onPostClicked(View view, String post);

        void onTagClicked(View view, String tag);
    }

    protected class FooterHolder extends RecyclerView.ViewHolder {
        ProgressBar progressWheel;

        public FooterHolder(View itemView) {
            super(itemView);
            progressWheel = (ProgressBar) itemView.findViewById(R.id.progress_wheel);
        }
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {


        View mView;
        final TextView text;
        final ViewGroup tags;
        final ImageView avatar;
        final TextView author;
        final TextView post_id;

        final TextView date;
        View qCommentSection;
        LinearLayout llPostContent;
        final View mainContent;
        final Button btnShowAll;
        final Button btnShow10;
        final Button qCommentToggle;
        final EditText qCommentText;
        final ImageButton qCommentButton;
        final ImageButton qRecommendButton;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            qCommentSection = itemView.findViewById(R.id.qcomment_section);

//set show mode.
            llPostContent = (LinearLayout) itemView.findViewById(R.id.post_text);
            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);

            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            btnShow10 = (Button) itemView.findViewById(R.id.btn_show_10_more);
            btnShowAll = (Button) itemView.findViewById(R.id.btn_show_all);
            qCommentToggle = (Button) itemView.findViewById(R.id.qcomment_toggle);

            date = (TextView) itemView.findViewById(R.id.date);
            qCommentText = (EditText) itemView.findViewById(R.id.qcomment_text);
            qCommentButton = (ImageButton) itemView.findViewById(R.id.qcomment_button);
            qRecommendButton = (ImageButton) itemView.findViewById(R.id.qrecommend_button);

            mainContent = itemView.findViewById(R.id.main_content);

        }

    }

    protected class CommentViewHolder extends RecyclerView.ViewHolder {
        View mView;
        final TextView text;
        final Button qCommentToggle;
        final ImageView avatar;
        final TextView author;
        final TextView post_id;

        final TextView date;
        View qRecSection;
        LinearLayout llCommentContent;
        final View mainContent;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            qRecSection = itemView.findViewById(R.id.qrec_section);
            qCommentToggle = (Button) itemView.findViewById(R.id.qcomment_toggle);
//set show mode.
            llCommentContent = (LinearLayout) itemView.findViewById(R.id.post_text);
            text = (TextView) itemView.findViewById(R.id.text);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            date = (TextView) itemView.findViewById(R.id.date);

            mainContent = itemView.findViewById(R.id.main_content);

        }
    }


    public void setOnErrorShowInSnackbarListener(OnErrorShowInSnackbarListener listener) {
        mOnErrorShowInSnackbarListener = listener;

    }
}