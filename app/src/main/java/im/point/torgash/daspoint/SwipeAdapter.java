package im.point.torgash.daspoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * Created by torgash on 15.02.16.
 */
public class SwipeAdapter extends BaseSwipeAdapter {
    private ArrayList<Map<String, String>> mPostMap;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private Context mCntx;
    Drawable drwExpandUp;
    Drawable drwExpandDown;
    public SwipeAdapter(Context context, ArrayList<Map<String, String>> postMap) {
        this.mPostMap = postMap;
        this.mCntx = context;
        this.mContext.set(context);

    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipePostListItemPart;
    }

    //ATTENTION: Never bind listener or fill values in generateView.
    //           You have to do that in fillValues method.
    @Override
    public View generateView(int position, ViewGroup parent) {
        return LayoutInflater.from(mContext.get()).inflate(R.layout.post_list_item, null);
    }

    @Override
    public void fillValues(int position, View convertView) {
        final ImageView ivExpandToggle = (ImageView) convertView.findViewById(R.id.expand_collapse);
        String authorNick = mPostMap.get(position).get("authorNick");
        String postDate = mPostMap.get(position).get("postDate");
        String postId = mPostMap.get(position).get("postId");
        String postText = "";


            postText = postText.concat("There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form. ");



        TextView tvAuthorNick = (TextView) convertView.findViewById(R.id.tvPostAuthorNick);
        TextView tvPostDate = (TextView) convertView.findViewById(R.id.tvPostDate);
        TextView tvPostId = (TextView) convertView.findViewById(R.id.tvPostId);
        final TextView tvPostText = (TextView) convertView.findViewById(R.id.expandable_text);
        tvAuthorNick.setText(authorNick);
        tvPostDate.setText(postDate);
        tvPostId.setText(postId);
        tvPostText.setText(postText);
        drwExpandDown = ContextCompat.getDrawable(mCntx, R.drawable.chevron_down);
        drwExpandUp = ContextCompat.getDrawable(mCntx, R.drawable.chevron_up);
        if(tvPostText.getLineCount() >6){
            tvPostText.setMaxLines(6);
            ivExpandToggle.setImageDrawable(drwExpandDown);
            tvPostText.setOnClickListener(new View.OnClickListener() {
                boolean textExpanded = false;
                @Override
                public void onClick(View view) {
                    if(!textExpanded) {
                        tvPostText.setMaxLines(tvPostText.getLineCount());
                        textExpanded = true;
                        ivExpandToggle.setImageDrawable(drwExpandUp);
                    }
                    else{
                        tvPostText.setMaxLines(6);
                        textExpanded = false;
                        ivExpandToggle.setImageDrawable(drwExpandDown);
                    }
                }
            });
        }
        else {
            ivExpandToggle.setImageDrawable(null);
        }


    }

    @Override
    public int getCount() {
        return mPostMap.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}