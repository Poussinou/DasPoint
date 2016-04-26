package im.point.torgash.daspoint.adapters;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.vanniktech.emoji.EmojiTextView;

import org.markdown4j.ExtDecorator;
import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;

import im.point.torgash.daspoint.ImageViewFullscreenActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.UserInfoActivity;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.listeners.OnFragmentInteractListener;
import im.point.torgash.daspoint.listeners.OnLinksDetectedListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.network.PostSubscriber;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;
import im.point.torgash.daspoint.utils.ActivePreferences;
import im.point.torgash.daspoint.utils.Constants;
import im.point.torgash.daspoint.utils.Utils;
import im.point.torgash.daspoint.widgets.FlowLayout;
import im.point.torgash.daspoint.widgets.PostListCommentButton;


public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = -1;
    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    boolean qRecSectionVisible = false;
    private PostList mPostList = null;
    //    private ImageSearchTask mTask;
    private OnLoadMoreRequestListener mOnLoadMoreRequestListener = null;
    private OnPostListUpdateListener mOnPostListUpdateListener = null;
    private OnPostClickListener mOnPostClickListener = null;
    private OnActivityInteractListener mOnActivityInteractListener;

    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnPostClickListener != null)
                mOnPostClickListener.onTagClicked(view, ((TextView) view).getText().toString());
        }
    };

    private boolean mHasHeader = false;
    private OnFragmentInteractListener mOnFragmentInteractListener;

    @Override
    public long getItemId(int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOOTER:
                return -1;
            case TYPE_HEADER:
                return 0;
            default:
                if (mHasHeader)
                    return mPostList.posts.get(position - 1).uid;
                else
                    return mPostList.posts.get(position).uid;
        }
    }

    public PostListAdapter(Context context) {
        super();
        setHasStableIds(true);
        mContext = context;
    }

    protected void setHasHeader(boolean hasHeader) {
        mHasHeader = hasHeader;
    }

    public PostList getPostList() {
        return mPostList;
    }

    public void setData(Context context, PostList postList) {
        mPostList = postList;
//        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
//            mTask.cancel(true);
//        }
//        mTask = new ImageSearchTask(context);
//        mTask.execute(mPostList);
        notifyDataSetChanged();
    }

    public void appendData(Context context, PostList postList) {

        int oldLength = ((null == mPostList) ? 0 : mPostList.posts.size());
        if (mPostList != null) {
            mPostList.append(postList);
        } else {
            mPostList = new PostList();
            mPostList.posts = new ArrayList<>();
        }
        notifyItemRangeInserted(oldLength, postList.posts.size());
//        if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
//            mTask = new ImageSearchTask(context);
//            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPostList);
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mPostList.posts.size() + (mHasHeader ? 1 : 0))
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

    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return new RecyclerView.ViewHolder(viewGroup) {
        };
    }

    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup) {

        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.post_list_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.post_id.getTag().equals("post")){

                    mOnActivityInteractListener.showCommentZone(holder.post_id.getText().toString(), null, holder.shortenedText);
                }else{
                    String[] data = holder.post_id.getText().toString().split("/");
                    for (String s : data) {
                        Log.d(Constants.LOG_TAG, "data split result: " + s);
                    }
                    mOnActivityInteractListener.showCommentZone(data[0].substring(1), data[1], holder.shortenedText);
                }
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("primary", holder.fullText);

                clipboard.setPrimaryClip(clip);
                mOnActivityInteractListener.onErrorShow("post " + holder.post_id.getText().toString() + " copied to clipboard");
                return true;
            }
        });
        holder.btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://point.im/" + getPostId(holder.post_id.getText().toString())));
                Bundle extras = new Bundle();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){

                    extras.putBinder(EXTRA_CUSTOM_TABS_SESSION,
                            null /* Set to null for no session */);
                }

                intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, R.color.colorPrimary);

                intent.putExtras(extras);
                mOnActivityInteractListener.onIntentStart(intent);
            }
        });

        View.OnClickListener userClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent userInfoActivityIntent = new Intent(mContext, UserInfoActivity.class);

                userInfoActivityIntent.putExtra("login", holder.author.getText().toString());
                mOnActivityInteractListener.onIntentStart(userInfoActivityIntent);

        }};
        holder.avatar.setOnClickListener(userClickListener);
        holder.author.setOnClickListener(userClickListener);

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

    private String getPostId(String s) {
        if (s.contains("/")) {
            return s.split("/")[0].substring(1);
        }else{
            return s.substring(1);
        }

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
        if (mPostList.has_next) {
            footerHolder.progressWheel.setVisibility(View.VISIBLE);
            if (mOnLoadMoreRequestListener != null) {
                if (!mOnLoadMoreRequestListener.onLoadMoreRequested()) {
                    footerHolder.progressWheel.setVisibility(View.GONE);
                }
            }
        } else {
            footerHolder.progressWheel.setVisibility(View.GONE);
        }
    }

    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        //do nothin
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        int type = getItemViewType(i);
        if (type == TYPE_FOOTER) {
            onBindFooterViewHolder(holder);
        } else if (type == TYPE_HEADER) {
            onBindHeaderViewHolder(holder);
        } else {
            onBindItemViewHolder((ViewHolder) holder, mHasHeader ? (i - 1) : i);
        }
    }

    public void onBindItemViewHolder(final PostListAdapter.ViewHolder holder, int i) {
        final LayoutInflater li = (LayoutInflater) holder.itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final PointPost post = mPostList.posts.get(i);
        //Change it to my layout
        //holder.imageList.setImageUrls(post.post.text.images, post.post.files);
        holder.llPostContent.removeAllViews();
        View tv = li.inflate(R.layout.text_view, null);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.llPostContent.addView(tv);
        EmojiTextView tView = (EmojiTextView) tv.findViewById(R.id.post_text_view);
        tView.setLinksClickable(true);
        tView.setAutoLinkMask(Linkify.ALL);
        if (ActivePreferences.markDownMode) {

            Markdown4jProcessor processor = new Markdown4jProcessor();

            try {
                String HTMLText = processor.process(post.postText);

                tView.setText(Html.fromHtml(HTMLText));
            } catch (IOException e) {
                tView.setText(post.postText);
                e.printStackTrace();
            }

        }else{
            tView.setText(post.postText);
        }
        holder.shortenedText = post.postText.substring(0, post.postText.length() > 80 ? 80 : post.postText.length());
        holder.fullText = post.postText;
        OnLinksDetectedListener linksDetectedListener = new OnLinksDetectedListener() {
            @Override
            public void onLinksDetected(ArrayList<Map<String, String>> postContents) {
                holder.llPostContent.removeAllViews();
                for (Map<String, String> m : postContents) {
                    String mime = m.get("mime");
                    Log.d(Constants.LOG_TAG, "Creating a view with mime: " + mime);
                    if (mime.equals("youtube")) {
                        final String url = m.get("text");
                        String previewUrl = url;

                        try {
                            previewUrl = "http://img.youtube.com/vi/" + Utils.extractYoutubeId(url) + "/0.jpg";
                            Log.d(Constants.LOG_TAG, "Converted Youtube link to preview");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        View iv = li.inflate(R.layout.post_youtube_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llPostContent.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ImageLoader.getInstance().displayImage(previewUrl, ivPostImageView);
                        ivPostImageView.setAdjustViewBounds(true);
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnActivityInteractListener.onIntentStart(intent);


                            }
                        });
                        Log.d("DP", "Created youtube view");
                    }
                    if (mime.equals("image")) {
                        View iv = li.inflate(R.layout.post_image_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llPostContent.addView(iv);
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
                    if (mime.equals("gif")) {
                        View iv = li.inflate(R.layout.post_image_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llPostContent.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ivPostImageView.setImageResource(R.mipmap.gif_placeholder);

                        ivPostImageView.setAdjustViewBounds(true);
                        final String url = m.get("text");
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnActivityInteractListener.onIntentStart(intent);
                            }
                        });
                        Log.d("DP", "Created gif imageview");
                    }
                    if (mime.equals("text") && !m.get("text").trim().equals("")) {
                        View tv = li.inflate(R.layout.text_view, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llPostContent.addView(tv);
                        EmojiTextView tView = (EmojiTextView) tv.findViewById(R.id.post_text_view);
                        String text = m.get("text").trim();
                        tView.setLinksClickable(true);
                        tView.setAutoLinkMask(Linkify.ALL);
                        if (ActivePreferences.markDownMode) {

                            Markdown4jProcessor processor = new Markdown4jProcessor();

                            try {
                                String HTMLText = processor.process(text);
                                tView.setText(Html.fromHtml(HTMLText));
                            } catch (Exception e) {
                                tView.setText(text);
                                e.printStackTrace();
                            }

                        } else {
                            tView.setText(text);
                            Log.d("DP", "Created textview with text: \n " + text);
                        }
                    }
                    if (mime.equals("webpage")) {
                        View tv = li.inflate(R.layout.webpage_link, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        holder.llPostContent.addView(tv);
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

            @Override
            public void onLinksDetected(ArrayList<Map<String, String>> postContents, boolean positionAtBottom) {
                FlowLayout linksLayout = (FlowLayout)li.inflate(R.layout.links_layout, null);
                for (Map<String, String> m : postContents) {
                    String mime = m.get("mime");
                    Log.d(Constants.LOG_TAG, "Creating a view with mime: " + mime);
                    if (mime.equals("youtube")) {
                        final String url = m.get("text");
                        String previewUrl = url;

                        try {
                            previewUrl = "http://img.youtube.com/vi/" + Utils.extractYoutubeId(url) + "/0.jpg";
                            Log.d(Constants.LOG_TAG, "Converted Youtube link to preview");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        View iv = li.inflate(R.layout.post_youtube_view, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linksLayout.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ImageLoader.getInstance().displayImage(previewUrl, ivPostImageView);
                        ivPostImageView.setAdjustViewBounds(true);
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnActivityInteractListener.onIntentStart(intent);


                            }
                        });
                        Log.d("DP", "Created youtube view");
                    }
                    if (mime.equals("image")) {
                        View iv = li.inflate(R.layout.post_image_view_flow, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linksLayout.addView(iv);
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
                    if (mime.equals("gif")) {
                        View iv = li.inflate(R.layout.post_image_view_flow, null);
                        iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linksLayout.addView(iv);
                        ImageView ivPostImageView = (ImageView) iv.findViewById(R.id.post_image_view);
                        ivPostImageView.setImageResource(R.mipmap.gif_placeholder);

                        ivPostImageView.setAdjustViewBounds(true);
                        final String url = m.get("text");
                        ivPostImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mOnActivityInteractListener.onIntentStart(intent);
                            }
                        });
                        Log.d("DP", "Created gif imageview");
                    }
                    if (mime.equals("text") && !m.get("text").trim().equals("")) {
                        View tv = li.inflate(R.layout.text_view, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linksLayout.addView(tv);
                        EmojiTextView tView = (EmojiTextView) tv.findViewById(R.id.post_text_view);
                        String text = m.get("text").trim();
                        tView.setLinksClickable(true);
                        tView.setAutoLinkMask(Linkify.ALL);
                        if (ActivePreferences.markDownMode) {

                            Markdown4jProcessor processor = new Markdown4jProcessor();

                            try {
                                String HTMLText = processor.process(text);
                                tView.setText(Html.fromHtml(HTMLText));
                            } catch (Exception e) {
                                tView.setText(text);
                                e.printStackTrace();
                            }

                        } else {
                            tView.setText(text);
                            Log.d("DP", "Created textview with text: \n " + text);
                        }
                    }
                    if (mime.equals("webpage")) {
                        View tv = li.inflate(R.layout.webpage_link, null);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linksLayout.addView(tv);
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
                holder.llPostContent.addView(linksLayout);
            }
        };
        if (!ActivePreferences.economyMode) {

            post.searchAndDetectLinks(linksDetectedListener, true);
        }

        holder.author.setText(post.authorLogin);

        holder.itemView.setTag(R.id.post_id, post.postId);
        Log.d("DP", "Invading viewholder with post \n" + post);
        ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + post.authorAvatar, holder.avatar);

        holder.date.setText(post.postCreatedString);

        if (post.isRecommended) {
//            holder.mainContent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.quote_background));
            holder.recommend_info.setVisibility(View.VISIBLE);
            if (post.recText != null) {
                holder.recommend_text.setVisibility(View.VISIBLE);
                holder.recommend_text.setText(post.recText);
            } else {
                holder.recommend_text.setVisibility(View.GONE);
            }
//            holder.quote_mark.setVisibility(View.VISIBLE);
//            holder.quote_mark_top.setVisibility(View.VISIBLE);
            holder.recommend_author.setText(post.recAuthorLogin);
            if (!(post.recCommentId.equals("null"))) {

                holder.recommend_id.setText("/" + post.recCommentId);
            } else holder.recommend_id.setText("");

        } else {
            holder.mainContent.setBackgroundColor(Color.TRANSPARENT);
            holder.recommend_info.setVisibility(View.GONE);
            holder.recommend_text.setVisibility(View.GONE);
//            holder.quote_mark.setVisibility(View.INVISIBLE);
//            holder.quote_mark_top.setVisibility(View.GONE);
        }
        if (post.commentId.equals("null")) {
            holder.post_id.setText("#" + post.postId);
            holder.post_id.setTag("post");
        } else {
            holder.post_id.setText("#" + post.postId + "/" + post.commentId);
            holder.post_id.setTag("comment");
        }

//        holder.webLink.setTag(post.messageLink);
//        holder.favourite.setChecked(post.bookmarked);
//        holder.favourite.setTag(post.post.id);
        if (post.subscribed) {
            holder.comments.setBackgroundResource(R.drawable.button_green);
        }else{
            holder.comments.setBackgroundResource(R.drawable.button_white);
        }

        holder.comments.setCommentCount(post.commentsCount);
        // holder.comments.setVisibility(View.GONE);

        holder.tags.removeAllViews();
        if (!post.commentId.equals("null") || post.tags == null || post.tags.length == 0) {
//            holder.tags.setVisibility(View.GONE);
        } else {
            holder.tags.setVisibility(View.VISIBLE);

            int n = 0;
            for (String tag : post.tags) {
                final TextView v = (TextView) li.inflate(R.layout.tag, holder.tags, false);
                v.setText(tag);
                holder.tags.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                v.setOnClickListener(mOnTagClickListener);
            }
        }
        ;

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)

        qRecSectionVisible = false;
        holder.comments.setTag(post.postId);

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnActivityInteractListener.onTheadOpen(post.postId);
            }
        });
        holder.comments.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CommonRequestCallback callback = new CommonRequestCallback() {
                    @Override
                    public void onSuccess(String info) {
                        post.subscribed = !post.subscribed;
                        if (post.subscribed) {

                            holder.comments.setBackgroundResource(R.drawable.button_green);
                        }else{
                            holder.comments.setBackgroundResource(R.drawable.button_white);
                        }
                        mOnActivityInteractListener.onErrorShow(info);
                    }

                    @Override
                    public void onError(String error) {
                        mOnActivityInteractListener.onErrorShow(error);
                    }
                };
                new PostSubscriber(post.postId, callback, !post.subscribed).toggleSubscription();
                return true;
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
        this.mOnPostListUpdateListener = onPostListUpdateListener;
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

    protected class ViewHolder extends RecyclerView.ViewHolder {
        String shortenedText;
        String fullText;
        View mView;
        final TextView text;
        final ViewGroup tags;
        final ImageView avatar;
        final ImageButton btnWeb;
        final TextView recommend_text;


        final TextView recommend_author;
        final TextView author;
        final TextView post_id;
        final View recommend_info;
        final TextView recommend_id;
        final PostListCommentButton comments;
        final TextView date;

        LinearLayout llPostContent;
        final View mainContent;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;


//set show mode.
            llPostContent = (LinearLayout)itemView.findViewById(R.id.post_text);
            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            btnWeb = (ImageButton) itemView.findViewById(R.id.btnWeb);
            recommend_text = (TextView) itemView.findViewById(R.id.recommend_text);


            recommend_author = (TextView) itemView.findViewById(R.id.recommend_author);
            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            recommend_info = itemView.findViewById(R.id.recommend_info);
            recommend_id = (TextView) itemView.findViewById(R.id.recommend_id);
            comments = (PostListCommentButton) itemView.findViewById(R.id.btnComments);

            date = (TextView) itemView.findViewById(R.id.date);


            mainContent = itemView.findViewById(R.id.main_content);

        }
    }

//    private class ImageSearchTask extends AsyncTask<PostList, Integer, Void> {
//        SharedPreferences prefs;
//        private final boolean loadImages;
//
//        ImageSearchTask(Context context) {
//            prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
//            loadImages = prefs.getBoolean("loadImages", true);
//        }

    //        @Override
//        protected Void doInBackground(PostList... postLists) {
//            List<Post> posts = postLists[0].posts;
//            for (int i = 0; i < posts.size(); i++) {
//                Post post = posts.get(i);
//                post.post.text.images = ImageSearchHelper.checkImageLinks(ImageSearchHelper.getAllLinks(post.post.text.text));
//                publishProgress(i);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            if (!loadImages) cancel(true);
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            notifyItemChanged(values[0]);
//            super.onProgressUpdate(values);
//        }
//    }
    public void setOnActivityInteractListener (OnActivityInteractListener listener) {
        mOnActivityInteractListener = listener;

    }

    public void setListeners(OnActivityInteractListener aListener, OnFragmentInteractListener fListener) {
        mOnActivityInteractListener = aListener;
        mOnFragmentInteractListener = fListener;
    }
}