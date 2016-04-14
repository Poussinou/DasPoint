package im.point.torgash.daspoint.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
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

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.point.torgash.daspoint.ImageViewFullscreenActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.BubbleTextGetter;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.listeners.OnLinksDetectedListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.network.Commentator;
import im.point.torgash.daspoint.network.Recommender;
import im.point.torgash.daspoint.point.Comment;
import im.point.torgash.daspoint.point.PointThread;
import im.point.torgash.daspoint.point.ThreadHeaderPost;
import im.point.torgash.daspoint.utils.ActivePreferences;
import im.point.torgash.daspoint.utils.Constants;


public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BubbleTextGetter{
    private ThreadHeaderPost mPost;
    private List<Comment> mComments;
    private Context mContext;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = -1;

    int commentsToDisplay = 10;
    private PointThread mThread = null;
    //    private ImageSearchTask mTask;

    private OnPostClickListener mOnPostClickListener = null;
    private OnActivityInteractListener mOnActivityInteractListener;


    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnPostClickListener != null)
                mOnPostClickListener.onTagClicked(view, ((TextView) view).getText().toString());
        }
    };
    private boolean mHasHeader = true;

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
        final HeaderViewHolder holder = new HeaderViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mOnActivityInteractListener.showCommentZone("#" + mPost.postId, null, mPost.postText.substring(0, Math.min(mPost.postText.length(), 80)));

            }
        });
        return holder;

    }

    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup) {

        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.comment_list_item, viewGroup, false);
        final CommentViewHolder holder = new CommentViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    mOnActivityInteractListener.showCommentZone(mPost.postId, holder.comment_id_field, holder.shortenedText);

            }
        });
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
//                    mOnPostClickListener.onPostClicked(v, view.getTag(R.id.comment_id).toString());
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
            public void onLinksDetected(ArrayList<Map<String, String>> postContents) {
                headerHolder.llPostContent.removeAllViews();
                for (Map<String, String> m : postContents) {
                    String mime = m.get("mime");
                    if (mime.equals("image")) {
                        View iv = li.inflate(R.layout.post_image_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        headerHolder.llPostContent.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ImageLoader.getInstance().displayImage(m.get("text"), ivPostImageView);
                        ivPostImageView.setAdjustViewBounds(true);
                        final String url = m.get("text");
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!url.contains(".gif")) {
                                    Intent intent = new Intent(mContext, ImageViewFullscreenActivity.class);
                                    intent.putExtra("url", url);
                                    mOnActivityInteractListener.onIntentStart(intent);

                                } else {
                                    mOnActivityInteractListener.onErrorShow("Поддержка GIF еще не запилена.");
                                }
                            }
                        });
                        Log.d("DP", "Created imageview");
                    }
                    if (mime.equals("text") && !m.get("text").trim().equals("")) {
                        View tv = li.inflate(R.layout.text_view, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        headerHolder.llPostContent.addView(tv);
                        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
                        tView.setText(m.get("text").trim());
                        Log.d("DP", "Created textview with text: \n " + m.get("text"));
                    }
                    if (mime.equals("webpage")) {
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
                                mOnActivityInteractListener.onIntentStart(intent);
                            }
                        });
                    }

                }
            }
        };
        mPost.searchAndDetectLinks(linksDetectedListener);

        headerHolder.author.setText(mPost.authorLogin);

        headerHolder.itemView.setTag(R.id.post_id, mPost.postId);
        Log.d("DP", "Invading viewholder with post \n" + mPost);
        ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + mPost.authorAvatar, headerHolder.avatar);

        headerHolder.date.setText(mPost.postCreatedString);

        headerHolder.post_id.setTag(mPost.postId);
        headerHolder.post_id.setText("#" + mPost.postId);
//        holder.webLink.setTag(post.messageLink);
//        holder.favourite.setChecked(post.bookmarked);
//        holder.favourite.setTag(post.post.id);



        // holder.comments.setVisibility(View.GONE);

        headerHolder.tags.removeAllViews();

        for (String tag : mPost.tags) {
            final TextView v = (TextView) li.inflate(R.layout.tag, headerHolder.tags, false);
            v.setText(tag);
            headerHolder.tags.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            v.setOnClickListener(mOnTagClickListener);
        }


