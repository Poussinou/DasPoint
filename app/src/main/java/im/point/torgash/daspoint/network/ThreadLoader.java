package im.point.torgash.daspoint.network;

import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import im.point.torgash.daspoint.listeners.OnThreadUpdateListener;
import im.point.torgash.daspoint.point.Authorization;
import im.point.torgash.daspoint.point.Comment;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PointThread;
import im.point.torgash.daspoint.point.PostList;
import im.point.torgash.daspoint.point.ThreadHeaderPost;
import im.point.torgash.daspoint.utils.Constants;
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
public class ThreadLoader {


    String mApiURL;

    private OnThreadUpdateListener mOnThreadUpdateListener;
    private Handler networkBackgroundRequestHandler;
    private final int MSG_POSTLIST_READY = 1;
    private final int MSG_ERROR = 0;

    public ThreadLoader(String apiURL) {
        mApiURL = apiURL;

    }


    public void getPosts() {
        networkBackgroundRequestHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_POSTLIST_READY:

                            if (null != mOnThreadUpdateListener) {
                                mOnThreadUpdateListener.onThreadUpdated((PointThread) msg.obj);
                            }


                        break;
                    case MSG_ERROR:

                            if (null != mOnThreadUpdateListener) {
                                mOnThreadUpdateListener.onError((String) msg.obj);
                            }


                        break;
                }
            }

            ;
        };

        //this thread tries to download post list
        Thread getThread = new Thread(new Runnable() {
            final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS).writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .build();

            @Override
            public void run() {
                try {
                    Request request;


                    request = new Request.Builder()
                            .url(mApiURL)
                            .header("Authorization", Authorization.getToken())
                            .header("csrf_token", Authorization.getCSRFToken())
                            .build();


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

                    PointThread pointThread = new PointThread();


                    try {
                        JSONObject postJsonInitialList = new JSONObject(responseString);

                        if (postJsonInitialList.has("comments")) {
                            JSONArray commentsArray = postJsonInitialList.getJSONArray("comments");
                            pointThread.comments = new ArrayList<>();
                            for (int i = 0; i < commentsArray.length(); i++) {
                                Comment comment = new Comment(commentsArray.getJSONObject(i));
                                Log.d("DP", "Created post object: " + comment);
                                pointThread.comments.add(comment);
                            }
                            rearrange((ArrayList<Comment>)pointThread.comments);
                            pointThread.post = new ThreadHeaderPost(postJsonInitialList.getJSONObject("post"));
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_POSTLIST_READY, 0, 0, pointThread);
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
                    Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Network not available.");
                    networkBackgroundRequestHandler.sendMessage(msg);

                }
            }
        });
        getThread.start();
    }

    public void setOnThreadUpdateListener(OnThreadUpdateListener listener) {
        mOnThreadUpdateListener = listener;
    }
    private ArrayList<Comment> rearrange(ArrayList<Comment> list) {
        for (int i = 0; i < list.size(); i++) {
            int offset = 1;
            Comment commentToCompareWith = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                Comment commentCompared = list.get(j);
                if (!commentCompared.to_comment_id.equals("null") && commentCompared.to_comment_id.equals(commentToCompareWith.id)) {
                    list.remove(j);
                    commentCompared.quote = commentToCompareWith.authorName + ":\n" + commentToCompareWith.text.substring(0, Math.min(80, commentToCompareWith.text.length())) + "...";
                    Log.d(Constants.LOG_TAG, "Comment's quote assigned: " + commentCompared.quote);
                    commentCompared.offset = commentToCompareWith.offset + 1;
                    list.add(i + offset++, commentCompared);
                }
            }
        }
        return list;
    }

}
