package com.junyenhuang.birdhouse.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class MyListPreference extends ListPreference {
    private static final String TAG = MyListPreference.class.getSimpleName();
    private ListItemClickListener mListItemClickListener;
    private int hours = 0;
    private int minutes = 1;
    private Context context;

    public MyListPreference(Context context) {
        super(context);
        this.context = context;
    }

    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public void setOnListItemClickListener(ListItemClickListener listener) {
        mListItemClickListener = listener;
    }

    public interface ListItemClickListener {
        void onListItemClick(String entry, String value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        Log.d(TAG, "onDialogClosed::boolean=" + positiveResult);
        super.onDialogClosed(positiveResult);

        if(positiveResult && getEntryValues() != null && mListItemClickListener != null) {
            String entry = getEntry().toString();
            String value = getValue();
            Log.d(TAG, "entry=" + entry + " value=" + value);
            mListItemClickListener.onListItemClick(entry, value);
        }
    }

    public void setHour(int hour) {
        hours = hour;
    }

    public int getHour() {
        return hours;
    }

    public void setMinutes(int min) {
        minutes = min;
    }

    public int getMinutes() {
        return minutes;
    }
}
