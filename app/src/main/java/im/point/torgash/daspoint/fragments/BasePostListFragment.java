package im.point.torgash.daspoint.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Map;

import im.point.torgash.daspoint.MainActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.adapters.PostListAdapter;
import im.point.torgash.daspoint.listeners.OnErrorShowInSnackbarListener;
import im.point.torgash.daspoint.listeners.OnPostListLoadMoreListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;

/**
 * Created by Boss on 15.02.2016.
 */
public abstract class BasePostListFragment extends Fragment {
    SharedPreferences prefs;

    private View rootView;
    private int section_number;

    protected OnPostListUpdateListener mOnPostListUpdateListener;
    protected OnPostListLoadMoreListener mOnPostListLoadMoreListener;
    protected OnErrorShowInSnackbarListener mOnErrorShowInSnackbarListener;
    Handler h;
    private ArrayList<PointPost> postArrayList;
    private SwipeRefreshLayout mSwipeRefresh;

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


        final RecyclerView rvPostList = (RecyclerView) rootView.findViewById(R.id.postList);
        rvPostList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
        final PostListAdapter adapter = createAdapter();

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
                mOnErrorShowInSnackbarListener.onErrorShow(error);
            }
        };
        adapter.setOnPostListUpdateListener(mOnPostListUpdateListener);

        rvPostList.setAdapter(adapter);

        return rootView;
    }

    abstract void loadPosts();

    abstract void loadMorePosts(long before);

    protected PostListAdapter createAdapter() {
        return new PostListAdapter(getActivity());
    }

    public void setOnErrorShowInSnackbarListener(OnErrorShowInSnackbarListener listener) {
        mOnErrorShowInSnackbarListener = listener;
    }
}