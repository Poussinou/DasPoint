package im.point.torgash.daspoint.network;

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

import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.point.Authorization;
import im.point.torgash.daspoint.point.PointPost;
import im.point.torgash.daspoint.point.PostList;
import im.point.torgash.daspoint.utils.Constants;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by torgash on 19.02.16.
 */
public class Commentator {
    private CommonRequestCallback mCommonRequestCallback;
    private boolean isCommentingComment=false;
    private String mPostId;
    private String mCommentId;
    private String mCommentText;
    private final int MSG_COMMENT_POSTED = 1;
    private final int MSG_ERROR = 0;
    private Handler networkBackgroundRequestHandler;

    public Commentator(String postId, String commentText, CommonRequestCallback commonRequestCallback) {
        mPostId = postId;
        mCommentText = commentText;
        mCommonRequestCallback = commonRequestCallback;
    }

    public Commentator(String postId, String commentId, String commentText, CommonRequestCallback commonRequestCallback) {
        mPostId = postId;
        mCommentText = commentText;
        mCommentId = commentId;
        mCommonRequestCallback = commonRequestCallback;
        isCommentingComment = true;
    }

    public void postComment() {

        networkBackgroundRequestHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_COMMENT_POSTED:
                        if (null != mCommonRequestCallback) {
                            mCommonRequestCallback.onSuccess((String) msg.obj);
                        }
                        break;
                    case MSG_ERROR:
                        if (null != mCommonRequestCallback) {
                            mCommonRequestCallback.onError((String) msg.obj);
                        }
                        break;
                }
            }


        };

        //this thread tries to download post list
        Thread postComment = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS).writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            @Override
            public void run() {
                final MediaType MEDIA_TYPE_APP
                        = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
                try {
                    Request request;
                    String postBody = "";
                    if (!isCommentingComment) {
                        postBody = ""
                                + "text="
                                + mCommentText;

                    } else {
                        Log.d("DP", "commenting comment "
                                + mPostId + "/" + mCommentId);
                        postBody = ""
                                + "text="
                                + mCommentText
                                + "&comment_id="
                                + mCommentId;

                    }
                    Log.d(Constants.LOG_TAG, "comment request postbody is:");
                    request = new Request.Builder()
                            .url(Constants.POINT_API_COMMENT + mPostId)
                            .header("Authorization", Authorization.getToken())
                            .addHeader("X-CSRF", Authorization.getCSRFToken())
                            .post(RequestBody.create(MEDIA_TYPE_APP, postBody))
                            .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    }
                    String responseString = response.body().string();
                    Log.d("DP", "Comment posting result: " + responseString);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }


                    try {
                        JSONObject postJsonResult = new JSONObject(responseString);

                        if (postJsonResult.has("error")) {
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Error: " + postJsonResult.getString("error"));
                            networkBackgroundRequestHandler.sendMessage(msg);
                        } else {
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_COMMENT_POSTED, 0, 0, "Comment " + postJsonResult.get("id")
                                    + "/" + postJsonResult.getString("comment_id") + " posted");
                            // отправляем
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
        postComment.start();

    }

}
