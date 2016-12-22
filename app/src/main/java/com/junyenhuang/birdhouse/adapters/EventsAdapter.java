package com.junyenhuang.birdhouse.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.items.HouseEvent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.HouseEventViewHolder> {
    private Context context;
    private ArrayList<HouseEvent> mList;

    public EventsAdapter(ArrayList<HouseEvent> events, Context context) {
        mList = events;
        this.context = context;
    }

    public void updateData(ArrayList<HouseEvent> events) {
        if(events != null) {
            mList.clear();
            mList.addAll(events);
            notifyDataSetChanged();
        }
    }

    @Override
    public HouseEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_events, parent, false);
        return new HouseEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final HouseEventViewHolder holder, final int position) {
        if(mList == null) {
            Log.e("onBindViewHolder", "mList==null");
            return;
        }
        final HouseEvent item = mList.get(position);
        holder.tvEventTime.setText(item.getTimeString());
        String description = item.getDescription();
        int elementId = item.getElementKey();
            switch (elementId) {
                case 1: // switches
                case 2:
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.on_off).into(holder.ivIcon);
                    } else {
                        String[] splits = description.split("\\s+");
                        StringBuilder sb = new StringBuilder();
                        sb.append("#").append(splits[0]);
                        sb.append(" (").append(context.getString(R.string.element_humidity));
                        sb.append(": ").append(item.getEventValue()).append(")");
                        holder.tvDescription.setText(sb.toString());
                        if (splits[1].contains("ON")) {
                            //holder.tvDescription.setText(context.getString(R.string.event_sw_on));
                            Picasso.with(context).load(R.drawable.on).into(holder.ivIcon);
                        } else if (splits[1].contains("OFF")) {
                            //holder.tvDescription.setText(context.getString(R.string.event_sw_off));
                            Picasso.with(context).load(R.drawable.off).into(holder.ivIcon);
                        }
                    }
                    break;
                case 3: // entries
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.door_off).into(holder.ivIcon);
                    } else {
                        holder.tvDescription.setText(context.getString(R.string.event_entry));
                        Picasso.with(context).load(R.drawable.door_on).into(holder.ivIcon);
                    }
                    break;
                case 4: // intrusions
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.theif).into(holder.ivIcon);
                    } else {
                        holder.tvDescription.setText(context.getString(R.string.event_intrusion));
                        Picasso.with(context).load(R.drawable.theif_on).into(holder.ivIcon);
                    }
                    break;
                case 5: // power out/recovery
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.power_on).into(holder.ivIcon);
                    } else {
                        if (item.getEventValue().equals("OFF")) {
                            holder.tvDescription.setText(context.getString(R.string.event_power_out));
                            Picasso.with(context).load(R.drawable.power_off).into(holder.ivIcon);
                        } else if (item.getEventValue().equals("ON")) {
                            holder.tvDescription.setText(context.getString(R.string.event_power_back));
                            Picasso.with(context).load(R.drawable.power_on).into(holder.ivIcon);
                        }
                    }
                    break;
                case 6: // humidity
                case 7: // temp
                case 8: // nh3
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.enviro).into(holder.ivIcon);
                    } else {
                        String unit = item.getEventValue().split("\\s+")[1];
                        if (unit.equals("C")) {
                            holder.tvDescription.setText(context.getString(R.string.event_temp) + ": " + item.getEventValue());
                            Picasso.with(context).load(R.drawable.temp).into(holder.ivIcon);
                        } else if (unit.equals("%")) {
                            holder.tvDescription.setText(context.getString(R.string.event_humidity) + ": " + item.getEventValue());
                            Picasso.with(context).load(R.drawable.humidity).into(holder.ivIcon);
                        } else if (unit.toLowerCase().equals("ppm")) {
                            holder.tvDescription.setText(context.getString(R.string.event_nh3) + ": " + item.getEventValue());
                            Picasso.with(context).load(R.drawable.nh3).into(holder.ivIcon);
                        }
                    }
                    break;
                case 9: // count
                    if(description.equals(context.getString(R.string.no_event))) {
                        holder.tvDescription.setText(description);
                        Picasso.with(context).load(R.drawable.birds).into(holder.ivIcon);
                    } else {
                        String unit = context.getString(R.string.element_unit_count);
                        String value = item.getEventValue().replace(" 隻", unit);
                        holder.tvDescription.setText(context.getString(R.string.event_count) + ": " + value);
                        Picasso.with(context).load(R.drawable.birds).into(holder.ivIcon);
                    }
                    break;
                default:
                    holder.tvDescription.setText(description);
                    Picasso.with(context).load(R.drawable.warning).into(holder.ivIcon);
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

    public class HouseEventViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvEventTime;
        private TextView tvDescription;
        public HouseEventViewHolder(View v) {
            super(v);
            ivIcon = (ImageView)v.findViewById(R.id.iconImageView);
            tvEventTime = (TextView)v.findViewById(R.id.timeTextView);
            tvDescription = (TextView)v.findViewById(R.id.descriptionTextView);
        }
    }
}
