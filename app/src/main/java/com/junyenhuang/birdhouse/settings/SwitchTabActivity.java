package com.junyenhuang.birdhouse.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.EntryActivity;
import com.junyenhuang.birdhouse.JsonBuilder;
import com.junyenhuang.birdhouse.JsonParser;
import com.junyenhuang.birdhouse.LogTypeActivity;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.junyenhuang.birdhouse.items.SwitchSetting;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SwitchTabActivity extends AppCompatActivity implements SwitchFragment.PassData{
    private static final String TAG = SwitchTabActivity.class.getSimpleName();
    private ImageButton hotButton1;
    private static SettingGroup mSettings;
    private static int mHouseId = -1;
    private static String mSettingsString;
    private String mTitle;
    private int mMode0 = 0;
    private int mHumiMin0 = 1;
    private int mHumiMax0 = 100;
    private String mStartTime0 = "12:00";
    private int mStopMin0 = 1;
    private int mInterval0 = 1;
    private int mDuration0 = 10;

    private int mMode1 = 0;
    private int mHumiMin1 = 1;
    private int mHumiMax1 = 100;
    private String mStartTime1 = "12:00";
    private int mStopMin1 = 1;
    private int mInterval1 = 1;
    private int mDuration1 = 10;

    @Override
    public void onBackPressed() {
        if(hotButton1.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SwitchTabActivity.this);
            builder.setTitle(R.string.upload_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    uploadData(mHouseId, mSettings);
                    hotButton1.setEnabled(false);
                    SwitchTabActivity.super.onBackPressed();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                    //PreferenceManager.getDefaultSharedPreferences(SwitchTabActivity.this).edit()
                    //        .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                    SwitchTabActivity.super.onBackPressed();
                }
            });
            builder.create().show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);

        Intent intent = getIntent();
        mHouseId = intent.getIntExtra(Constants.JSON_ID, -1);
        mTitle = intent.getStringExtra(Constants.JSON_NAME);
        mSettingsString = intent.getStringExtra(Constants.JSON_SETTINGS_STRING);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                JsonParser parser = new JsonParser(getApplicationContext());
                mSettings = parser.parseSettings(mSettingsString, mHouseId, false);
                mMode0 = mSettings.switches.get(0).getMode();
                mHumiMin0 = mSettings.switches.get(0).getHumidityMin();
                mHumiMax0 = mSettings.switches.get(0).getHumidityMax();
                mStartTime0 = mSettings.switches.get(0).getStart();
                mStopMin0 = mSettings.switches.get(0).getTotalDurationMinutes();
                mInterval0 = mSettings.switches.get(0).getInterval();
                mDuration0 = mSettings.switches.get(0).getDuration();

                mMode1 = mSettings.switches.get(1).getMode();
                mHumiMin1 = mSettings.switches.get(1).getHumidityMin();
                mHumiMax1 = mSettings.switches.get(1).getHumidityMax();
                mStartTime1 = mSettings.switches.get(1).getStart();
                mStopMin1 = mSettings.switches.get(1).getTotalDurationMinutes();
                mInterval1 = mSettings.switches.get(1).getInterval();
                mDuration1 = mSettings.switches.get(1).getDuration();
                return null;
            }
        }.execute();

        mMode0 = intent.getIntExtra(Constants.JSET_SWITCH_MODE + "0", 0);
        mHumiMin0 = intent.getIntExtra(Constants.JSET_HUM_MIN + "0", 1);
        mHumiMax0 = intent.getIntExtra(Constants.JSET_HUM_MAX + "0", 100);
        mStartTime0 = intent.getStringExtra(Constants.JSET_START + "0");
        mStopMin0 = intent.getIntExtra(Constants.JSET_STOP + "0", 1);
        mInterval0 = intent.getIntExtra(Constants.JSET_SW_HOUR + "0", 1);
        mDuration0 = intent.getIntExtra(Constants.JSET_SW_MIN + "0", 10);

        mMode1 = intent.getIntExtra(Constants.JSET_SWITCH_MODE + "1", 0);
        mHumiMin1 = intent.getIntExtra(Constants.JSET_HUM_MIN + "1", 1);
        mHumiMax1 = intent.getIntExtra(Constants.JSET_HUM_MAX + "1", 100);
        mStartTime1 = intent.getStringExtra(Constants.JSET_START + "1");
        mStopMin1 = intent.getIntExtra(Constants.JSET_STOP + "1", 1);
        mInterval1 = intent.getIntExtra(Constants.JSET_SW_HOUR + "1", 1);
        mDuration1 = intent.getIntExtra(Constants.JSET_SW_MIN + "1", 10);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(intent.getStringExtra(Constants.JSON_NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupBottomControls();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle args1 = new Bundle();
        args1.putInt("SWITCH_NUMBER", 0);
        args1.putInt(Constants.JSET_SWITCH_MODE, mMode0);
        args1.putInt(Constants.JSET_HUM_MIN, mHumiMin0);
        args1.putInt(Constants.JSET_HUM_MAX, mHumiMax0);
        args1.putString(Constants.JSET_START, mStartTime0);
        args1.putInt(Constants.JSET_STOP, mStopMin0);
        args1.putInt(Constants.JSET_SW_HOUR, mInterval0);
        args1.putInt(Constants.JSET_SW_MIN, mDuration0);
        args1.putString(Constants.JSON_SETTINGS_STRING, getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING));
        SwitchFragment frag1 = new SwitchFragment();
        frag1.setArguments(args1);

        Bundle args2 = new Bundle();
        args2.putInt("SWITCH_NUMBER", 1);
        args2.putInt(Constants.JSET_SWITCH_MODE, mMode1);
        args2.putInt(Constants.JSET_SWITCH_MODE, mMode1);
        args2.putInt(Constants.JSET_HUM_MIN, mHumiMin1);
        args2.putInt(Constants.JSET_HUM_MAX, mHumiMax1);
        args2.putString(Constants.JSET_START, mStartTime1);
        args2.putInt(Constants.JSET_STOP, mStopMin1);
        args2.putInt(Constants.JSET_SW_HOUR, mInterval1);
        args2.putInt(Constants.JSET_SW_MIN, mDuration1);
        args2.putString(Constants.JSON_SETTINGS_STRING, getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING));
        SwitchFragment frag2 = new SwitchFragment();
        frag2.setArguments(args2);

        adapter.addFragment(frag1, getString(R.string.switch1).toUpperCase(Locale.getDefault()));
        adapter.addFragment(frag2, getString(R.string.switch2).toUpperCase(Locale.getDefault()));
        viewPager.setAdapter(adapter);
    }

    private void setupBottomControls() {
        ImageButton hotButton2, hotButton3, hotButton4, hotButton5;

        LinearLayout applyLayout = (LinearLayout)findViewById(R.id.hot_button0);
        applyLayout.setVisibility(View.VISIBLE);

        LinearLayout refreshLayout = (LinearLayout)findViewById(R.id.hot_button1);
        refreshLayout.setVisibility(View.GONE);

        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_0);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_apply, null);
        hotButton1.setImageDrawable(drawable);
        hotButton1.setEnabled(false);
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SwitchTabActivity.this);
                builder.setTitle(R.string.upload_title);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadData(mHouseId, mSettings);
                        hotButton1.setEnabled(false);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                        //PreferenceManager.getDefaultSharedPreferences(SwitchTabActivity.this).edit()
                        //        .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                    }
                });
                builder.create().show();
            }
        });

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotButton1.isEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SwitchTabActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadData(mHouseId, mSettings);
                            Intent intent = new Intent(SwitchTabActivity.this, EntryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SwitchTabActivity.this, EntryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                } else {
                    startActivity(new Intent(getApplicationContext(), EntryActivity.class));
                    finish();
                }
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotButton1.isEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SwitchTabActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadData(mHouseId, mSettings);
                            Intent intent = new Intent(SwitchTabActivity.this, OverviewMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SwitchTabActivity.this, OverviewMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                } else {
                    startActivity(new Intent(getApplicationContext(), OverviewMainActivity.class));
                    finish();
                }
            }
        });

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_logs, null);
        hotButton4.setImageDrawable(drawable);
        hotButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotButton1.isEnabled()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SwitchTabActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadData(mHouseId, mSettings);
                            //Intent intent = new Intent(SwitchTabActivity.this, LogActivity.class);
                            Intent intent = new Intent(SwitchTabActivity.this, LogTypeActivity.class);
                            intent.putExtra(Constants.JSON_ID, mHouseId);
                            intent.putExtra(Constants.JSON_NAME, mTitle);
                            intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            //finish();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Intent intent = new Intent(SwitchTabActivity.this, LogActivity.class);
                            Intent intent = new Intent(SwitchTabActivity.this, LogTypeActivity.class);
                            intent.putExtra(Constants.JSON_ID, mHouseId);
                            intent.putExtra(Constants.JSON_NAME, mTitle);
                            intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            //finish();
                        }
                    });
                    builder.create().show();
                } else {
                    //startActivity(new Intent(getApplicationContext(), LogActivity.class));
                    Intent intent = new Intent(SwitchTabActivity.this, LogTypeActivity.class);
                    intent.putExtra(Constants.JSON_ID, mHouseId);
                    intent.putExtra(Constants.JSON_NAME, mTitle);
                    intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    //finish();
                }
            }
        });

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        Picasso.with(this).load(R.drawable.setting_on).into(hotButton5);
        hotButton5.setActivated(true);
    }

    @Override
    public void onPassData(SwitchSetting switchSetting, int switchNum) {
        mSettings.switches.set(switchNum, switchSetting);
        hotButton1.setEnabled(true);
    }

    private void uploadData(final int houseId, SettingGroup settings) {
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");

                StringBuilder url = new StringBuilder();
                //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                url.append("").append(savedUrl).append("");
                url.append(String.valueOf(params[0]));
                if(params[1] == null) {
                    return null;
                }
                String jsonString = new JsonBuilder()
                        .buildString((SettingGroup)params[1], SwitchTabActivity.this);
                if(jsonString == null) {
                    return null;
                }
                PreferenceManager.getDefaultSharedPreferences(SwitchTabActivity.this).edit()
                        .putString(Constants.JSON_SETTINGS_STRING + String.valueOf(params[0]), jsonString).apply();
                HashMap<String, String> map = new HashMap<>();
                map.put("", jsonString);
                new WebRequest().makeWebServiceCall(
                        url.toString(), WebRequest.POSTRequest, map);
                return null;
            }
        }.execute(houseId, settings);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
