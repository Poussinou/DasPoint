package im.point.torgash.daspoint.listeners;

import im.point.torgash.daspoint.point.PostList;

/**
 * Created by torgash on 19.02.16.
 */
public interface OnPostListUpdateListener extends BasicNetworkListener{
    void onPostListUpdated(PostList postList);
}
