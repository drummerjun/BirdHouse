package com.junyenhuang.birdhouse.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.database.DBHandler;
import com.junyenhuang.birdhouse.items.Mp3Info;
import com.junyenhuang.birdhouse.settings.TrackSettingActivity;

import java.util.ArrayList;

public class TracklistAdapter extends RecyclerView.Adapter<TracklistAdapter.TrackViewHolder> {
    private static final String TAG = TracklistAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<Mp3Info> mList;
    private String tag;

    public TracklistAdapter(Context context, ArrayList<Mp3Info> list) {
        this.context = context;
        mList = list;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_songs, parent, false);
        tag = parent.getResources().getString(R.string.track_tag);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        if(mList == null) {
            return;
        }
        Mp3Info mp3 = mList.get(position);
        holder.tvTitle.setText(tag + " " + mp3.getId());
        holder.tvStart.setText(" " + mp3.getStart());
        holder.tvStop.setText(
                setDurationDescriptionText(mp3.getDurationHour(), mp3.getDurationMin()));
        holder.tvVol.setText(String.valueOf(mp3.getVol()));
        holder.tvTrack.setText(" " + String.valueOf(mp3.getSongId()));
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DBHandler db = new DBHandler(context);
                    int position = holder.getAdapterPosition();
                    db.deleteTrack(mList.get(position));
                    mList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mList.size());
                    context.sendBroadcast(new Intent("com.birdhouse.tracksEDITED"));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String setDurationDescriptionText(int hour, int minutes) {
        StringBuilder sb = new StringBuilder();
        if(hour == 23 && minutes == 59) {
            sb.append(" " + context.getString(R.string.allday));
            return sb.toString();
        }

        if(hour > 0) {
            sb.append(" " + hour + " ");
            if(hour > 1) {
                sb.append(context.getString(R.string.hours));
            } else {
                sb.append(context.getString(R.string.hour));
            }
        }

        if(minutes > 0) {
            sb.append(" " + minutes + " ");
            if(minutes > 1) {
                sb.append(context.getString(R.string.minutes));
            } else {
                sb.append(context.getString(R.string.minute));
            }
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        if(mList == null) {
            return 0;
        } else {
            return mList.size();
        }
    }

    public int updateData(ArrayList<Mp3Info> tracklist) {
        mList.clear();
        mList.addAll(tracklist);
        notifyDataSetChanged();
        return tracklist.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvTitle, tvStart, tvStop, tvVol, tvTrack;
        private ImageButton btnDelete;
        public TrackViewHolder(View v) {
            super(v);
            tvTitle = (TextView)v.findViewById(R.id.titleTextView);
            tvStart = (TextView)v.findViewById(R.id.startTextView);
            tvStop = (TextView)v.findViewById(R.id.stopTextView);
            tvVol = (TextView)v.findViewById(R.id.volumeTextView);
            tvTrack = (TextView)v.findViewById(R.id.trackNumTextView);
            btnDelete = (ImageButton)v.findViewById(R.id.deleteButton);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Mp3Info mp3 = mList.get(getAdapterPosition());
            Intent intent = new Intent(context, TrackSettingActivity.class);
            intent.putExtra(Constants.JSON_ID, mp3.getId());
            intent.putExtra(Constants.JSET_MP3_ID, mp3.getSongId());
            intent.putExtra(Constants.JSET_VOLUME, mp3.getVol());
            intent.putExtra(Constants.JSET_START, mp3.getStart());
            //intent.putExtra(Constants.JSET_STOP, mp3.getStop());
            intent.putExtra(Constants.JSET_STOP, mp3.getTotalDurationMinutes());
            Log.d(TAG + "::onClick", "ID=" + mp3.getId() + " Track=" + mp3.getSongId() + " VOL=" + mp3.getVol()
                + "\nTime=" + mp3.getStart() + " - " + mp3.getStop());
            context.startActivity(intent);
        }
    }
}
