package com.junyenhuang.birdhouse.carol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.JsonParser;
import com.junyenhuang.birdhouse.LogActivity;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.adapters.ElementGridAdapter;
import com.junyenhuang.birdhouse.database.MainDatabase;
import com.junyenhuang.birdhouse.items.Element;
import com.junyenhuang.birdhouse.items.House;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HouseFragment extends Fragment {
    private static final String TAG = HouseFragment.class.getSimpleName();
    private static final String HOUSE_ID = "house_id";

    private int screenWidth;
    private int screenHeight;
    private static House mHouse;
    private TextView textView1;
    private GridView elementsGrid;
    private static Context mContext;

    public static Fragment newInstance(Context context, House house) {
        Bundle b = new Bundle();
        b.putInt(HOUSE_ID, house.getId());
        //b.putFloat(SCALE, scale);
        mHouse = house;
        mContext = context;
        return Fragment.instantiate(context, HouseFragment.class.getName(), b);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWidthAndHeight();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        final int id = this.getArguments().getInt(HOUSE_ID);
        final RelativeLayout linearLayout = (RelativeLayout) inflater.inflate(R.layout.carol_fragment_overview, container, false);

        new AsyncTask<Integer, RelativeLayout, ArrayList<Element>>() {
            MainDatabase db = new MainDatabase(getActivity());
            House house1;
            int threshold = 0;
            @Override
            protected ArrayList<Element> doInBackground(Integer... params) {
                house1 = db.getHouse(params[0]);
                JsonParser parser = new JsonParser(getActivity());
                SettingGroup settings = parser.parseSettings(house1.getSettingString(), params[0], false);
                threshold = settings.getNh3Limit();
                return parser.parseElements(house1.getElementString());
            }

            @Override
            protected void onPostExecute(final ArrayList<Element> s) {
                super.onPostExecute(s);
                db.close();

                //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 2, screenHeight / 2);
                RelativeLayout relLayout = (RelativeLayout)linearLayout.findViewById(R.id.rel_layout);

                textView1 = (TextView) relLayout.findViewById(R.id.ctext1);
                TextView textView4 = (TextView) relLayout.findViewById(R.id.ctext4);

                //textView1.setLayoutParams(layoutParams);
                textView1.setText(house1.getName());
                textView4.setText(house1.getName());

                //TextView textView2 = (TextView)relLayout.findViewById(R.id.ctext2);
                //TextView textView3 = (TextView)relLayout.findViewById(R.id.ctext3);
                ImageView onlineView = (ImageView)relLayout.findViewById(R.id.onlineImage);
                if(house1.isOnline()) {
                    Picasso.with(mContext).load(R.drawable.online).into(onlineView);
                    //onlineView.setImageDrawable(onlineImage);
                    //onlineView.setImageResource(R.drawable.online);
                    /*
                    textView2.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent1));
                    textView2.setText(R.string.online);
                    textView3.setVisibility(View.VISIBLE);
                    textView3.setText(R.string.online);
                    */
                    //getActivity().sendBroadcast(new Intent("com.jun.houseOnline"));
                } else {
                    Picasso.with(mContext).load(R.drawable.offline).into(onlineView);
                    //onlineView.setImageResource(R.drawable.offline);
                    //onlineView.setImageDrawable(offlineImage);
                    /*
                    textView2.setTextColor(ContextCompat.getColor(mContext, android.R.color.holo_red_dark));
                    textView2.setText(R.string.offline);
                    //textView3.setText(R.string.offline);
                    textView3.setVisibility(View.INVISIBLE);
                    */
                    //getActivity().sendBroadcast(new Intent("com.jun.houseOffline"));
                    return;
                }
                elementsGrid = (GridView) relLayout.findViewById(R.id.element_grid);
                elementsGrid.setAdapter(new ElementGridAdapter(s, getContext(), id, threshold));
                elementsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), LogActivity.class);
                        //Intent intent = new Intent(getActivity(), LogTypeActivity.class);
                        intent.putExtra(Constants.JSON_ID, house1.getId());
                        intent.putExtra(Constants.JSON_NAME, house1.getName());
                        if(id == 6 || id == 7 || id == 8) {
                            intent.putExtra("ELEMENT_ID", 6);
                            intent.putExtra("FILTER", s.get(position).getUnit());
                        } else if(id == 1) {
                            intent.putExtra("ELEMENT_ID", 1);
                            intent.putExtra("FILTER", "1");
                        } else if(id == 2) {
                            intent.putExtra("ELEMENT_ID", 1);
                            intent.putExtra("FILTER", "2");
                        } else {
                            intent.putExtra("ELEMENT_ID", (int) id);
                        }
                        startActivity(intent);
                        getActivity().overridePendingTransition(0, 0);
                        //getActivity().finish();
                    }
                });
            }
        }.execute(id);
        return linearLayout;
    }

    /**
     * Get device screen width and height
     */
    private void getWidthAndHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
    }
}