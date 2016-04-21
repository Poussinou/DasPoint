package im.point.torgash.daspoint.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

import im.point.torgash.daspoint.R;

/**
 * Created by torgash on 20.04.16.
 */
public class Utils {
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
}
