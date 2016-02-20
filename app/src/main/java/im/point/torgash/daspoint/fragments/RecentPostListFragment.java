package im.point.torgash.daspoint.fragments;

import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.network.PostsLoader;
import im.point.torgash.daspoint.utils.Constants;

/**
 * Created by torgash on 19.02.16.
 */
public class RecentPostListFragment extends BasePostListFragment {
    static RecentPostListFragment instance;
    public RecentPostListFragment(){

    }
    public static RecentPostListFragment getInstance(){
        if(null == instance){
            return new RecentPostListFragment();
        }
        else return instance;
    }

    @Override
    void loadPosts() {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_RECENT_URL);
        loader.setmOnPostListUpdateListener(mOnPostListUpdateListener);
        loader.getPosts();
    }

    @Override
    void loadMorePosts(long before) {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_RECENT_URL, before);
        loader.setmOnPostListLoadMoreListener(mOnPostListLoadMoreListener);
        loader.getPosts();
    }


}
