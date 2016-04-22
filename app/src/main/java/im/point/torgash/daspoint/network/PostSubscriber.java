package im.point.torgash.daspoint.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import im.point.torgash.daspoint.listeners.CommonRequestCallback;
import im.point.torgash.daspoint.point.Authorization;
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
public class PostSubscriber {
    private CommonRequestCallback mCommonRequestCallback;

    private String mPostId;
    private boolean mSubscribe; //defines if we should subscribe (true) or unsubscribe (false)
    private final int MSG_SUBSCRIBED = 1;
    private final int MSG_UNSUBSCRIBED = 2;
    private final int MSG_ERROR = 0;
    private Handler networkBackgroundRequestHandler;

    public PostSubscriber(String postId, CommonRequestCallback commonRequestCallback, boolean subscribe) {
        mPostId = postId;
        mSubscribe = subscribe;
        mCommonRequestCallback = commonRequestCallback;
    }

    public void toggleSubscription(){
        if (mSubscribe) {
            subscribe();
        }
        else{
            unsubscribe();

        }
    }

    private void unsubscribe() {

        networkBackgroundRequestHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SUBSCRIBED:
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
        Thread unsubscribeThread = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS).writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            @Override
            public void run() {
                final MediaType MEDIA_TYPE_APP
                        = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
                try {
                    Request request;


                    request = new Request.Builder()
                            .url(Constants.POST_OPERATIONS + mPostId + "/s")
                            .header("Authorization", Authorization.getToken())
                            .addHeader("X-CSRF", Authorization.getCSRFToken())
                            .delete()
                            .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    }
                    String responseString = response.body().string();
                    Log.d("DP", "Unsubscription result: " + responseString);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    if (responseString.equals("true")) {
                        Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_SUBSCRIBED, 0, 0, "You have subscribed to " + mPostId);
                        // отправляем
                        networkBackgroundRequestHandler.sendMessage(msg);

                    }else {

                        try {
                            JSONObject subscribeJsonResult = new JSONObject(responseString);

                            if (subscribeJsonResult.has("error")) {
                                Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Error: " + subscribeJsonResult.getString("error"));
                                networkBackgroundRequestHandler.sendMessage(msg);
                            } else {
                                Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_SUBSCRIBED, 0, 0, "You have unsubscribed from #" + mPostId);
                                // отправляем
                                networkBackgroundRequestHandler.sendMessage(msg);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //we got completely wrong message, let's notify developer
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Make screenshot \n @ \n Send it to developer \n " + e.toString());
                            networkBackgroundRequestHandler.sendMessage(msg);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Network not available.");
                    networkBackgroundRequestHandler.sendMessage(msg);

                }
            }
        });
        unsubscribeThread.start();

    }


    public void subscribe() {

        networkBackgroundRequestHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SUBSCRIBED:
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
        Thread subscribeThread = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS).writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .build();
            @Override
            public void run() {
                final MediaType MEDIA_TYPE_APP
                        = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
                try {
                    Request request;


                    request = new Request.Builder()
                            .url(Constants.POST_OPERATIONS + mPostId + "/s")
                            .header("Authorization", Authorization.getToken())
                            .addHeader("X-CSRF", Authorization.getCSRFToken())
                            .post(RequestBody.create(MEDIA_TYPE_APP, ""))
                            .build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    }
                    String responseString = response.body().string();
                    Log.d("DP", "Subscription result: " + responseString);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    if (responseString.equals("true")) {
                        Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_SUBSCRIBED, 0, 0, "You have subscribed to #" + mPostId);
                        // отправляем
                        networkBackgroundRequestHandler.sendMessage(msg);

                    }else{

                        try {
                            JSONObject subscribeJsonResult = new JSONObject(responseString);

                            if (subscribeJsonResult.has("error")) {
                                Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Error: " + subscribeJsonResult.getString("error"));
                                networkBackgroundRequestHandler.sendMessage(msg);
                            } else if(subscribeJsonResult.has("ok") && subscribeJsonResult.getBoolean("ok")){
                                Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_SUBSCRIBED, 0, 0, "You have subscribed to " + mPostId);
                                // отправляем
                                networkBackgroundRequestHandler.sendMessage(msg);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            //we got completely wrong message, let's notify developer
                            Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Make screenshot \n @ \n Send it to developer \n " + e.toString());
                            networkBackgroundRequestHandler.sendMessage(msg);
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = networkBackgroundRequestHandler.obtainMessage(MSG_ERROR, "Network not available.");
                    networkBackgroundRequestHandler.sendMessage(msg);

                }
            }
        });
        subscribeThread.start();

    }

}
