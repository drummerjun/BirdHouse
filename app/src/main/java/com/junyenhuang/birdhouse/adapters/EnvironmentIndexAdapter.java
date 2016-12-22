package com.junyenhuang.birdhouse.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.items.EnvironmentIndex;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EnvironmentIndexAdapter extends RecyclerView.Adapter<EnvironmentIndexAdapter.EnvironmentIndexViewHolder> {
    private Context context;
    private ArrayList<EnvironmentIndex> mList;

    public EnvironmentIndexAdapter(ArrayList<EnvironmentIndex> events, Context context) {
        mList = events;
        this.context = context;
    }

    public void updateData(ArrayList<EnvironmentIndex> events) {
        if(events != null) {
            mList.clear();
            mList.addAll(events);
            notifyDataSetChanged();
        }
    }

    @Override
    public EnvironmentIndexViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_eindex, parent, false);
        return new EnvironmentIndexViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EnvironmentIndexViewHolder holder, final int position) {
        if(mList == null) {
            Log.e("onBindViewHolder", "mList==null");
            return;
        }
        final EnvironmentIndex item = mList.get(position);
        holder.tvEventTime.setText(item.getTimeString());
        if(item.getHumidity().isEmpty()) {
            holder.humidityCard.setVisibility(View.GONE);
        } else {
            holder.humidityCard.setVisibility(View.VISIBLE);
            holder.tvHumidity.setText(item.getHumidity());
            Picasso.with(context).load(R.drawable.humidity).into(holder.humidityIcon);
        }

        if(item.getTemperature().isEmpty()) {
            holder.tempCard.setVisibility(View.GONE);
        } else {
            holder.tempCard.setVisibility(View.VISIBLE);
            holder.tvTemp.setText(item.getTemperature());
            Picasso.with(context).load(R.drawable.temp).into(holder.tempIcon);
        }

        if(item.getNh3().isEmpty()) {
            holder.nh3Card.setVisibility(View.GONE);
        } else {
            holder.nh3Card.setVisibility(View.VISIBLE);
            holder.tvNh3.setText(item.getNh3());
            Picasso.with(context).load(R.drawable.nh3).into(holder.nh3Icon);
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) {
            Log.e("getItemCount", "count==null");
            return 0;
        } else {
            return mList.size();
        }
    }

    public class EnvironmentIndexViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout humidityCard, tempCard, nh3Card;
        private ImageView humidityIcon, tempIcon, nh3Icon;
        private TextView tvEventTime;
        private TextView tvHumidity, tvTemp, tvNh3;
        public EnvironmentIndexViewHolder(View v) {
            super(v);
            humidityCard = (LinearLayout)v.findViewById(R.id.humidity_layout);
            tempCard = (LinearLayout)v.findViewById(R.id.temp_layout);
            nh3Card = (LinearLayout)v.findViewById(R.id.nh3_layout);
            humidityIcon = (ImageView)v.findViewById(R.id.humidityImageView);
            tempIcon = (ImageView)v.findViewById(R.id.tempImageView);
            nh3Icon = (ImageView)v.findViewById(R.id.nh3ImageView);
            tvEventTime = (TextView)v.findViewById(R.id.timeTextView);
            tvHumidity = (TextView)v.findViewById(R.id.humidityTextView);
            tvTemp = (TextView)v.findViewById(R.id.tempTextView);
            tvNh3 = (TextView)v.findViewById(R.id.nh3TextView);
        }
    }
}
