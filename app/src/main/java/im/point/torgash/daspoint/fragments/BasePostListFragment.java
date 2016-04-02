package im.point.torgash.daspoint.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.point.torgash.daspoint.MainActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.adapters.PostListAdapter;
import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.listeners.OnFragmentInteractListener;
import im.point.torgash.daspoint.listeners.OnPostListLoadMoreListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.network.Commentator;
import im.point.torgash.daspoint.network.Recommender;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;
import im.point.torgash.daspoint.widgets.EmptyRecyclerView;

/**
 * Created by Boss on 15.02.2016.
 */
public abstract class BasePostListFragment extends Fragment {
    SharedPreferences prefs;

    private View rootView;
    private int section_number;
    boolean mIsLoadingMore;
    protected OnPostListUpdateListener mOnPostListUpdateListener;
    protected OnPostListLoadMoreListener mOnPostListLoadMoreListener;
    static OnActivityInteractListener mOnErrorShowInSnackbarListener;
    protected OnFragmentInteractListener mOnFragmentInteractListener;
    boolean isCommentZoneShown = false;
    PostListAdapter adapter;

    Handler h;
    private ArrayList<PointPost> postArrayList;
    private SwipeRefreshLayout mSwipeRefresh;
    private View commentZone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);


        View rootView = null;
        ArrayList<Map<String, String>> postList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.post_list, null);

        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadPosts();

            }
        });
        mOnFragmentInteractListener = new OnFragmentInteractListener() {
            @Override
            public void toggleCommentZone(String tag) {

                if (isCommentZoneShown) {
                    isCommentZoneShown = false;
                    commentZone.setVisibility(View.GONE);
                    commentZone.setTag(null);
                } else {
                    isCommentZoneShown = true;
                    commentZone.setVisibility(View.VISIBLE);
                    commentZone.setTag(tag);
                }
            }
        };

        commentZone = rootView.findViewById(R.id.commentZone);
        //now let's implement comment zone functions
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
                Log.d("DP", "Commenting (comment_id=" + post.commentId);
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
                if (post.commentId.equals("null")) {

                    new Commentator(post.postId, etQCommentText.getText().toString(), callback).postComment();
                } else {
                    new Commentator(post.postId, post.commentId, etQCommentText.getText().toString(), callback).postComment();
                }

            }
        });

        qRecommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mOnErrorShowInSnackbarListener.onErrorShow("Recommending...");
                qCommentButton.setEnabled(false);
                qRecommendButton.setEnabled(false);
                Log.d("DP", "Recommending (comment_id=" + post.commentId);
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
                if (post.commentId.equals("null")) {

                    new Recommender(post.postId, etQCommentText.getText().toString(), callback).postComment();
                } else {
                    new Recommender(post.postId, post.commentId, etQCommentText.getText().toString(), callback).postComment();
                }

            }
        });

        final EmptyRecyclerView rvPostList = (EmptyRecyclerView) rootView.findViewById(R.id.postList);
        rvPostList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
        adapter = createAdapter();

        mOnPostListUpdateListener = new OnPostListUpdateListener() {
            @Override
            public void onPostListUpdated(PostList postList) {
                adapter.setData(getActivity(), postList);
                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onError(String error) {
                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }
                Log.d("DP", "Error: " + error);
                if (mOnErrorShowInSnackbarListener != null) {

                    mOnErrorShowInSnackbarListener.onErrorShow(error);
                }
            }
        };
        mOnPostListLoadMoreListener = new OnPostListLoadMoreListener() {
            @Override
            public void onPostListLoadMore(PostList postList) {
                adapter.appendData(getActivity(), postList);
            }

            @Override
            public void onError(String error) {
                mOnErrorShowInSnackbarListener.onErrorShow(error);
            }
        };
        adapter.setOnErrorShowInSnackbarListener(mOnErrorShowInSnackbarListener);

        adapter.setOnLoadMoreRequestListener(new PostListAdapter.OnLoadMoreRequestListener() {
            @Override
            public boolean onLoadMoreRequested() {

                if (mIsLoadingMore) {
                    //do nothing
                } else {
                    List<PointPost> posts = adapter.getPostList().posts;
                    if (posts.size() < 1) {
                        adapter.getPostList().has_next = false;
                        return false;
                    } else {
                        loadMore(posts.get(posts.size() - 1).uid);
                    }
                }
                return true;
            }
        });
        rvPostList.setHasFixedSize(true);
        View emptyView = rootView.findViewById(R.id.emptyview);
        rvPostList.setItemViewCacheSize(8);
        rvPostList.setAdapter(adapter);
        rvPostList.setEmptyView(emptyView);
        loadPosts();
        return rootView;
    }

    @Override
    public void onResume() {
        mOnErrorShowInSnackbarListener.showFAB();
        super.onResume();
    }

    abstract void loadPosts();

    abstract void loadMore(long before);

    protected PostListAdapter createAdapter() {
        return new PostListAdapter(getActivity());
    }

    public void setOnErrorShowInSnackbarListener(OnActivityInteractListener listener) {
        mOnErrorShowInSnackbarListener = listener;
        adapter.setOnErrorShowInSnackbarListener(listener);
    }
}
