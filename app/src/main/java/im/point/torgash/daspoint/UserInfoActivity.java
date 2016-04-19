package im.point.torgash.daspoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {
    public static final String PREFERENCES = "prefs";
    static final String AUTH_TOKEN = "auth_token";
    static final String CSRF_TOKEN = "csrf_token";
    static final int MSG_AVATAR = 1;
    //let's define variables for our views
    NestedScrollView nestedScrollView;
    ImageView ivUserAvatar;
    TextView userInfoAbout;
    LinearLayout userInfoTextGroup;
    ImageButton userInfoSubscribed;
    ImageButton userInfoBlackListed;
    ImageButton userInfoWhiteListed;
    Toolbar toolbar;


    LayoutInflater li;
    static boolean isInFront; //just to ensure that the activity is not suspended
    protected SharedPreferences prefs;
    String token = "";
    String csrf_token = "";


    public void onResume() {
        super.onResume();
        isInFront = true;
    }
    final String MARK_DOWN_MODE = "MARKDOWN_MODE";
    final String ECONOMY_MODE = "ECONOMY_MODE";

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        setContentView(R.layout.activity_user_info);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = getIntent().getStringExtra("login");
        if(null != title) setTitle(title);

        token = prefs.getString("token", "");
        csrf_token = prefs.getString("csrf_token", "");

        li= (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //initializing view variables
        ivUserAvatar = (ImageView)findViewById(R.id.userInfoIvAvatar);
        userInfoAbout = (TextView) findViewById(R.id.userInfoTvAbout);
        userInfoTextGroup = (LinearLayout) findViewById(R.id.userInfoTextGroup);
        userInfoSubscribed = (ImageButton) findViewById(R.id.userInfoBtnSubscribe);
        userInfoBlackListed = (ImageButton) findViewById(R.id.userInfoBtnBlackList);
        userInfoWhiteListed = (ImageButton) findViewById(R.id.userInfoBtnWhiteList);
        findUserInfo(title);

    }
    public void findUserInfo(final String login){
        final int MSG_ERROR = 0;
        final Handler h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_AVATAR:
                        if (UserInfoActivity.isInFront){
                            JSONObject jsonUserInfo = (JSONObject) msg.obj;
                            try {
                                ImageLoader.getInstance().displayImage("http://i.point.im/a/280/" + jsonUserInfo.getString("avatar"), ivUserAvatar);

                                if (jsonUserInfo.getBoolean("subscribed")) {

                                    userInfoSubscribed.setBackgroundResource(R.drawable.circle_green);
                                } else {
                                    userInfoSubscribed.setBackgroundResource(R.drawable.circle_grey);
                                }


                                userInfoTextGroup.removeAllViews();
                                if (!jsonUserInfo.getString("name").equals("")) {

                                    userInfoTextGroup.addView(createTextView(jsonUserInfo.getString("name")));
                                }
                                if (!jsonUserInfo.getString("location").equals("")) {

                                    userInfoTextGroup.addView(createTextView("Место: " + jsonUserInfo.getString("location")));
                                }
                                if (!jsonUserInfo.getString("about").equals("")) {

                                    userInfoTextGroup.addView(createTextView("О себе:\n" + jsonUserInfo.getString("about")));
                                }
                                if (!jsonUserInfo.getString("homepage").equals("")) {

                                    userInfoTextGroup.addView(createTextView("Сайт: " + jsonUserInfo.getString("homepage")));
                                }
                                if (!jsonUserInfo.getString("created").equals("")) {
                                    String time = jsonUserInfo.get("created").toString();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    Date qdate = new Date();
                                    try {
                                        qdate = sdf.parse(time);
                                    } catch (ParseException e) {
                                        Log.d("JSON", "Date parsing exception.", e);
                                    }
                                    SimpleDateFormat sdfout = new SimpleDateFormat("dd MMMM yyyy");
                                    String created = "Зарегистрирован: " + sdfout.format(qdate);
                                    userInfoTextGroup.addView(createTextView(created));
                                }
                                if (!jsonUserInfo.get("gender").toString().equals("null")) {
                                    if(jsonUserInfo.getBoolean("gender")){

                                        userInfoTextGroup.addView(createTextView("Пол: человек"));
                                    }else{
                                        userInfoTextGroup.addView(createTextView("Пол: женщина"));
                                    }

                                }else{
                                    userInfoTextGroup.addView(createTextView("Пол: бот"));
                                }
                                if (!jsonUserInfo.getString("xmpp").equals("")) {

                                    userInfoTextGroup.addView(createTextView("XMPP: " + jsonUserInfo.getString("xmpp")));
                                }
                                if (!jsonUserInfo.getString("email").equals("")) {

                                    userInfoTextGroup.addView(createTextView("Email: " + jsonUserInfo.getString("email")));
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            tvUserName.setText(prefs.getString("user_name", ""));
                        }

                        break;
                    case MSG_ERROR:
                        if(isInFront)Snackbar.make(toolbar, msg.obj.toString(), Snackbar.LENGTH_LONG)
                                .setAction("Discard", null).show();
                        userInfoAbout.setText("Загрузка не удалась");
                        break;


                }
            }

            ;
        };

        Thread getUserInfo = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient.Builder().readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS).writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                    .build();

            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url("http://point.im/api/user/" + login)
                            .header("Authorization", token).header("csrf_token", csrf_token)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String responseString = response.body().string();
                    JSONObject jsonUserInfo = new JSONObject(responseString);
                    Log.d("DP", "User info request status: " + responseString);
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }



                    Message msg = h.obtainMessage(MSG_AVATAR, 0, 0, jsonUserInfo);
                    // отправляем
                    h.sendMessage(msg);


                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = h.obtainMessage(MSG_ERROR, "Network not available.");
                    h.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Message msg = h.obtainMessage(MSG_ERROR, "Торгаш - хуй и не может в JSON");
                    h.sendMessage(msg);
                }
            }
        });
        getUserInfo.start();
    }

    //returns new View with a TextView inside to represent some info
    private View createTextView(String text) {
        View v = li.inflate(R.layout.text_view, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView tv = (TextView) v.findViewById(R.id.post_text_view);
        tv.setText(text);
        tv.setLinksClickable(true);
        tv.setAutoLinkMask(Linkify.ALL);

        return v;
    }
}
