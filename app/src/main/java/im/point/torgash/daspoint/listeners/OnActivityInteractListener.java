package im.point.torgash.daspoint.listeners;

import android.content.Intent;

/**
 * Created by torgash on 20.02.16.
 */
public interface OnActivityInteractListener {
    void onErrorShow(String error);

    void onIntentStart(Intent intent);

    void onTheadOpen(String postId);

    void hideFAB();

    void showFAB();
}
