package im.point.torgash.daspoint.fragments;

import im.point.torgash.daspoint.network.PostsLoader;
import im.point.torgash.daspoint.utils.Constants;

/**
 * Created by torgash on 19.02.16.
 */
public class BlogPostListFragment extends BasePostListFragment {
    static BlogPostListFragment instance;
    public BlogPostListFragment(){

    }
    public static BlogPostListFragment getInstance(){
        if(null == instance){
            return new BlogPostListFragment();
        }
        else return instance;
    }

    @Override
    void loadPosts() {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_BLOG_URL);
        loader.setOnPostListUpdateListener(mOnPostListUpdateListener);
        loader.getPosts();
    }

    @Override
    void loadMore(long before) {

        PostsLoader loader = new PostsLoader(Constants.POINT_API_BLOG_URL, before);
        loader.setOnPostListLoadMoreListener(mOnPostListLoadMoreListener);
        loader.getPosts();
    }


}
