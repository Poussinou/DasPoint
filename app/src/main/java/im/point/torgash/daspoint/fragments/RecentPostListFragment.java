package im.point.torgash.daspoint.fragments;

import im.point.torgash.daspoint.listeners.OnErrorShowInSnackbarListener;
import im.point.torgash.daspoint.network.PostsLoader;
import im.point.torgash.daspoint.utils.Constants;

/**
 * Created by torgash on 19.02.16.
 */
public class RecentPostListFragment extends BasePostListFragment {
    static RecentPostListFragment instance;
    public RecentPostListFragment(){

    }
    public static RecentPostListFragment getInstance(OnErrorShowInSnackbarListener listener){
        mOnErrorShowInSnackbarListener  = listener;
        if(null == instance){
            return new RecentPostListFragment();
        }
        else return instance;
    }

    @Override
    void loadPosts() {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_RECENT_URL);
        loader.setOnPostListUpdateListener(mOnPostListUpdateListener);
        loader.getPosts();
    }

    @Override
    void loadMore(long before) {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_RECENT_URL, before);
        loader.setOnPostListLoadMoreListener(mOnPostListLoadMoreListener);
        loader.getPosts();
    }


}
