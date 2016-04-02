package im.point.torgash.daspoint.fragments;

import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.network.PostsLoader;
import im.point.torgash.daspoint.utils.Constants;

/**
 * Created by torgash on 19.02.16.
 */
public class AllPostsListFragment extends BasePostListFragment {
    static AllPostsListFragment instance;
    public AllPostsListFragment(){

    }
    public static AllPostsListFragment getInstance(OnActivityInteractListener listener){
        mOnErrorShowInSnackbarListener  = listener;
        if(null == instance){
            return new AllPostsListFragment();
        }
        else return instance;
    }

    @Override
    void loadPosts() {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_ALL_URL);
        loader.setOnPostListUpdateListener(mOnPostListUpdateListener);
        loader.getPosts();
    }

    @Override
    void loadMore(long before) {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_ALL_URL, before);
        loader.setOnPostListLoadMoreListener(mOnPostListLoadMoreListener);
        loader.getPosts();
    }


}
