package com.junyenhuang.birdhouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.junyenhuang.birdhouse.adapters.EntryItemAdapter;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.EntryItem;
import com.junyenhuang.birdhouse.items.HouseEvent;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EntryActivity extends AppCompatActivity {
    private static final String TAG =  EntryActivity.class.getSimpleName();
    private View mProgressView;
    private RecyclerView mRecList;
    private EntryItemAdapter mAdapter;

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name).setMessage(R.string.exit_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //onBackPressed();
                        dialog.dismiss();
                        finish();
                    }
                })
                .create().show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                        + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1)
                        + "/" + new DecimalFormat("00").format(calendar.get(Calendar.DATE))
        );
        sb.append("");
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
        int userID = prefs.getInt(Constants.USER_ID, 0);

        //int userID = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).getInt(Constants.USER_ID, 0);
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                boolean DEBUG = false;
                if (!DEBUG) {
                    String jsonStr = new WebRequest().makeWebServiceCall(params[0], WebRequest.GETRequest);
                    Log.d("Response: ", "> " + jsonStr);
                    JsonParser parser = new JsonParser(EntryActivity.this);
                    parser.parse(jsonStr);
                } else {
                }
                return null;
            }
        }.execute("" + savedUrl + "" + Constants.GET_DEVICES + String.valueOf(userID));
        //}.execute(Constants.BASE_URL + Constants.GET_DEVICES + String.valueOf(userID));
        //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_LOGS + sb.toString());
        new DoWebRequest().execute("" + savedUrl + "" + sb.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_change_pwd) {
            startActivity(new Intent(EntryActivity.this, PasswordActivity.class));
        } else if(item.getItemId() == R.id.action_change_server) {
            View viewInflated = LayoutInflater.from(EntryActivity.this)
                    .inflate(R.layout.dialog_url, (ViewGroup)findViewById(android.R.id.content), false);
            final EditText url = (EditText)viewInflated.findViewById(R.id.input_url);
            String inputUrl = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE)
                    .getString(Constants.BASE_URL_TAG, "");
            url.setText(inputUrl);

            AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
            builder.setView(viewInflated);
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!url.getText().toString().isEmpty()) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).edit();
                        String inputUrl = url.getText().toString();
                        inputUrl = inputUrl.replace("", "");
                        String lastChar = inputUrl.substring(inputUrl.length() - 1);
                        if(lastChar.equals("")) {
                            inputUrl = inputUrl.substring(0, inputUrl.length() - 1 - 1);
                        }
                        editor.putString(Constants.BASE_URL_TAG, inputUrl);
                        editor.apply();
                    }
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
        return super.onOptionsItemSelected(item);
    }

    private void refreshContents(ArrayList<EntryItem> events) {
        if(mAdapter == null) {
            mAdapter = new EntryItemAdapter(EntryActivity.this, events);
            mRecList.setAdapter(mAdapter);
        } else {
            mAdapter.updateData(events);
        }
    }

    private class DoWebRequest extends AsyncTask<String, Void, ArrayList<EntryItem>> {
        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<EntryItem> doInBackground(String... params) {
            //DEBUG_STR
            boolean DEBUG = false;

            if(!DEBUG) {
                String jsonStr = new WebRequest().makeWebServiceCall(params[0], WebRequest.GETRequest);
                Log.d("Response: ", "> " + jsonStr);
                JsonParser parser = new JsonParser(getApplicationContext());
                //houseEvents = parser.parseEvents(jsonStr);
                return buildAlarmEvents(parser.parseEvents(jsonStr, -1, -1, ""));
                // TODO coding complete, pending verification
            } else {
                JsonParser parser = new JsonParser(getApplicationContext());
                //events = parser.parse(Constants.DEBUG_STR);
                //parser.parseSettings(houses.get(0).getSettingString(), houses.get(0).getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<EntryItem> results) {
            super.onPostExecute(results);
            if(results.isEmpty()) {
                EntryItem e = new EntryItem();
                e.setHouseName(getString(R.string.zero_critical));
                results.add(e);
            }
            refreshContents(results);
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            super.onCancelled();
        }
    }

    private ArrayList<EntryItem> buildAlarmEvents(ArrayList<HouseEvent> events) {
        ArrayList<EntryItem> localAlarms = new ArrayList<>();
        for(HouseEvent event : events) {
            int insert_index = -1;
            if(event.getEventKey().equals("") && (event.getPriority() > 0)) {
                for (int i = 0; i < localAlarms.size(); i++) {
                    if (event.getHouseKey() == localAlarms.get(i).getHouseID()) {
                        insert_index = i;
                        break;
                    }
                }
                //Log.d(TAG, "insert_index=" + insert_index);
                if (compareTimeWithinFiveMinutes(event.getTimeString())) { //current set at 1hour for debug purposes
                    if (insert_index > -1) {
                        localAlarms.get(insert_index).getCriticalEvents().add(0, event);
                        if(event.getPriority() > localAlarms.get(insert_index).getHighestPriority()) {
                            localAlarms.get(insert_index).setHighestPriority(event.getPriority());
                        }
                    } else { // new alarm event
                        EntryItem alarmItem = new EntryItem();
                        alarmItem.setHouseID(event.getHouseKey());
                        alarmItem.setHighestPriority(event.getPriority());
                        alarmItem.getCriticalEvents().add(event);
                        alarmItem.setHouseName(event.getHouseName());
                        localAlarms.add(alarmItem);
                    }
                }
            }
        }
        for (EntryItem alarm : localAlarms) {
            Collections.sort(alarm.getCriticalEvents(), new SingleEventComparator());
        }
        Collections.sort(localAlarms, new AlarmComparator());
        return localAlarms;
    }

    private class SingleEventComparator implements Comparator<HouseEvent> {
        @Override
        public int compare(HouseEvent left, HouseEvent right) {
            return right.getPriority() - left.getPriority();
        }
    }

    private class AlarmComparator implements Comparator<EntryItem> {
        @Override
        public int compare(EntryItem left, EntryItem right) {
            return right.getHighestPriority() - left.getHighestPriority();
        }
    }

    private boolean compareTimeWithinFiveMinutes(String timeString) {
        //String givenDateString = "Tue Apr 23 16:08:28 GMT+05:30 2013";
        //2016-11-21 13:33:36
        long fiveMinMarkMillis = TimeUnit.MINUTES.toMillis(30);
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date mDate = sdf.parse(timeString);
            long eventTimeInMilliseconds = mDate.getTime();
            if((currentTimeMillis - eventTimeInMilliseconds) <= fiveMinMarkMillis) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        mProgressView = findViewById(R.id.loading_progress);
        mRecList = (RecyclerView)findViewById(R.id.recycler);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        ImageButton hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_1);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_refresh, null);
        hotButton1.setImageDrawable(drawable);
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                StringBuilder sb = new StringBuilder();
                sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                                + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1)
                                + "/" + new DecimalFormat("00").format(calendar.get(Calendar.DATE))
                );
                sb.append("?key=alarm");

                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
                new DoWebRequest().execute("" + savedUrl + "" + sb.toString());

                //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_LOGS + sb.toString());
            }
        });

        ImageButton hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        Picasso.with(this).load(R.drawable.event_on).into(hotButton2);
        hotButton2.setActivated(true);

        ImageButton hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EntryActivity.this, OverviewMainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        ImageButton hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        Picasso.with(this).load(R.drawable.total_none).into(hotButton4);
        hotButton4.setEnabled(false);

        ImageButton hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        Picasso.with(this).load(R.drawable.setting_none).into(hotButton5);
        hotButton5.setEnabled(false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
