package im.point.torgash.daspoint.listeners;

import android.text.Spannable;

/**
 * Created by Admin on 21.02.2016.
 */
public interface HtmlSpannerRequestCallback extends BasicNetworkListener{
    public void onSuccess(Spannable html);
}
