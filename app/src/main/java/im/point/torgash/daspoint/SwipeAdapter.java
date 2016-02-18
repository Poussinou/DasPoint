package im.point.torgash.daspoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.markdown4j.Markdown4jProcessor;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import de.hdodenhof.circleimageview.CircleImageView;
import im.point.torgash.daspoint.im.point.torgash.daspoint.utils.URLImageParser;

/**
 * Created by torgash on 15.02.16.
 */
public class SwipeAdapter extends BaseSwipeAdapter {

    private ArrayList<PointRecent> mPostArray;
    private final ThreadLocal<Context> mContext = new ThreadLocal<>();
    private Context mCntx;
    Drawable drwExpandUp;
    Drawable drwExpandDown;

    //constructor for ArrayList<PointRecent>
    public SwipeAdapter(Context context, ArrayList<PointRecent> postArray) {
        this.mPostArray = postArray;
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
        PointRecent currentPost = mPostArray.get(position);
        String authorNick = currentPost.authorLogin;
        String postDate = currentPost.postCreatedString;
        String postId = currentPost.postId;
        String postText = currentPost.postText;
        String authorAvatar = currentPost.authorAvatar;
        int commentsCount = currentPost.commentsCount;

        CircleImageView civPostAuthorAvatar = (CircleImageView) convertView.findViewById(R.id.civPostAuthorAvatar);
        TextView tvAuthorNick = (TextView) convertView.findViewById(R.id.tvPostAuthorNick);
        TextView tvPostDate = (TextView) convertView.findViewById(R.id.tvPostDate);
        TextView tvPostId = (TextView) convertView.findViewById(R.id.tvPostId);
        final TextView tvPostText = (TextView) convertView.findViewById(R.id.expandable_text);
        TextView tvCommentsCount = (TextView) convertView.findViewById(R.id.post_comments_count);
        tvAuthorNick.setText(authorNick);
        tvPostDate.setText(postDate);
        tvPostId.setText(postId);

        try {
            URLImageParser p = new URLImageParser(tvPostText, mCntx);
            Spanned htmlSpan = Html.fromHtml(convertLinksToHtmlTags(new Markdown4jProcessor().process(postText)), p, null);
            tvPostText.setText(htmlSpan);
            Log.d("DP", "text = \n" + convertLinksToHtmlTags(new Markdown4jProcessor().process(postText)));
        } catch (IOException e) {
            tvPostText.setText(postText);
            Log.d("DP", "couldn't process markdown for post #" + tvPostId);
        }
        tvPostText.setMovementMethod(LinkMovementMethod.getInstance());
        tvCommentsCount.setText(String.valueOf(commentsCount));
        tvPostText.setMaxLines(8);
//        tvPostText.setOnClickListener(null);

        tvPostText.scrollTo(0, 0);
        tvPostText.setMovementMethod(null);
        ImageLoader.getInstance().displayImage("http://i.point.im/a/40/" + authorAvatar, civPostAuthorAvatar);
        drwExpandDown = ContextCompat.getDrawable(mCntx, R.drawable.chevron_down);
        drwExpandUp = ContextCompat.getDrawable(mCntx, R.drawable.chevron_up);
        ivExpandToggle.setImageDrawable(null);
        if(tvPostText.getLineCount() >8){

            ivExpandToggle.setImageDrawable(drwExpandDown);
            ivExpandToggle.setOnClickListener(new View.OnClickListener() {
                boolean textExpanded = false;
                @Override
                public void onClick(View view) {
                    if(!textExpanded) {
                        tvPostText.setMaxLines(tvPostText.getLineCount());
                        textExpanded = true;
                        ivExpandToggle.setImageDrawable(drwExpandUp);
                    }
                    else{
                        tvPostText.setMaxLines(8);
                        textExpanded = false;
                        ivExpandToggle.setImageDrawable(drwExpandDown);
                    }
                }
            });
        }
        else {
            ivExpandToggle.setImageDrawable(null);
        }

        SwipeLayout swipeLayout =  (SwipeLayout)convertView.findViewById(R.id.swipePostListItemPart);

//set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, convertView.findViewById(R.id.bottom_wrapper));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, convertView.findViewById(R.id.bottom_wrapper_right));
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });

        EditText etQComment = (EditText) convertView.findViewById(R.id.qcomment_text);
        ImageButton ibQComment = (ImageButton) convertView.findViewById(R.id.qcomment_button);
        ibQComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

    @Override
    public int getCount() {
        return mPostArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String convertLinksToHtmlTags (String text) {
        String htmlText = text;
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            if (url.contains(".jpg") || url.contains(".png")|| url.contains(".gif")) {
                htmlText = text.replace(url, url + "<br  /><img src=\"" + url + "\"><br  />");
            }

        }
        return htmlText;
    }

}