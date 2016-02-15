package im.point.torgash.daspoint;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Boss on 15.02.2016.
 */
public class PlaceholderFragment extends Fragment{
    public static final int RECENT = 0;
    public static final int BLOG = 1;
    public static final int COMMENTS = 2;
    public static final int ALL = 3;
    public static final String SECTION_NUMBER = "section_number";
    private View rootView;
    private int section_number;
    public static PlaceholderFragment[] instance = new PlaceholderFragment[5];

    public PlaceholderFragment () {

    }
    public static PlaceholderFragment getInstance(int n, Context context) {

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
        rootView = inflater.inflate(R.layout.placeholder_list_fragment, null);
        TextView tvSectionNumber = (TextView) rootView.findViewById(R.id.tvSectionNumber);
        try {
            section_number = getArguments().getInt(SECTION_NUMBER);
            switch (section_number) {
                case RECENT:
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
        switch (section_number) {
            case 0:
                tvSectionNumber.setText("Section Recent was chosen");
                break;
            case 1:
                tvSectionNumber.setText("Section Blog was chosen");
                break;
        }
        return rootView;
    }
}
