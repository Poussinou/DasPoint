package im.point.torgash.daspoint.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Map;

import im.point.torgash.daspoint.MainActivity;
import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.adapters.ThreadAdapter;
import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.listeners.OnThreadUpdateListener;
import im.point.torgash.daspoint.network.ThreadLoader;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PointThread;
import im.point.torgash.daspoint.utils.Constants;
import im.point.torgash.daspoint.widgets.EmptyRecyclerView;
import im.point.torgash.daspoint.widgets.FastScroller;

/**
 * Created by Boss on 15.02.2016.
 */
public class ThreadFragment extends Fragment {
    SharedPreferences prefs;
    String mPostId;

    protected OnThreadUpdateListener mOnThreadUpdateListener;

    static OnActivityInteractListener mOnActivityInteractListener;

    ThreadAdapter adapter;

    Handler h;
    private ArrayList<PointPost> postArrayList;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        mPostId = getArguments().getString("postId", null);



        View rootView = null;
        ArrayList<Map<String, String>> postList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.thread_list, null);

        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) rootView.findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something here when it starts to refresh
                // e.g. to request data from server
                loadComments();
            }
        });


        final EmptyRecyclerView rvThreadList = (EmptyRecyclerView) rootView.findViewById(R.id.postList);
        rvThreadList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
        adapter = createAdapter();
        FastScroller fastScroller=(FastScroller)rootView.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(rvThreadList);
        mOnThreadUpdateListener = new OnThreadUpdateListener() {
            @Override
            public void onThreadUpdated(PointThread thread) {
                adapter.setData(getActivity(), thread);
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.refreshComplete();
                }
            }

            @Override
            public void onError(String error) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.refreshComplete();
                }
                Log.d("DP", "Error: " + error);
                if (mOnActivityInteractListener != null) {

                    mOnActivityInteractListener.onErrorShow(error);
                }
            }
        };

        adapter.setOnActivityInteractListener(mOnActivityInteractListener);


        rvThreadList.setHasFixedSize(true);
        View emptyView = rootView.findViewById(R.id.emptyview);
        rvThreadList.setItemViewCacheSize(8);
        rvThreadList.setAdapter(adapter);
        rvThreadList.setEmptyView(emptyView);
        loadComments();
        return rootView;
    }

    void loadComments(){
        ThreadLoader loader = new ThreadLoader(Constants.POINT_API_COMMENT + mPostId);
        loader.setOnThreadUpdateListener(mOnThreadUpdateListener);
        loader.getPosts();
    }



    protected ThreadAdapter createAdapter() {
        return new ThreadAdapter(getActivity());
    }

    public void setOnErrorShowInSnackbarListener(OnActivityInteractListener listener) {
        mOnActivityInteractListener = listener;

    }
}
