package im.point.torgash.daspoint;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {
    public static final String PREFERENCES = "prefs";
    static final String AUTH_TOKEN = "auth_token";
    static final String CSRF_TOKEN = "csrf_token";
    static final int MSG_AVATAR = 1;
    NestedScrollView nestedScrollView;
    ImageView ivUserAvatar;
    static boolean isInFront;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = getIntent().getStringExtra("login");
        if(null != title) setTitle(title);

        token = prefs.getString("token", "");
        csrf_token = prefs.getString("csrf_token", "");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ivUserAvatar = (ImageView)findViewById(R.id.userInfoIvAvatar);
        findUserInfo(title);

    }
    public void findUserInfo(final String login){
        final Handler h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_AVATAR:
                        if (UserInfoActivity.isInFront){

                            ImageLoader.getInstance().displayImage("http://i.point.im/a/280/" + msg.obj.toString(), ivUserAvatar);
//                            tvUserName.setText(prefs.getString("user_name", ""));
                        }

                        break;

                }
            }

            ;
        };

        Thread getUserInfo = new Thread(new Runnable() {

            final OkHttpClient client = new OkHttpClient();

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

                    String userAvatar = jsonUserInfo.getString("avatar");
                    String userName = jsonUserInfo.getString("name");
                    //writing avatar link to prefs

                    Message msg = h.obtainMessage(MSG_AVATAR, 0, 0, userAvatar);
                    // отправляем
                    h.sendMessage(msg);


                } catch (IOException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getUserInfo.start();
    }
}
