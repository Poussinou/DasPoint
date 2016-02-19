package im.point.torgash.daspoint;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import im.point.torgash.daspoint.adapters.PostListAdapter;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Boss on 15.02.2016.
 */
public class PlaceholderFragment extends Fragment {
    SharedPreferences prefs;
    private String token;
    private String csrf_token;
    public static final int RECENT = 0;
    public static final int BLOG = 1;
    public static final int COMMENTS = 2;
    public static final int ALL = 3;
    public static final String SECTION_NUMBER = "section_number";
    public static final int MSG_POSTLIST = 256;
    private View rootView;
    private int section_number;
    public static PlaceholderFragment[] instance = new PlaceholderFragment[5];
    static Context mContext;
    Handler h;
    private ArrayList<PointRecent> postArrayList;

    public PlaceholderFragment() {

    }

    public static PlaceholderFragment getInstance(int n, Context context) {
        mContext = context;
        try {
            if (null == instance[n]) {
                instance[n] = new PlaceholderFragment();
                Bundle args = new Bundle();
                args.putInt(SECTION_NUMBER, n);
                instance[n].setArguments(args);
                return instance[n];
            }
            return instance[n];
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Exception: " + e, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        csrf_token = prefs.getString("csrf_token", "");
        View rootView = null;
        ArrayList<Map<String, String>> postList = new ArrayList<>();
        rootView = inflater.inflate(R.layout.post_list, null);

        final RecyclerView rvPostList = (RecyclerView) rootView.findViewById(R.id.postList);
        rvPostList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));

        //handler for recentlist to get out of the thread
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_POSTLIST:
                        if(((MainActivity)getActivity()).isInFront()){
                            postArrayList = new ArrayList<>();
                            try {
                                JSONObject postJsonInitialList = new JSONObject(msg.obj.toString());
                                JSONArray postJsonList = postJsonInitialList.getJSONArray("posts");
                                for (int i = 0; i < postJsonList.length(); i++) {
                                    PointRecent tempPostObject = new PointRecent(postJsonList.getJSONObject(i));
                                    Log.d("DP", "Created post object: " + tempPostObject);
                                    postArrayList.add(tempPostObject);
                                }
                                PostListAdapter adapter = new PostListAdapter(mContext, postArrayList);

                                rvPostList.setAdapter(adapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                        break;

                }
            };
        };



        for (int i = 0; i < 20; i++) {

            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("authorNick", "@Veresk");
            tempMap.put("postDate", "2016/02/07 в 12:00");
            tempMap.put("postId", String.valueOf(new Random().nextInt(20215)));
            tempMap.put("postText", getString(R.string.lorem));
            postList.add(tempMap);
        }
        try {
            section_number = getArguments().getInt(SECTION_NUMBER);
            switch (section_number) {
                case RECENT:
                    Thread getRecentThread = new Thread(new Runnable() {

                        final OkHttpClient client = new OkHttpClient();

                        @Override
                        public void run() {
                            try {
                                Request request = new Request.Builder()
                                        .url("http://point.im/api/recent").header("Authorization", token).header("csrf_token", csrf_token)
                                        .build();

                                Response response = client.newCall(request).execute();
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }
                                String responseString = response.body().string();
                                Log.d("DP", responseString);
                                Headers responseHeaders = response.headers();
                                for (int i = 0; i < responseHeaders.size(); i++) {
                                    Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                                }
                                Message msg = h.obtainMessage(MSG_POSTLIST, 0, 0, responseString);
                                // отправляем
                                h.sendMessage(msg);


                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                    });
                    getRecentThread.start();

                    break;
                case BLOG:
                    break;
                case COMMENTS:
                    break;
                case ALL:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return rootView;
    }
}
