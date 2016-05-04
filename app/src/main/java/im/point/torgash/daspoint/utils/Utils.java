package im.point.torgash.daspoint.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import im.point.torgash.daspoint.R;
import im.point.torgash.daspoint.listeners.HtmlSpannerRequestCallback;
import im.point.torgash.daspoint.point.Authorization;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by torgash on 20.04.16.
 */
public class Utils {
    public static final int MSG_ERROR = 0;
    public static final int MSG_HTML_PROCESSED = 1;

    public static String extractYoutubeId(String url) throws MalformedURLException{
        URL mUrl = new URL(url);
        String host = mUrl.getHost();
        String id = "";

        if (host.contains("youtube")) {

            String query = mUrl.getQuery();
            String[] param = query.split("&");
            for(String row: param) {
                String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
        }

        if (host.contains("youtu.be")) {
            id = mUrl.getPath().substring(1);
        }

        Log.d(Constants.LOG_TAG, "youtube ID extracted: " + id);
        return id;
    }
    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()

                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnFail(R.mipmap.image_load_failed)
                .showImageOnLoading(R.drawable.timer_sand)

                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)

                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheSize(300 * 1024 * 1024)
                .memoryCacheSize(16 * 1024 * 1024)
                .threadPriority(Thread.MIN_PRIORITY)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static void processHtml(final String htmlText, final HtmlSpannerRequestCallback callback) {
        final Handler handler = new Handler() {


            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_HTML_PROCESSED:
                        if (null != callback) {
                            callback.onSuccess((Spannable) msg.obj);
                        }
                        break;
                    case MSG_ERROR:
                        if (null != callback) {
                            callback.onError((String) msg.obj);
                        }
                        break;
                }
            }


        };

        //this thread tries to download post list
        Thread htmlProcessThread = new Thread(new Runnable() {


            @Override
            public void run() {
                try {
                    HtmlSpanner spanner = new HtmlSpanner();

                    Spannable s = (new HtmlSpanner()).fromHtml(htmlText);
                    Log.d(Constants.LOG_TAG, "Spannable: " + s);
                    Message msg = handler.obtainMessage(MSG_HTML_PROCESSED, 0, 0, s);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage(MSG_ERROR, "Error: " + e.toString());
                    handler.sendMessage(msg);
                }


            }
        });
        htmlProcessThread.start();

    }


}
