package com.junyenhuang.birdhouse.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.JsonParser;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.junyenhuang.birdhouse.items.SwitchSetting;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import io.apptik.widget.MultiSlider;

public class SwitchFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = SwitchFragment.class.getSimpleName();
    private PassData dataMessenger;
    private int mMode = 0;
    private int mSwitchNum = 0;
    private SwitchSetting mSwitch;
    private static int mHouseId = -1;
    private static SettingGroup mSettings;
    private static String mSettingsString;
    private TextView tvSummaryHumidity, tvSummaryStart, tvSummaryStop, tvSummaryInterval, tvSummaryDuration;

    public SwitchFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSwitch = new SwitchSetting();

        Bundle args = getArguments();
        mHouseId = args.getInt(Constants.JSON_ID);
        mSettingsString = args.getString(Constants.JSON_SETTINGS_STRING);
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                JsonParser parser = new JsonParser(getContext());
                mSettings = parser.parseSettings(params[0].toString(), Integer.parseInt(params[1].toString()), false);
                return null;
            }
        }.execute(mSettingsString, mHouseId);
        mMode = args.getInt(Constants.JSET_SWITCH_MODE);
        mSwitchNum = args.getInt("SWITCH_NUMBER");
        Log.d(TAG, "switch mode = " + mMode + " mSwitchNum=" + mSwitchNum);

        mSwitch.setMode(args.getInt(Constants.JSET_SWITCH_MODE));
        mSwitch.setHumidityMin(args.getInt(Constants.JSET_HUM_MIN));
        mSwitch.setHumidityMax(args.getInt(Constants.JSET_HUM_MAX));
        mSwitch.setStart(args.getString(Constants.JSET_START));
        mSwitch.setTotalDurationMinutes(args.getInt(Constants.JSET_STOP));
        mSwitch.setInterval(args.getInt(Constants.JSET_SW_HOUR));
        mSwitch.setDuration(args.getInt(Constants.JSET_SW_MIN));

        JsonParser parser = new JsonParser(getActivity());
        parser.parseSettings(mSettingsString, mHouseId, false);
        View v = inflater.inflate(R.layout.frag_switch, container, false);
        initViews(v);
        return v;
    }

    /*
    @Override
    public void onPause() {
        super.onPause();
        if(mSettings != null) {
            mSettings.switches.set(mSwitchNum, mSwitch);
            //DEBUG_STR
            boolean DEBUG = false;
            if(!DEBUG) {
                ///* DEBUG_START
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        StringBuilder url = new StringBuilder();
                        url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                        url.append(String.valueOf(mHouseId));
                        Log.d(TAG, "post URL=" + url);
                        mSettingsString = new JsonBuilder()
                                .buildString(mSettings, getActivity());
                        HashMap<String, String> map = new HashMap<>();
                        map.put("settings", mSettingsString);
                        new WebRequest().makeWebServiceCall(
                                url.toString(), WebRequest.POSTRequest, map);
                        return null;
                    }
                }.execute();
            }
        }
    }
    */

    public void initViews(View v) {
        final LinearLayout humdity_control = (LinearLayout)v.findViewById(R.id.humidity_control_layout);
        final LinearLayout time_control = (LinearLayout)v.findViewById(R.id.time_control_layout);
        final LinearLayout button1 = (LinearLayout)v.findViewById(R.id.button1);
        final LinearLayout button2 = (LinearLayout)v.findViewById(R.id.button2);
        final ImageView icon_humi = (ImageView)v.findViewById(R.id.button_image_1);
        final ImageView icon_clock = (ImageView)v.findViewById(R.id.button_image_2);
        Button sButton3 = (Button)v.findViewById(R.id.button3);
        Button sButton4 = (Button)v.findViewById(R.id.button4);
        Button sButton5 = (Button)v.findViewById(R.id.button5);
        Button sButton6 = (Button)v.findViewById(R.id.button6);
        Button sButton7 = (Button)v.findViewById(R.id.button7);

        tvSummaryHumidity = (TextView)v.findViewById(R.id.humidity_summary);
        tvSummaryStart = (TextView)v.findViewById(R.id.start_summary);
        tvSummaryStop = (TextView)v.findViewById(R.id.duration_summary);
        tvSummaryInterval = (TextView)v.findViewById(R.id.every_summary);
        tvSummaryDuration = (TextView)v.findViewById(R.id.for_summary);
        buildSwitchButtonSummary();

        if(mMode == 1) {
            humdity_control.setVisibility(View.VISIBLE);
            button1.setActivated(true);
            icon_humi.setActivated(true);
            time_control.setVisibility(View.GONE);
            icon_clock.setActivated(false);
            button2.setActivated(false);
        } else if(mMode == 2) {
            humdity_control.setVisibility(View.GONE);
            button1.setActivated(false);
            icon_humi.setActivated(false);
            time_control.setVisibility(View.VISIBLE);
            icon_clock.setActivated(true);
            button2.setActivated(true);
        } else {
            humdity_control.setVisibility(View.GONE);
            button1.setActivated(false);
            icon_humi.setActivated(false);
            time_control.setVisibility(View.GONE);
            icon_clock.setActivated(false);
            button2.setActivated(false);
        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == 1) {
                    mMode = 0;
                    mSwitch.setMode(mMode);
                    button1.setActivated(false);
                    icon_humi.setActivated(false);
                    humdity_control.setVisibility(View.GONE);
                } else {
                    mMode = 1;
                    mSwitch.setMode(mMode);
                    button1.setActivated(true);
                    icon_humi.setActivated(true);
                    button2.setActivated(false);
                    icon_clock.setActivated(false);
                    humdity_control.setVisibility(View.VISIBLE);
                    time_control.setVisibility(View.GONE);
                }
                dataMessenger.onPassData(mSwitch, mSwitchNum);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == 2) {
                    mMode = 0;
                    mSwitch.setMode(mMode);
                    button2.setActivated(false);
                    icon_clock.setActivated(false);
                    time_control.setVisibility(View.GONE);
                } else {
                    mMode = 2;
                    mSwitch.setMode(mMode);
                    button1.setActivated(false);
                    icon_humi.setActivated(false);
                    button2.setActivated(true);
                    icon_clock.setActivated(true);
                    humdity_control.setVisibility(View.GONE);
                    time_control.setVisibility(View.VISIBLE);
                }
                dataMessenger.onPassData(mSwitch, mSwitchNum);
            }
        });

        sButton3.setOnClickListener(this);
        sButton4.setOnClickListener(this);
        sButton5.setOnClickListener(this);
        sButton6.setOnClickListener(this);
        sButton7.setOnClickListener(this);
    }

    private void buildSwitchButtonSummary() {
        tvSummaryHumidity.setText(mSwitch.getHumidityMin() + " - " + mSwitch.getHumidityMax() + "%");
        tvSummaryStart.setText(mSwitch.getStart());
        if(mSwitch.getTotalDurationMinutes() == 1439) {
            tvSummaryStop.setText(getString(R.string.allday));
        } else {
            int hour = mSwitch.getTotalDurationMinutes() / 60;
            int minutes = mSwitch.getTotalDurationMinutes() % 60;
            StringBuilder sb = new StringBuilder();
            if (hour == 1) {
                sb.append(hour).append(getString(R.string.hour));
            } else if (hour > 0) {
                sb.append(hour).append(getString(R.string.hours));
            }
            if (minutes == 1) {
                sb.append(minutes).append(getString(R.string.minute));
            } else if (minutes > 0) {
                sb.append(minutes).append(getString(R.string.minutes));
            }
            tvSummaryStop.setText(sb.toString());
        }

        tvSummaryInterval.setText(mSwitch.getInterval() + getString(R.string.hours));

        String[] hour_list = getResources().getStringArray(R.array.pref_duration_list_title);
        String[] hour_values = getResources().getStringArray(R.array.pref_duration_list_values);
        int highlightIndex = Arrays.asList(hour_values).indexOf(String.valueOf(mSwitch.getDuration()));
        try {
            tvSummaryDuration.setText(hour_list[highlightIndex]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            tvSummaryDuration.setText(String.valueOf(mSwitch.getDuration() + getString(R.string.minutes)));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button3:
                showRangeBarDialog();
                break;
            case R.id.button4:
                showTimePickerDialog();
                break;
            case R.id.button5:
                showNumberPickerDialog();
                break;
            case R.id.button6:
                showHourListDialog();
                break;
            case R.id.button7:
                showDurationListDialog();
                break;
        }
    }

    private void showRangeBarDialog() {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.preference_rangebar,
                (ViewGroup)getActivity().findViewById(android.R.id.content),
                false);
        final TextView tvMin = (TextView)layout.findViewById(R.id.min);
        tvMin.setText(String.valueOf(mSwitch.getHumidityMin()));
        final TextView tvMax = (TextView)layout.findViewById(R.id.max);
        tvMax.setText(String.valueOf(mSwitch.getHumidityMax()));

        final MultiSlider rangeBar = (MultiSlider)layout.findViewById(R.id.rangebar);
        rangeBar.getThumb(0).setValue(mSwitch.getHumidityMin());
        rangeBar.getThumb(1).setValue(mSwitch.getHumidityMax());
        rangeBar.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value) {
                if(thumbIndex == 0) {
                    tvMin.setText(String.valueOf(value));
                } else if(thumbIndex == 1) {
                    tvMax.setText(String.valueOf(value));
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(layout);
        builder.setTitle(R.string.humidity_range);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSwitch.setHumidityMin(rangeBar.getThumb(0).getValue());
                mSwitch.setHumidityMax(rangeBar.getThumb(1).getValue());
                buildSwitchButtonSummary();
                dataMessenger.onPassData(mSwitch, mSwitchNum);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showTimePickerDialog() {
        /*
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date mDate = sdf.parse(mSwitch.getStart());
            hour = mDate.getHours();
            minute = mDate.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                NumberFormat formatter = new DecimalFormat("00");
                String timeString = formatter.format(selectedHour) + ":" + formatter.format(selectedHour);
                mSwitch.setStart(timeString);
                buildSwitchButtonSummary();
                Log.d(TAG, "set start=" + mSwitch.getStart());
                //eReminderTime.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true); //Yes 24 hour time
        mTimePicker.setTitle(getString(R.string.pref_timer_start));
        mTimePicker.show();
        */

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_numberpickers,
                (ViewGroup)getActivity().findViewById(android.R.id.content),
                false);

        final NumberPicker hourPicker = (NumberPicker)layout.findViewById(R.id.numberPicker);
        final NumberPicker minutePicker = (NumberPicker)layout.findViewById(R.id.numberPicker2);
        TextView titleTV = (TextView)layout.findViewById(R.id.fake_dialog_title);
        TextView hourTV = (TextView)layout.findViewById(R.id.hourTextView);
        TextView minuteTV = (TextView)layout.findViewById(R.id.minuteTextView);
        titleTV.setText(getString(R.string.pref_timer_start));
        hourTV.setText(R.string.clock_hour);
        minuteTV.setText(R.string.clock_minute);
        String[] startTimeString = mSwitch.getStart().split(":");

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });
        hourPicker.setValue(Integer.parseInt(startTimeString[0]));
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });
        minutePicker.setValue(Integer.parseInt(startTimeString[1]));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(layout);
        //builder.setTitle(getString(R.string.pref_timer_start));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int hour = hourPicker.getValue();
                int minutes = minutePicker.getValue();
                Log.d(TAG, "hour=" + hour);
                Log.d(TAG, "minute=" + minutes);
                NumberFormat formatter = new DecimalFormat("00");
                String timeString = formatter.format(hour) + ":" + formatter.format(minutes);
                mSwitch.setStart(timeString);
                buildSwitchButtonSummary();
                dataMessenger.onPassData(mSwitch, mSwitchNum);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showNumberPickerDialog() {
        final String[] duration_titles = getResources().getStringArray(R.array.pref_stoptime_list_title);
        final String[] duration_values = getResources().getStringArray(R.array.pref_stoptime_list_value);
        int highlightIndex = Arrays.asList(duration_values).indexOf(String.valueOf(mSwitch.getTotalDurationMinutes()));
        if(highlightIndex == -1) {
            highlightIndex = 6;
        }
        Log.d(TAG, "highlightIndex=" + highlightIndex);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle(R.string.title_duration);
        builder1.setSingleChoiceItems(duration_titles, highlightIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, duration_values[which] + " set");
                if(duration_values[which].equals("0")) {
                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.dialog_numberpickers,
                            (ViewGroup)getActivity().findViewById(android.R.id.content),
                            false);

                    final NumberPicker hourPicker = (NumberPicker)layout.findViewById(R.id.numberPicker);
                    final NumberPicker minutePicker = (NumberPicker)layout.findViewById(R.id.numberPicker2);
                    TextView titleTV = (TextView)layout.findViewById(R.id.fake_dialog_title);
                    titleTV.setText(getString(R.string.title_duration));
                    hourPicker.setMinValue(0);
                    hourPicker.setMaxValue(23);
                    hourPicker.setValue(mSwitch.getTotalDurationMinutes() / 60);
                    hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            if(newVal == 0) {
                                minutePicker.setMinValue(1);
                            } else if(newVal > 0) {
                                minutePicker.setMinValue(0);
                            }
                        }
                    });
                    minutePicker.setMinValue(1);
                    minutePicker.setMaxValue(59);
                    minutePicker.setValue(mSwitch.getTotalDurationMinutes() % 60);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(layout);
                    //builder.setTitle(getString(R.string.title_duration));
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int hour = hourPicker.getValue();
                            int minutes = minutePicker.getValue();
                            Log.d(TAG, "hour=" + hour);
                            Log.d(TAG, "minute=" + minutes);
                            mSwitch.setTotalDurationMinutes((hour * 60) + minutes);
                            buildSwitchButtonSummary();
                            dataMessenger.onPassData(mSwitch, mSwitchNum);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    mSwitch.setTotalDurationMinutes(Integer.parseInt(duration_values[which]));
                    buildSwitchButtonSummary();
                    dataMessenger.onPassData(mSwitch, mSwitchNum);
                }
                dialog.dismiss();
            }
        });
        builder1.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder1.create().show();
    }

    private void showHourListDialog() {
        final String[] hour_list = getResources().getStringArray(R.array.pref_interval_list);

        int highlightIndex = Arrays.asList(hour_list).indexOf(String.valueOf(mSwitch.getInterval()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pref_title_interval);
        builder.setSingleChoiceItems(hour_list, highlightIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, hour_list[which] + " set");
                mSwitch.setInterval(Integer.parseInt(hour_list[which]));
                buildSwitchButtonSummary();
                dataMessenger.onPassData(mSwitch, mSwitchNum);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showDurationListDialog() {
        final String[] hour_list = getResources().getStringArray(R.array.pref_duration_list_title);
        final String[] hour_values = getResources().getStringArray(R.array.pref_duration_list_values);
        int highlightIndex = Arrays.asList(hour_values).indexOf(String.valueOf(mSwitch.getDuration()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_duration);
        builder.setSingleChoiceItems(hour_list, highlightIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, hour_values[which] + " set");
                mSwitch.setDuration(Integer.parseInt(hour_values[which]));
                buildSwitchButtonSummary();
                dataMessenger.onPassData(mSwitch, mSwitchNum);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataMessenger = (PassData) context;
    }

    public interface PassData {
        public void onPassData(SwitchSetting switchSetting, int switchNum);
    }
}
