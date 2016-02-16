package im.point.torgash.daspoint;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private View rootView;
    private int section_number;
    public static PlaceholderFragment[] instance = new PlaceholderFragment[5];
    static Context mContext;

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

                                Log.d("DP", response.body().string());
                                Headers responseHeaders = response.headers();
                                for (int i = 0; i < responseHeaders.size(); i++) {
                                    Log.d("DP", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                                }



                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                    });
                    getRecentThread.start();
                    rootView = inflater.inflate(R.layout.post_list, null);
                    SwipeAdapter adapter = new SwipeAdapter(mContext, postList);
                    ListView lvPostList = (ListView) rootView.findViewById(R.id.lvPostList);
                    lvPostList.setAdapter(adapter);
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
