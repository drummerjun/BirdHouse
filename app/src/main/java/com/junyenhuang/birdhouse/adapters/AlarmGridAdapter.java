package com.junyenhuang.birdhouse.adapters;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.items.HouseEvent;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AlarmGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HouseEvent> mEvents;

    public AlarmGridAdapter(ArrayList<HouseEvent> events, Context context) {
        mEvents = events;
        this.context = context;
    }

    public void updateData(ArrayList<HouseEvent> events) {
        if(events != null) {
            mEvents.clear();
            mEvents.addAll(events);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if(mEvents == null) {
            return 0;
        } else {
            return mEvents.size();
        }
    }

    @Override
    public HouseEvent getItem(int position) {
        return mEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mEvents.get(position).getHouseKey();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.card_griditem1, null);
            final HouseEvent e = mEvents.get(position);
            try {
                String timeString = e.getTimeString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date mDate = sdf.parse(timeString);
                int hour = mDate.getHours();
                int minutes = mDate.getMinutes();
                TextView timeTV = (TextView) gridView.findViewById(R.id.cvtext4);
                NumberFormat formatter = new DecimalFormat("00");
                String formatString = formatter.format(hour) + ":" + formatter.format(minutes);
                timeTV.setText(formatString);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            String description = e.getDescription();
            int elementKey = e.getElementKey();
            RelativeLayout relativeLayout = (RelativeLayout)gridView.findViewById(R.id.rel_layout);
            TextView textView = (TextView)gridView.findViewById(R.id.cvtext3);
            ImageView imageView = (ImageView)gridView.findViewById(R.id.icon456);
            textView.setText(description);
            switch (elementKey) {
                case 4:
                    textView.setText(context.getString(R.string.event_intrusion));
                    relativeLayout.setBackgroundColor(
                            ResourcesCompat.getColor(
                                    context.getResources(), R.color.colorTransparentRed, null)
                    );
                    Picasso.with(context).load(R.drawable.theif_on).into(imageView);
                    break;
                case 3:
                    textView.setText(context.getString(R.string.event_entry));
                    Picasso.with(context).load(R.drawable.door_on).into(imageView);
                    relativeLayout.setBackgroundColor(
                            ResourcesCompat.getColor(
                                    context.getResources(), R.color.colorTransparentGreen, null)
                    );
                    break;
                case 5:
                    if(e.getEventValue().equals("OFF")) {
                        textView.setText(context.getString(R.string.event_power_out));
                        Picasso.with(context).load(R.drawable.power_off).into(imageView);
                        //imageView.setImageDrawable(powerOffDrawable);
                    } else if(e.getEventValue().equals("ON")) {
                        textView.setText(context.getString(R.string.event_power_back));
                        Picasso.with(context).load(R.drawable.power_on).into(imageView);
                    }
                    relativeLayout.setBackgroundColor(
                            ResourcesCompat.getColor(
                                    context.getResources(), R.color.colorTransparentRed, null)
                    );
                    break;
                case 6:
                    textView.setText(context.getString(R.string.event_nh3) + ": " + e.getEventValue());
                    Picasso.with(context).load(R.drawable.nh3).into(imageView);
                    relativeLayout.setBackgroundColor(
                            ResourcesCompat.getColor(
                                    context.getResources(), R.color.colorTransparentOrange, null)
                    );
                    break;
                default:
                    textView.setText(e.getDescription() + " " + e.getEventValue());
                    Picasso.with(context).load(R.drawable.warning).into(imageView);
            }
        } else {
            gridView = convertView;
        }
        return gridView;
    }
}
