package im.point.torgash.daspoint.listeners;

import im.point.torgash.daspoint.point.PointThread;

/**
 * Created by torgash on 17.03.16.
 */
public interface OnThreadUpdateListener extends BasicNetworkListener {
    public void onThreadUpdated(PointThread thread);
}
