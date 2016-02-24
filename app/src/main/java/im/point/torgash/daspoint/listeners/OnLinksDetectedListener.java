package im.point.torgash.daspoint.listeners;

import android.view.View;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Admin on 22.02.2016.
 */
public interface OnLinksDetectedListener {
    void onLinksDetected(ArrayList<Map<String,String>> postContents);
}
