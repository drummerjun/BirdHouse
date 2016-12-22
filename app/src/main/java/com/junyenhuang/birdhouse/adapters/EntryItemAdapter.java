package com.junyenhuang.birdhouse.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.items.DynamicGridView;
import com.junyenhuang.birdhouse.items.EntryItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EntryItemAdapter extends RecyclerView.Adapter<EntryItemAdapter.EntryViewHolder> {
    private static final String TAG = EntryItemAdapter.class.getSimpleName();
    private ArrayList<EntryItem> alarmList;
    private Context context;

    public EntryItemAdapter(Context context, ArrayList<EntryItem> alarms) {
        alarmList = alarms;
        this.context = context;
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_notifications, parent, false);
        return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EntryViewHolder holder, final int position) {
        if(alarmList == null) {
            Log.e(TAG + "::onBindViewHolder", "eventList==null");
            return;
        }

        Picasso.with(context).load(R.drawable.warning).into(holder.warningImageView);
        holder.tvHouse.setText(alarmList.get(position).getHouseName());
        holder.tvShadow.setText(alarmList.get(position).getHouseName());
        /*
        //TODO display alarm status of each house
        final int id = alarmList.get(position).getHouseID();
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                MainDatabase db = new MainDatabase(context);
                House house1 = db.getHouse(params[0]);
                db.close();
                String nameString = house1.getName();
                return nameString;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s != null && !s.isEmpty()) {
                    holder.tvHouse.setText(s);
                }
            }
        }.execute(id);
        */

        holder.gridEvents.setAdapter(new AlarmGridAdapter(
                alarmList.get(position).getCriticalEvents(),
                context));
        holder.gridEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Context context = v.getContext();
                Intent intent = new Intent(context, OverviewMainActivity.class);
                intent.putExtra(Constants.JSON_ID, (int)id);
                intent.putExtra(Constants.ENTRY_TAG, TAG);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                ((Activity)context).finish();
                ((Activity)context).overridePendingTransition(0, 0);
                //((Activity)v.getContext()).overridePendingTransition(0, 0);
            }
        });
        holder.gridEvents.setExpanded(true);
    }

    @Override
    public int getItemCount() {
        if(alarmList == null) {
            Log.e(TAG + "::getItemCount", "count==null");
            return 0;
        } else {
            return alarmList.size();
        }
    }

    public void updateData(ArrayList<EntryItem> items) {
        if(items != null) {
            alarmList.clear();
            alarmList.addAll(items);
            notifyDataSetChanged();
        }
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        public final String TAG = EntryViewHolder.class.getSimpleName();
        private TextView tvHouse, tvShadow;
        private DynamicGridView gridEvents;
        private ImageView warningImageView;
        public EntryViewHolder(View v) {
            super(v);
            warningImageView = (ImageView)v.findViewById(R.id.warning);
            tvHouse = (TextView)v.findViewById(R.id.eetext1);
            tvShadow = (TextView)v.findViewById(R.id.eeshadow);
            gridEvents = (DynamicGridView)v.findViewById(R.id.events_grid);
        }
    }
}
