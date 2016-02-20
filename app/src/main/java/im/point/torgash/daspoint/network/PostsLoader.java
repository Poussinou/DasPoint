package im.point.torgash.daspoint.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import im.point.torgash.daspoint.listeners.OnPostListLoadMoreListener;
import im.point.torgash.daspoint.listeners.OnPostListUpdateListener;
import im.point.torgash.daspoint.point.Authorization;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class contains methods for async loading post lists from point.im api<br />
 * After creating PostsLoader instance, ALWAYS set one of listeners available to it
 * via set....Listener()<br />
 * Otherwise, it will do its background work and won't return anything to the UI.
 * <p>
 * Created by torgash on 19.02.16.
 */
public class PostsLoader {


    String mApiURL;
    long mBefore;
    boolean isLoadingMore = false;
    private OnPostListUpdateListener mOnPostListUpdateListener;
    private OnPostListLoadMoreListener mOnPostListLoadMoreListener;
    private Handler networkBackgroundRequestHandler;
    private final int MSG_POSTLIST_READY = 1;
    private final int MSG_ERROR = 0;

    public PostsLoader(String apiURL) {
        mApiURL = apiURL;

    }

    public PostsLoader(String apiURL, long before) {
        mApiURL = apiURL;
        mBefore = before;
        isLoadingMore = true;
    }

    public void getPosts() {
        networkBackgroundRequestHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_POSTLIST_READY:
                        if (!isLoadingMore) {
                            if (null != mOnPostListUpdateListener) {
                                mOnPostListUpdateListener.onPostListUpdated((PostList) msg.obj);
                            }

                        } else if (null != mOnPostListLoadMoreListener) {
                            mOnPostListLoadMoreListener.onPostListLoadMore((PostList) msg.obj);
                        }

                        break;
                    case MSG_ERROR:
                        if (!isLoadingMore) {
                            if (null != mOnPostListUpdateListener) {
                                mOnPostListUpdateListener.onError((String) msg.obj);
                            }

                        } else if (null != mOnPostListLoadMoreListener) {
                            mOnPostListLoadMoreListener.onError((String) msg.obj);
                        }
                        break;
                }
            }

            ;
        };

        //this thread tries to download post list
        Thread getPostList = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient();

            @Override
            public void run() {
                try {
                    Request request;
                    if (!isLoadingMore) {

                        request = new Request.Builder()
                                .url(mApiURL)
                                .header("Authorization", Authorization.getToken())
                                .header("csrf_token", Authorization.getCSRFToken())
                                .build();

                    } else {
                        request = new Request.Builder()
                                .url(mApiURL)
                                .header("Authorization", Authorization.getToken())
                                .header("csrf_token", Authorization.getCSRFToken())
                                .header("before", String.valueOf(mBefore))
                                .build();
                    }

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    }
                    String responseString = response.body().string();
                    Log.d("DP", responseString);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    PostList postList = new PostList();
                    try {
                        JSONObject postJsonInitialList = new JSONObject(responseString);
                        if (postJsonInitialList.has("has_next")) {
                            postList.has_next = postJsonInitialList.getBoolean("has_next");
                        }
                        if (postJsonInitialList.has("posts")) {
                            JSONArray postJsonList = postJsonInitialList.getJSONArray("posts");
                            List<PointPost> tempPostList = postList.posts;
                            for (int i = 0; i < postJsonList.length(); i++) {
                                PointPost tempPostObject = new PointPost(postJsonList.getJSONObject(i));
                                Log.d("DP", "Created post object: " + tempPostObject);
                                tempPostList.add(tempPostObject);
                            }
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_POSTLIST_READY, 0, 0, postList);
                            // отправляем
                            networkBackgroundRequestHandler.sendMessage(msg);

                        } else {
                            //we got wrong JSON, let's do something with it and send appropriate message
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, postJsonInitialList.toString());
                            networkBackgroundRequestHandler.sendMessage(msg);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        //we got completely wrong message, let's notify developer
                        Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Make screenshot \n @ \n Send it to developer \n " + e.toString());
                        networkBackgroundRequestHandler.sendMessage(msg);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, e.toString());
                    networkBackgroundRequestHandler.sendMessage(msg);

                }
            }
        });
        getPostList.start();
    }

    public void setmOnPostListUpdateListener(OnPostListUpdateListener listener) {
        mOnPostListUpdateListener = listener;
    }

    public void setmOnPostListLoadMoreListener(OnPostListLoadMoreListener listener) {
        mOnPostListLoadMoreListener = listener;
    }

}