//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)


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
        final Comment comment = mComments.get(i);
        //Change it to my layout
        //holder.imageList.setImageUrls(thread.thread.text.images, thread.thread.files);
        holder.llCommentContent.removeAllViews();
        View tv = li.inflate(R.layout.text_view, null);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.llCommentContent.addView(tv);
        TextView tView = (TextView) tv.findViewById(R.id.post_text_view);
        tView.setText(comment.text);
        tView.setLinksClickable(true);
        tView.setAutoLinkMask(Linkify.ALL);
        if (ActivePreferences.markDownMode) {

            Markdown4jProcessor processor = new Markdown4jProcessor();

            try {
                String HTMLText = processor.process(comment.text);
                tView.setText(Html.fromHtml(HTMLText));
            } catch (IOException e) {
                tView.setText(comment.text);
                e.printStackTrace();
            }

        }else{
            tView.setText(comment.text);
        }



        holder.shortenedText = comment.text.substring(0, comment.text.length() > 80 ? 80 : comment.text.length());
        holder.comment_id_field = comment.id;
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
                                    mOnActivityInteractListener.onIntentStart(intent);

                                } else {
                                    mOnActivityInteractListener.onErrorShow("Поддержка GIF еще не запилена.");
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
                                mOnActivityInteractListener.onIntentStart(intent);
                            }
                        });
                    }

                }
            }
        };
        if (!ActivePreferences.economyMode) {

            comment.searchAndDetectLinks(linksDetectedListener);
        }


        holder.author.setText(comment.authorName);

        holder.itemView.setTag(R.id.post_id, comment.id);
        Log.d("DP", "Invading viewholder with thread \n" + mThread);


        holder.date.setText(comment.createdString);
        holder.comment_id.setTag(comment.id);

        if (!comment.to_comment_id.equals("null")) {
            holder.comment_id.setText("/" + comment.id + "\u2192" + "/" + comment.to_comment_id);

        }else{
            holder.comment_id.setText("/" + comment.id);
        }


    }

    @Override
    public int getItemCount() {
        if (mComments == null) return 0;
        else return mComments.size() + 1;
    }



    public void setOnPostClickListener(OnPostClickListener onPostClickListener) {
        mOnPostClickListener = onPostClickListener;
    }

    public void setOnPostListUpdateListener(OnPostListUpdateListener onPostListUpdateListener) {
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return mComments.get(pos).id;
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

        LinearLayout llPostContent;
        final View mainContent;


        final EditText qCommentText;
        final ImageButton qCommentButton;
        final ImageButton qRecommendButton;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


//set show mode.
            llPostContent = (LinearLayout) itemView.findViewById(R.id.post_text);
            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);

            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);



            date = (TextView) itemView.findViewById(R.id.date);
            qCommentText = (EditText) itemView.findViewById(R.id.qcomment_text);
            qCommentButton = (ImageButton) itemView.findViewById(R.id.qcomment_button);
            qRecommendButton = (ImageButton) itemView.findViewById(R.id.qrecommend_button);

            mainContent = itemView.findViewById(R.id.main_content);

        }

    }

    protected class CommentViewHolder extends RecyclerView.ViewHolder {
        String shortenedText;
        View mView;
        final TextView text;

        final TextView author;
        final TextView comment_id;

        final TextView date;

        LinearLayout llCommentContent;
        final View mainContent;
        public String comment_id_field;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


//set show mode.

            llCommentContent = (LinearLayout) itemView.findViewById(R.id.comment_text);
            text = (TextView) itemView.findViewById(R.id.text);


            author = (TextView) itemView.findViewById(R.id.comment_author);
            comment_id = (TextView) itemView.findViewById(R.id.comment_id);
            date = (TextView) itemView.findViewById(R.id.date);

            mainContent = itemView.findViewById(R.id.main_content);

        }
    }


    public void setOnActivityInteractListener (OnActivityInteractListener listener) {
        mOnActivityInteractListener = listener;

    }
}