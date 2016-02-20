package im.point.torgash.daspoint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.point.torgash.daspoint.fragments.BasePostListFragment;
import im.point.torgash.daspoint.fragments.RecentPostListFragment;
import im.point.torgash.daspoint.listeners.OnErrorShowInSnackbarListener;
import im.point.torgash.daspoint.point.Authorization;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static boolean isInFront;

    private OnErrorShowInSnackbarListener mOnErrorShowInSnackbarListener;
    @Override
    public void onResume() {
        super.onResume();
        isInFront = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }

    public static final String PREFERENCES = "prefs";
    static final String AUTH_TOKEN = "auth_token";
    static final String CSRF_TOKEN = "csrf_token";
    static final int MSG_AVATAR = 1;
    SharedPreferences prefs;
    String token = "";
    String csrf_token = "";
    String username;
    Handler h;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //UIL initialization
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()

                .cacheInMemory(true)
                .cacheOnDisk(true)

                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())

                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheSize(100 * 1024 * 1024)
                .memoryCacheSize(16 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(config);


        prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if (!prefs.contains("token")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            token = prefs.getString("token", "");
            csrf_token = prefs.getString("csrf_token", "");
            Authorization.setToken(token);
            Authorization.setCSRFToken(csrf_token);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Let's implement our placeholder fragment here



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        username = prefs.getString("username", "");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(0);
        final ImageView ivUserAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.ivUserAvatar);
        TextView tvUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tvNavUserName);
        tvUserName.setText(username);

        if (!prefs.contains("user_avatar")) {
            h = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case MSG_AVATAR:
                            if (MainActivity.this.isInFront)
                                ImageLoader.getInstance().displayImage("http://i.point.im/a/280/" + msg.obj.toString(), ivUserAvatar);

                            break;

                    }
                }

                ;
            };
            final String user_avatar = "";
            Thread getUserInfo = new Thread(new Runnable() {

                final OkHttpClient client = new OkHttpClient();

                @Override
                public void run() {
                    try {
                        Request request = new Request.Builder()
                                .url("http://point.im/api/user/" + prefs.getString("username", ""))
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

                        //writing avatar link to prefs
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_avatar", userAvatar);
                        editor.apply();
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
        } else {
            ImageLoader.getInstance().displayImage("http://i.point.im/a/280/" + prefs.getString("user_avatar", ""), ivUserAvatar);
        }
        mOnErrorShowInSnackbarListener = new OnErrorShowInSnackbarListener() {
            @Override
            public void onErrorShow(String error) {
                Snackbar.make(fab, error, Snackbar.LENGTH_LONG)
                        .setAction("Discard", null).show();
            }
        };
        RecentPostListFragment.getInstance().setOnErrorShowInSnackbarListener(mOnErrorShowInSnackbarListener);
        getFragmentManager().beginTransaction().replace(R.id.post_list_fragment, RecentPostListFragment.getInstance()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_recent) {
            RecentPostListFragment recentPostListFragment = RecentPostListFragment.getInstance();
            getFragmentManager().beginTransaction().replace(R.id.post_list_fragment, recentPostListFragment).commit();
            // Handle the camera action
        } else if (id == R.id.nav_blog) {

        } else if (id == R.id.nav_comments) {

        } else if (id == R.id.nav_all) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static boolean isInFront() {
        return isInFront;
    }

}
