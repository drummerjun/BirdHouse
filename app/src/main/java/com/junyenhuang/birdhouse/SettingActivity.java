package com.junyenhuang.birdhouse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.junyenhuang.birdhouse.settings.GeneralSettingActivity;
import com.junyenhuang.birdhouse.settings.SwitchTabActivity;
import com.junyenhuang.birdhouse.settings.TracklistActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG =  SettingActivity.class.getSimpleName();
    private TextView telTextView, ppmTextView;
    private ImageView entryImg, intrusionImg, intrusionAlarmImg, powerOutImg, powerBackImg;
    private static SettingGroup mSettings;
    private ArrayList<Boolean> mSelectedHouses = new ArrayList<>();
    private SparseArray<String> mSimpleHouses;
    private static int mHouseId = -1;
    private static final String PARAMS = "";
    private static boolean mSelectedAll = false;
    private static String mSettingsString;
    private String mTitle;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingActivity.this, OverviewMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String newTitle = prefs.getString(Constants.ACTION_NAME_CHANGE + mHouseId, "");
        if(!newTitle.isEmpty() && !newTitle.equals(mTitle)) {
            getSupportActionBar().setTitle(newTitle);
            mTitle = newTitle;
        }

        String newSettingString = prefs.getString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString);
        if(!newSettingString.equals(mSettingsString)) {
            mSettingsString = newSettingString;
            //refresh!
            new AsyncTask<String, Void, SettingGroup>() {
                @Override
                protected SettingGroup doInBackground(String... params) {
                    JsonParser parser = new JsonParser(SettingActivity.this);
                    return parser.parseSettings(params[0], mHouseId, false);
                }

                @Override
                protected void onPostExecute(SettingGroup settings) {
                    super.onPostExecute(settings);
                    buildGeneralSummary(settings);
                }
            }.execute(newSettingString);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        mHouseId = intent.getIntExtra(Constants.JSON_ID, -1);
        mTitle = intent.getStringExtra(Constants.JSON_NAME);
        mSettingsString = intent.getStringExtra(Constants.JSON_SETTINGS_STRING);
        JsonParser parser = new JsonParser(getApplicationContext());
        mSettings = parser.parseSettings(mSettingsString, mHouseId, true);
        mSimpleHouses = loadSimpleHouses();

        initConstantViews();
        buildGeneralSummary(mSettings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.general_button:
                intent = new Intent(this, GeneralSettingActivity.class);
                intent.putExtra(Constants.JSON_ID, mHouseId);
                intent.putExtra(Constants.JSON_NAME, mTitle);
                intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case R.id.switch_button:
                intent = new Intent(this, SwitchTabActivity.class);
                intent.putExtra(Constants.JSON_ID, mHouseId);
                intent.putExtra(Constants.JSON_NAME, mTitle);
                intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);

                intent.putExtra(Constants.JSET_SWITCH_MODE + "0", mSettings.switches.get(0).getMode());
                intent.putExtra(Constants.JSET_HUM_MIN + "0", mSettings.switches.get(0).getHumidityMin());
                intent.putExtra(Constants.JSET_HUM_MAX + "0", mSettings.switches.get(0).getHumidityMax());
                intent.putExtra(Constants.JSET_START + "0", mSettings.switches.get(0).getStart());
                intent.putExtra(Constants.JSET_STOP + "0", mSettings.switches.get(0).getTotalDurationMinutes());
                intent.putExtra(Constants.JSET_SW_HOUR + "0", mSettings.switches.get(0).getInterval());
                intent.putExtra(Constants.JSET_SW_MIN + "0", mSettings.switches.get(0).getDuration());

                intent.putExtra(Constants.JSET_SWITCH_MODE + "1", mSettings.switches.get(1).getMode());
                intent.putExtra(Constants.JSET_HUM_MIN + "1", mSettings.switches.get(1).getHumidityMin());
                intent.putExtra(Constants.JSET_HUM_MAX + "1", mSettings.switches.get(1).getHumidityMax());
                intent.putExtra(Constants.JSET_START + "1", mSettings.switches.get(1).getStart());
                intent.putExtra(Constants.JSET_STOP + "1", mSettings.switches.get(1).getTotalDurationMinutes());
                intent.putExtra(Constants.JSET_SW_HOUR + "1", mSettings.switches.get(1).getInterval());
                intent.putExtra(Constants.JSET_SW_MIN + "1", mSettings.switches.get(1).getDuration());

                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case R.id.audio_button:
                intent = new Intent(this, TracklistActivity.class);
                intent.putExtra(Constants.JSON_ID, mHouseId);
                intent.putExtra(Constants.JSON_NAME, mTitle);
                intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
        }
    }

    private void buildGeneralSummary(SettingGroup setting) {
        telTextView.setText(setting.getPhoneNumber());
        ppmTextView.setText(setting.getNh3Limit() + "ppm");
        if(setting.front.get(0).getAction() == 1 && setting.front.get(1).getAction() == 1) {
            // front gate sms notification
            Picasso.with(SettingActivity.this).load(R.drawable.i24hr).into(entryImg);
            //entryImg.setImageResource(R.drawable.ok);
        } else if(setting.front.get(0).getAction() == 1 && setting.front.get(1).getAction() == 0) {
            //off
            Picasso.with(SettingActivity.this).load(R.drawable.sun).into(entryImg);
            //entryImg.setImageResource(R.drawable.none);
        } else if(setting.front.get(0).getAction() == 0 && setting.front.get(1).getAction() == 1) {
            //off
            Picasso.with(SettingActivity.this).load(R.drawable.moon).into(entryImg);
        } else if(setting.front.get(0).getAction() == 0 && setting.front.get(1).getAction() == 0) {
            //off
            Picasso.with(SettingActivity.this).load(R.drawable.none).into(entryImg);
        }

        switch(setting.irs.get(1).getAction()) {
            case 0:
                Picasso.with(SettingActivity.this).load(R.drawable.none).into(intrusionAlarmImg);
                Picasso.with(SettingActivity.this).load(R.drawable.phone_small).into(intrusionImg);
                //intrusionAlarmImg.setImageResource(R.drawable.none);
                //intrusionImg.setImageResource(R.drawable.phone_small);
                // no alarm
                // call out
                break;
            case 1:
                Picasso.with(SettingActivity.this).load(R.drawable.none).into(intrusionAlarmImg);
                Picasso.with(SettingActivity.this).load(R.drawable.message).into(intrusionImg);
                //intrusionAlarmImg.setImageResource(R.drawable.none);
                //intrusionImg.setImageResource(R.drawable.message);
                // no alarm
                // send sms
                break;
            case 2:
                Picasso.with(SettingActivity.this).load(R.drawable.ok).into(intrusionAlarmImg);
                Picasso.with(SettingActivity.this).load(R.drawable.phone_small).into(intrusionImg);
                //intrusionAlarmImg.setImageResource(R.drawable.ok);
                //intrusionImg.setImageResource(R.drawable.phone_small);
                // alarm
                // call out
                break;
            case 3:
                Picasso.with(SettingActivity.this).load(R.drawable.ok).into(intrusionAlarmImg);
                Picasso.with(SettingActivity.this).load(R.drawable.message).into(intrusionImg);
                //intrusionAlarmImg.setImageResource(R.drawable.ok);
                //intrusionImg.setImageResource(R.drawable.message);
                // alarm
                // send sms
                break;
            default:
                Picasso.with(SettingActivity.this).load(R.drawable.none).into(intrusionAlarmImg);
                Picasso.with(SettingActivity.this).load(R.drawable.none).into(intrusionImg);
                //intrusionAlarmImg.setImageResource(R.drawable.none);
                //intrusionImg.setImageResource(R.drawable.none);
                //off
                break;
        }

        if(setting.getPowerOff()) {
            Picasso.with(SettingActivity.this).load(R.drawable.ok).into(powerOutImg);
            //powerOutImg.setImageResource(R.drawable.ok);
        } else {
            Picasso.with(SettingActivity.this).load(R.drawable.none).into(powerOutImg);
            //powerOutImg.setImageResource(R.drawable.none);
        }

        if(setting.getPowerOn()) {
            Picasso.with(SettingActivity.this).load(R.drawable.ok).into(powerBackImg);
            //powerBackImg.setImageResource(R.drawable.ok);
        } else {
            Picasso.with(SettingActivity.this).load(R.drawable.none).into(powerBackImg);
            //powerBackImg.setImageResource(R.drawable.none);
        }
    }

    private void initConstantViews() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout generalButton = (LinearLayout)findViewById(R.id.general_button);
        LinearLayout switchButton = (LinearLayout)findViewById(R.id.switch_button);
        LinearLayout audioButton = (LinearLayout)findViewById(R.id.audio_button);

        generalButton.setOnClickListener(this);
        switchButton.setOnClickListener(this);
        audioButton.setOnClickListener(this);

        telTextView = (TextView)findViewById(R.id.tvTel);
        ppmTextView = (TextView)findViewById(R.id.tvNh3);

        entryImg = (ImageView)findViewById(R.id.imgEntry);
        intrusionImg = (ImageView)findViewById(R.id.imgIntrusionNotify);
        intrusionAlarmImg = (ImageView)findViewById(R.id.imgIntrusion);
        powerOutImg = (ImageView)findViewById(R.id.imgPowerOut);
        powerBackImg = (ImageView)findViewById(R.id.imgPowerBack);

        ImageButton hotButton1, hotButton2, hotButton3, hotButton4, hotButton5;

        LinearLayout applyLayout = (LinearLayout)findViewById(R.id.hot_button0);
        applyLayout.setVisibility(View.VISIBLE);
        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_0);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_apply, null);
        hotButton1.setImageDrawable(drawable);
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectHouseDialog();
            }
        });

        LinearLayout refreshLayout = (LinearLayout)findViewById(R.id.hot_button1);
        refreshLayout.setVisibility(View.GONE);

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, EntryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                //finish();
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, OverviewMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                //finish();
            }
        });

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_logs, null);
        hotButton4.setImageDrawable(drawable);
        hotButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(SettingActivity.this, LogActivity.class);
                Intent intent = new Intent(SettingActivity.this, LogTypeActivity.class);
                intent.putExtra(Constants.JSON_ID, mHouseId);
                intent.putExtra(Constants.JSON_NAME, mTitle);
                intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                //finish();
            }
        });

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        Picasso.with(this).load(R.drawable.setting_on).into(hotButton5);
        hotButton5.setActivated(true);

        ImageView generalImageView = (ImageView)findViewById(R.id.setting_icon);
        Picasso.with(this).load(R.drawable.setting).into(generalImageView);
        ImageView switchImageView = (ImageView)findViewById(R.id.switch_icon);
        Picasso.with(this).load(R.drawable.onoff).into(switchImageView);
        ImageView audioImageView = (ImageView)findViewById(R.id.audio_icon);
        Picasso.with(this).load(R.drawable.music).into(audioImageView);
    }

    private void uploadData(int houseId) {
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");

                StringBuilder url = new StringBuilder();
                url.append("").append(savedUrl).append("");
                //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                url.append(String.valueOf(params[0]));
                String jsonString = new JsonBuilder()
                        .buildString(mSettings, SettingActivity.this);
                if(jsonString == null) {
                    return null;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put(PARAMS, jsonString);
                new WebRequest().makeWebServiceCall(
                        url.toString(), WebRequest.POSTRequest, map);
                return null;
            }
        }.execute(houseId);
    }

    private SparseArray<String> loadSimpleHouses() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String housePairs = prefs.getString(Constants.EXTRA_HOUSENAMES, "");
        SparseArray<String> map = new SparseArray<>();
        if(!housePairs.isEmpty()) {
            for(String pairs : housePairs.split(String.valueOf((char)0x0124))) {
                String[] indiv = pairs.split(String.valueOf((char)0x0061));
                map.put(Integer.parseInt(indiv[0]), (indiv[1]));
            }
        }
        return map;
    }

    protected void showSelectHouseDialog() {
        if(mSimpleHouses.size() == 0) {
            return;
        }

        final boolean[] appliedHouses = new boolean[mSimpleHouses.size()];
        for(int i = 0; i < mSimpleHouses.size(); i++) {
            if(mHouseId == mSimpleHouses.keyAt(i)) {
                mSelectedHouses.add(true);
                appliedHouses[i] = true;
            } else {
                mSelectedHouses.add(false);
            }
        }

        DialogInterface.OnMultiChoiceClickListener mDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    mSelectedHouses.set(which, true);
                } else {
                    mSelectedHouses.set(which, false);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] names = new String[mSimpleHouses.size()];
        for (int i = 0; i < mSimpleHouses.size(); i++) {
            names[i] = mSimpleHouses.valueAt(i);
        }
        builder.setTitle(R.string.apply_houses)
                .setMultiChoiceItems(names, appliedHouses, mDialogListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for(int i = 0; i < mSelectedHouses.size(); i++) {
                            if(mSelectedHouses.get(i)) {
                                uploadData(mSimpleHouses.keyAt(i));
                            }
                        }
                        dialog.dismiss();
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mSelectedAll = false;
                        //mSelectedHouses.clear();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                getString(R.string.apply_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialog.show();
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView list = dialog.getListView();
                for (int i = 0; i < list.getCount(); i++) {
                    if(mSelectedAll) {
                        list.setItemChecked(i, false);
                        mSelectedHouses.set(i, false);
                        if(mHouseId == mSimpleHouses.keyAt(i)) {
                            list.setItemChecked(i, true);
                            mSelectedHouses.set(i, true);
                        }
                    } else {
                        list.setItemChecked(i, true);
                        mSelectedHouses.set(i, true);
                    }
                }
                mSelectedAll = !mSelectedAll;
            }
        });
    }
}
