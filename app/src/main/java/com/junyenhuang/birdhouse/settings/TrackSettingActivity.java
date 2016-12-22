package com.junyenhuang.birdhouse.settings;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.database.DBHandler;
import com.junyenhuang.birdhouse.items.Mp3Info;

public class TrackSettingActivity extends AppCompatPreferenceActivity {
    private static final String TAG = TrackSettingActivity.class.getSimpleName();
    private static DBHandler db;
    private static Mp3Info mp3;
    private static int song_id;
    private static String start;
    //private static String stop;
    private static int duration;
    private static int volume;
    private int id;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            PreferenceManager.getDefaultSharedPreferences(preference.getContext()).edit()
                    .putBoolean("TRACKS_EDITED", true).apply();
            if (preference instanceof ListPreference) {
                ListPreference pref = (ListPreference) preference;
                int index = pref.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? pref.getEntries()[index]
                                : null);
                if(pref.getKey().equals("key_song_id")) {
                    mp3.setSongId(Integer.parseInt(stringValue));
                }
            } else if(preference instanceof TimePreference) {
                TimePreference pref = (TimePreference) preference;
                stringValue = pref.getTimeSummary();
                if (pref.getKey().equals("key_timepref_start_1")) {
                    mp3.setStart(stringValue);
                //} else if (pref.getKey().equals("key_timepref_stop_1")) {
                //    mp3.setStop(stringValue);
                }
            } else if(preference instanceof SeekBarPreference) {
                int newValue = 30;
                if(!stringValue.isEmpty()) {
                    newValue = Integer.parseInt(stringValue);
                }
                SeekBarPreference pref = (SeekBarPreference) preference;
                //pref.setNewValue(newValue);
                if(pref.getKey().equals("key_volume")) {
                    mp3.setVol(newValue);
                }
            } else {
                preference.setSummary(stringValue);
            }
            Log.d(TAG + "::onPreferenceChange", "PREF=" + preference.getKey() + " ID=" + mp3.getId()
                    + " Track=" + mp3.getSongId() + " VOL=" + mp3.getVol()
                    + " Time=" + mp3.getStart() + "-" + mp3.getStop()
                    + " Duration=" + mp3.getTotalDurationMinutes());
            db.updateTrack(mp3);
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        id = intent.getIntExtra(Constants.JSON_ID, -1);
        song_id = intent.getIntExtra(Constants.JSET_MP3_ID, 1);
        volume = intent.getIntExtra(Constants.JSET_VOLUME, 30);
        start = intent.getStringExtra(Constants.JSET_START);
        //stop = intent.getStringExtra(Constants.JSET_STOP);
        duration = intent.getIntExtra(Constants.JSET_STOP, 60);

        Log.d(TAG + "::onCreate intent", "ID=" + id + " Track=" + song_id
                + " VOL=" + volume + " Time=" + start + " for" + duration);

        setupActionBar();
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new AudioPreferenceFragment()).commit();

        db = new DBHandler(getApplicationContext());
        mp3 = db.getTrack(id);
        Log.d(TAG + "::onCreate DB", "ID=" + mp3.getId() + " Track=" + mp3.getSongId()
                + " VOL=" + mp3.getVol() + " Time=" + mp3.getStart() + "-" + mp3.getStop()
                + " DURATION(in minutes)=" + mp3.getTotalDurationMinutes());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(db != null) {
            db.close();
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.track_tag)
                    + " " + String.valueOf(id));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AudioPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_song);
            setHasOptionsMenu(true);

            ListPreference track = (ListPreference)findPreference("key_song_id");
            track.setValue(String.valueOf(song_id));
            bindPreferenceSummaryToValue(track);

            TimePreference startTime = (TimePreference)findPreference("key_timepref_start_1");
            startTime.setTime(start);
            //startTime.getTimeSummary();
            bindPreferenceSummaryToValue(startTime);

            /*
            TimePreference stopTime = (TimePreference)findPreference("key_timepref_stop_1");
            stopTime.setTime(stop);
            //stopTime.getTimeSummary();
            bindPreferenceSummaryToValue(stopTime);
            */

            final MyListPreference stopper = (MyListPreference)findPreference("key_list_stop_1");
            int index = stopper.findIndexOfValue(String.valueOf(duration));
            Log.d(TAG, "duration=" + duration + " findIndexOfValue=" + index);
            if(index > -1) {
                stopper.setValueIndex(index);
            } else {
                stopper.setValueIndex(6);
            }
            stopper.setSummary(setMySummary(stopper, String.valueOf(duration)));
            stopper.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(Integer.parseInt(newValue.toString()) == 0) {
                        Log.d(TAG, "onPreferenceChange CUSTOM");
                    } else {
                        mp3.setTotalDurationMinutes(Integer.parseInt(newValue.toString()));
                        int index = ((ListPreference)preference).findIndexOfValue(newValue.toString());
                        preference.setSummary(((ListPreference)preference).getEntries()[index]);
                    }
                    return true;
                }
            });

            stopper.setOnListItemClickListener(new MyListPreference.ListItemClickListener() {
                @Override
                public void onListItemClick(String entry, String value) {
                    if (value.equals("0")) {
                        LayoutInflater lf = LayoutInflater.from(getActivity());
                        View v = lf.inflate(R.layout.dialog_numberpickers,
                                (ViewGroup)getActivity().findViewById(android.R.id.content),
                                false);
                        final NumberPicker hourPicker = (NumberPicker) v.findViewById(R.id.numberPicker);
                        final NumberPicker minutePicker = (NumberPicker) v.findViewById(R.id.numberPicker2);
                        TextView titleTV = (TextView)v.findViewById(R.id.fake_dialog_title);
                        titleTV.setText(getString(R.string.title_duration));
                        hourPicker.setMinValue(0);
                        hourPicker.setMaxValue(23);
                        hourPicker.setValue(0);
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
                        minutePicker.setValue(1);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        //builder.setTitle(R.string.title_duration);
                        builder.setView(v);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = hourPicker.getValue();
                                int minutes = minutePicker.getValue();
                                Log.d(TAG, "hour=" + hour);
                                Log.d(TAG, "minute=" + hour);

                                mp3.setDurationHour(hour);
                                mp3.setDurationMin(minutes);

                                StringBuilder sb = new StringBuilder();
                                if(hour == 23 && minutes == 59) {
                                    sb.append(getString(R.string.allday));
                                    stopper.setValue("1439");
                                } else {
                                    if (hour > 0) {
                                        sb.append(hour + " ");
                                        if (hour > 1) {
                                            sb.append(getString(R.string.hours));
                                        } else {
                                            sb.append(getString(R.string.hour));
                                        }
                                        sb.append(" ");
                                    }

                                    if (minutes > 0) {
                                        sb.append(minutes + " ");
                                        if (minutes > 1) {
                                            sb.append(getString(R.string.minutes));
                                        } else {
                                            sb.append(getString(R.string.minute));
                                        }
                                    }
                                }
                                stopper.setSummary(sb.toString());
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                        .putBoolean("TRACKS_EDITED", true).apply();
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });

            SeekBarPreference trackVol = (SeekBarPreference)findPreference("key_volume");
            trackVol.setNewValue(volume);
            bindPreferenceSummaryToValue(trackVol);
        }

        private String setMySummary(Preference preferece, String summary) {
            Log.d(TAG, "new value=" + summary);
            MyListPreference pref = (MyListPreference)preferece;
            StringBuilder sb = new StringBuilder();
            CharSequence[] entries = pref.getEntries();
            int index = pref.findIndexOfValue(summary);
            Log.d(TAG, "findIndexOfValue=" + index);
            if(index > -1 && index < 6) {
                sb.append(entries[index]);
            } else {
                pref.setValueIndex(6);
                int duration = Integer.parseInt(summary);
                int hour = duration / 60;
                int minutes = duration % 60;
                if(hour == 23 && minutes == 59) {
                    sb.append(getString(R.string.allday));
                } else {
                    if (hour > 0) {
                        sb.append(hour + " ");
                        if (hour > 1) {
                            sb.append(getResources().getString(R.string.hours));
                        } else {
                            sb.append(getResources().getString(R.string.hour));
                        }
                        sb.append(" ");
                    }

                    if (minutes > 0) {
                        sb.append(minutes + " ");
                        if (minutes > 1) {
                            sb.append(getResources().getString(R.string.minutes));
                        } else {
                            sb.append(getResources().getString(R.string.minute));
                        }
                    }
                }
            }
            return sb.toString();
        }
    }
}
