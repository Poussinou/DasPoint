package im.point.torgash.daspoint.listeners;

import android.content.Intent;

/**
 * Created by torgash on 20.02.16.
 */
public interface OnErrorShowInSnackbarListener {
    void onErrorShow(String error);

    void onIntentStart(Intent intent);
}
