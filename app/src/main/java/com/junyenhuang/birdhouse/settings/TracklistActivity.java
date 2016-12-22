package com.junyenhuang.birdhouse.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.junyenhuang.birdhouse.adapters.TracklistAdapter;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.database.DBHandler;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.Mp3Info;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class TracklistActivity extends AppCompatActivity {
    private static final String TAG = TracklistActivity.class.getSimpleName();
    private ArrayList<Mp3Info> oldList;
    private TracklistAdapter mAdapter;// = new TracklistAdapter(mList);
    private RecyclerView mRecList;
    private DBHandler db;
    private static int mHouseId = -1;
    private static SettingGroup mSettings;
    private static String mSettingsString, mTitle;
    private ImageButton hotButton1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(db != null) {
            db.close();
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settinglist);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.birdhouse.tracksEDITED");
        registerReceiver(mReceiver, filter);

        initViews();
        mHouseId = getIntent().getIntExtra(Constants.JSON_ID, -1);
        mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
        mTitle = getIntent().getStringExtra(Constants.JSON_NAME);
        Log.d(TAG, "id=" + mHouseId);
        db = new DBHandler(getApplicationContext());
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                oldList = db.getTracklist((Integer)params[1]);
                Log.d(TAG, "id=" + params[1] + " size=" + oldList.size());
                JsonParser parser = new JsonParser(TracklistActivity.this);
                mSettings = parser.parseSettings(params[0].toString(), (Integer)params[1], false);
                return null;
            }
        }.execute(mSettingsString, mHouseId);

        mAdapter = new TracklistAdapter(getApplicationContext(), db.getTracklist(mHouseId));
        mRecList = (RecyclerView)findViewById(R.id.recycler);
        //recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);
        mRecList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mHouseId > -1) {
            new AsyncTask<Integer, Void, ArrayList<Mp3Info>>() {
                @Override
                protected ArrayList<Mp3Info> doInBackground(Integer... params) {
                    return db.getTracklist(params[0]);
                }

                @Override
                protected void onPostExecute(ArrayList<Mp3Info> results) {
                    super.onPostExecute(results);
                    mAdapter.updateData(results);
                }
            }.execute(mHouseId);
        }
        boolean edited = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("TRACKS_EDITED", false);
        hotButton1.setEnabled(edited);
    }

    @Override
    public void onBackPressed() {
        if(!hotButton1.isEnabled()) {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        } else {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.upload_db).setMessage(R.string.upload_message)
                    .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.setTracklist(mHouseId, oldList);
                            dialog.dismiss();
                            PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                                    .putBoolean("TRACKS_EDITED", false).apply();
                            TracklistActivity.super.onBackPressed();
                            overridePendingTransition(0, 0);
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            oldList = null;
                            uploadData(mHouseId, mSettings);
                            //onBackPressed();
                            dialog.dismiss();
                            TracklistActivity.super.onBackPressed();
                            overridePendingTransition(0, 0);
                        }
                    })
                    .create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        menu.findItem(R.id.action_new).setVisible(true);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("TRACKS_EDITED", true).apply();
            hotButton1.setEnabled(true);
            Mp3Info mp3 = new Mp3Info();
            mp3.setHouseId(mHouseId);
            new AsyncTask<Object, Void, ArrayList<Mp3Info>>() {
                @Override
                protected ArrayList<Mp3Info> doInBackground(Object... params) {
                    db.addTrack((Mp3Info)params[1]);
                    return db.getTracklist((Integer)params[0]);
                }

                @Override
                protected void onPostExecute(ArrayList<Mp3Info> results) {
                    super.onPostExecute(results);
                    int count = mAdapter.updateData(results);
                    Log.d(TAG, "db.trackCount=" + count);
                    mRecList.scrollToPosition(count - 1);
                }
            }.execute(mHouseId, mp3);

            //int count = mAdapter.updateData(db.getTracklist(mHouseId));
            //Log.d(TAG, "db.trackCount=" + count);
            //mRecList.scrollToPosition(count - 1);
        } else if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadData(int houseId, SettingGroup settings) {
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");

                StringBuilder url = new StringBuilder();
                //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                url.append("http://").append(savedUrl).append("/api/settings/");
                url.append(String.valueOf(params[0]));
                Log.d(TAG, "post URL=" + url);
                if(params[1] == null) {
                    return null;
                }
                //SettingGroup settings = new JsonParser(getApplicationContext())
                //        .parseSettings(params[1].toString(), mHouseId, false);
                String jsonString = new JsonBuilder()
                        .buildString((SettingGroup)params[1], TracklistActivity.this);
                if(jsonString == null) {
                    return null;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put("settings", jsonString);
                new WebRequest().makeWebServiceCall(
                        url.toString(), WebRequest.POSTRequest, map);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                hotButton1.setEnabled(false);
                PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                        .putBoolean("TRACKS_EDITED", false).apply();
                super.onPostExecute(aVoid);
            }
        }.execute(houseId, settings);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageButton hotButton2, hotButton3, hotButton4, hotButton5;

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
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TracklistActivity.this);
                builder.setTitle(R.string.upload_db).setMessage(R.string.upload_message)
                        .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.setTracklist(mHouseId, oldList);
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                oldList = null;
                                uploadData(mHouseId, mSettings);
                                //onBackPressed();
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotButton1.isEnabled()) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TracklistActivity.this);
                    builder.setTitle(R.string.upload_db).setMessage(R.string.upload_message)
                            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.setTracklist(mHouseId, oldList);
                                    dialog.dismiss();
                                    PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                                            .putBoolean("TRACKS_EDITED", false).apply();
                                    startActivity(new Intent(TracklistActivity.this, EntryActivity.class));
                                    finish();
                                }
                            })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    oldList = null;
                                    uploadData(mHouseId, mSettings);
                                    //onBackPressed();
                                    dialog.dismiss();
                                    startActivity(new Intent(TracklistActivity.this, EntryActivity.class));
                                    finish();
                                }
                            })
                            .create().show();
                } else {
                    startActivity(new Intent(TracklistActivity.this, EntryActivity.class));
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
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TracklistActivity.this);
                    builder.setTitle(R.string.upload_db).setMessage(R.string.upload_message)
                            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.setTracklist(mHouseId, oldList);
                                    dialog.dismiss();
                                    PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                                            .putBoolean("TRACKS_EDITED", false).apply();
                                    startActivity(new Intent(TracklistActivity.this, OverviewMainActivity.class));
                                    finish();
                                }
                            })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    oldList = null;
                                    uploadData(mHouseId, mSettings);
                                    //onBackPressed();
                                    dialog.dismiss();
                                    startActivity(new Intent(TracklistActivity.this, OverviewMainActivity.class));
                                    finish();
                                }
                            })
                            .create().show();
                } else {
                    startActivity(new Intent(TracklistActivity.this, OverviewMainActivity.class));
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
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TracklistActivity.this);
                    builder.setTitle(R.string.upload_db).setMessage(R.string.upload_message)
                            .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.setTracklist(mHouseId, oldList);
                                    dialog.dismiss();
                                    PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                                            .putBoolean("TRACKS_EDITED", false).apply();
                                    //startActivity(new Intent(TracklistActivity.this, LogActivity.class));
                                    Intent intent = new Intent(TracklistActivity.this, LogTypeActivity.class);
                                    intent.putExtra(Constants.JSON_ID, mHouseId);
                                    intent.putExtra(Constants.JSON_NAME, mTitle);
                                    intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    //finish();
                                }
                            })
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    oldList = null;
                                    uploadData(mHouseId, mSettings);
                                    //onBackPressed();
                                    dialog.dismiss();
                                    Intent intent = new Intent(TracklistActivity.this, LogTypeActivity.class);
                                    intent.putExtra(Constants.JSON_ID, mHouseId);
                                    intent.putExtra(Constants.JSON_NAME, mTitle);
                                    intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    //finish();
                                }
                            })
                            .create().show();
                } else {
                    Intent intent = new Intent(TracklistActivity.this, LogTypeActivity.class);
                    intent.putExtra(Constants.JSON_ID, mHouseId);
                    intent.putExtra(Constants.JSON_NAME, mTitle);
                    intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //finish();
                }
            }
        });

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        Picasso.with(this).load(R.drawable.setting_on).into(hotButton5);
        hotButton5.setActivated(true);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.birdhouse.tracksEDITED")) {
                PreferenceManager.getDefaultSharedPreferences(TracklistActivity.this).edit()
                        .putBoolean("TRACKS_EDITED", true).apply();
                hotButton1.setEnabled(true);
            }
        }
    };
}
