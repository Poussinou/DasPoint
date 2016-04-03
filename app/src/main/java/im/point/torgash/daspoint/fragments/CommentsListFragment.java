package im.point.torgash.daspoint.fragments;

import im.point.torgash.daspoint.listeners.OnActivityInteractListener;
import im.point.torgash.daspoint.network.PostsLoader;
import im.point.torgash.daspoint.utils.Constants;

/**
 * Created by torgash on 19.02.16.
 */
public class CommentsListFragment extends BasePostListFragment {
    static CommentsListFragment instance;
    public CommentsListFragment(){

    }
    public static CommentsListFragment getInstance(OnActivityInteractListener listener){
        mOnErrorShowInSnackbarListener  = listener;
        if(null == instance){
            return new CommentsListFragment();
        }
        else return instance;
    }

    @Override
    void loadPosts() {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_COMMENTS_URL);
        loader.setOnPostListUpdateListener(mOnPostListUpdateListener);
        loader.getPosts();
    }

    @Override
    void loadMore(long before) {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_COMMENTS_URL, before);
        loader.setOnPostListLoadMoreListener(mOnPostListLoadMoreListener);
        loader.getPosts();
    }


}
