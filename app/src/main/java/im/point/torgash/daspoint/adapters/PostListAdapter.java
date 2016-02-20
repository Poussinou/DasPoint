package im.point.torgash.daspoint.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.daimajia.swipe.SwipeLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;


public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = -1;

    private PostList mPostList = null;
    //    private ImageSearchTask mTask;
    private OnLoadMoreRequestListener mOnLoadMoreRequestListener = null;
    private OnPostListUpdateListener mOnPostListUpdateListener = null;
    private OnPostClickListener mOnPostClickListener = null;
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
                    return mPostList.posts.get(position - 1).uid;
                else
                    return mPostList.posts.get(position).uid;
        }
    }

    public PostListAdapter(Context context) {
        super();
        setHasStableIds(true);
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

    public void onBindItemViewHolder(PostListAdapter.ViewHolder holder, int i) {
        PointPost post = mPostList.posts.get(i);
        holder.author.setText("@" + post.authorLogin);

        holder.itemView.setTag(R.id.post_id, post.postId);
        Log.d("DP", "Invading viewholder with post \n" + post);
        //Change it to my layout
        //holder.imageList.setImageUrls(post.post.text.images, post.post.files);
        holder.text.setText(post.postText);
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
            if (post.recCommentId != "null") {

                holder.recommend_id.setText("/" + post.recCommentId);
            } else holder.recommend_id.setText("");
            ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + post.recAuthorAvatar, holder.recomender_avatar);
        } else {
            holder.mainContent.setBackgroundColor(Color.TRANSPARENT);
            holder.recommend_info.setVisibility(View.GONE);
            holder.recommend_text.setVisibility(View.GONE);
//            holder.quote_mark.setVisibility(View.INVISIBLE);
//            holder.quote_mark_top.setVisibility(View.GONE);
        }
        if (post.commentId == null) {
            holder.post_id.setText("#" + post.postId);
        } else {
            holder.post_id.setText("#" + post.postId + "/" + post.commentId);
        }
        holder.post_id.setTag(post.postId);
//        holder.webLink.setTag(post.messageLink);
//        holder.favourite.setChecked(post.bookmarked);
//        holder.favourite.setTag(post.post.id);


        holder.comments.setText(String.valueOf(post.commentsCount));
        // holder.comments.setVisibility(View.GONE);

        LayoutInflater li;
        li = (LayoutInflater) holder.itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        holder.tags.removeAllViews();
        if (post.commentId != null || post.tags == null || post.tags.length == 0) {
            holder.tags.setVisibility(View.GONE);
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
        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.mView.findViewById(R.id.bottom_wrapper));
        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                ((TextView)layout.findViewById(R.id.qcomment_text)).requestFocus();

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });
//        holder.swipeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(final View v, boolean hasFocus) {
//                if (hasFocus) {
//                    // Request focus in a short time because the
//                    // keyboard may steal it away.
//                    v.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (!v.hasFocus()) {
//                                v.requestFocus();
//                            }
//                        }
//                    }, 1000);
//                }
//            }
//        });

//        TextView tvQcommentInput = (TextView)holder.mView.findViewById(R.id.qcomment_text);
//        tvQcommentInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            final int minDelta = 300;           // threshold in ms
//            long focusTime = 0;                 // time of last touch
//            View focusTarget = null;
//            @Override
//            public void onFocusChange(final View v, boolean hasFocus) {
//                if (hasFocus) {
//                    // Request focus in a short time because the
//                    // keyboard may steal it away.
//                    v.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (!v.hasFocus()) {
//                                v.requestFocus();
//                            }
//                        }
//                    }, 1000);
//                }
//            }
//        });
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
        View mView;
        final TextView text;
        final ViewGroup tags;
        final ImageView avatar;
        final ImageView recomender_avatar;
        final TextView recommend_text;


        final TextView recommend_author;
        final TextView author;
        final TextView post_id;
        final View recommend_info;
        final TextView recommend_id;
        final TextView comments;
        final TextView date;
        SwipeLayout swipeLayout;

        final View mainContent;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipePostListItemPart);

//set show mode.

            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            recomender_avatar = (ImageView) itemView.findViewById(R.id.recommend_avatar);
            recommend_text = (TextView) itemView.findViewById(R.id.recommend_text);


            recommend_author = (TextView) itemView.findViewById(R.id.recommend_author);
            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            recommend_info = itemView.findViewById(R.id.recommend_info);
            recommend_id = (TextView) itemView.findViewById(R.id.recommend_id);
            comments = (TextView) itemView.findViewById(R.id.comments);

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
}