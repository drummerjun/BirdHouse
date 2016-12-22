package com.junyenhuang.birdhouse.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.junyenhuang.birdhouse.R;

import io.apptik.widget.MultiSlider;

public class SeekBarPreference extends Preference {
    private int volume = 0;
    private MultiSlider slider = null;
    private TextView volText;

    public SeekBarPreference(Context context) {
        super(context);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(R.layout.preference_seekbar, parent, false);
        volText = (TextView)view.findViewById(R.id.volume);
        volText.setText(String.valueOf(volume));
        slider = (MultiSlider)view.findViewById(R.id.seekbar);
        slider.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                volText.setText(String.valueOf(value));
                volume = value;
                String range = getRangeString();
                if (callChangeListener(range)) {
                    persistString(range);
                }
            }
        });
        return view;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        slider.getThumb(0).setValue(volume);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String volume;
        if (restorePersistedValue) {
            if (defaultValue == null) {
                volume = getPersistedString("50");
            } else {
                volume = getPersistedString(defaultValue.toString());
            }
        } else {
            volume = defaultValue.toString();
        }
        setNewValue(volume);
    }

    private void setNewValue(String value) {
        volume = Integer.parseInt(value);
    }

    public void setNewValue(int value) {
        volume = value;
    }

    private String getRangeString() {
        return String.valueOf(volume);
    }
}