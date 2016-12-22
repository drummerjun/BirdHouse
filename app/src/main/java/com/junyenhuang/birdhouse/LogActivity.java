package com.junyenhuang.birdhouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.junyenhuang.birdhouse.adapters.EnvironmentIndexAdapter;
import com.junyenhuang.birdhouse.adapters.EventsAdapter;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.EnvironmentIndex;
import com.junyenhuang.birdhouse.items.HouseEvent;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class LogActivity extends AppCompatActivity {
    private static final String TAG = LogActivity.class.getSimpleName();
    private ArrayList<EnvironmentIndex> newList;
    private View mProgressView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecList;
    private String mDateString, mTitle;
    private int mHouseID, mElementID = -1;
    private String mFilterType = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        mHouseID = getIntent().getIntExtra(Constants.JSON_ID, -1);
        mTitle = getIntent().getStringExtra(Constants.JSON_NAME);
        mElementID = getIntent().getIntExtra("ELEMENT_ID", -1);
        mFilterType = getIntent().getStringExtra("FILTER");
        initConstantViews();

        if(mElementID == 2) {
            mElementID = 1;
        } else if(mElementID == 7 || mElementID == 8) {
            mElementID = 6;
        }

        Calendar calendar = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        if(mElementID == 9) {
            sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                    + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1));
                    //+ "/" + String.valueOf(calendar.get(Calendar.DATE));
        } else {
            sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                    + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1)
                    + "/" + new DecimalFormat("00").format(calendar.get(Calendar.DATE))
            );
        }
        mDateString = sb.substring(1);
        boolean first = true;
        if(mHouseID > -1) {
            sb.append("?key2=" + mHouseID);
            first = false;
        }

        if(first) {
            sb.append("?key1=" + mElementID);
        } else {
            sb.append("&key1=" + mElementID);
        }

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
        StringBuilder url = new StringBuilder();
        url.append("http://").append(savedUrl).append("/api/logs").append(sb.toString());
        //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_LOGS + sb.toString());
        new DoWebRequest().execute(url.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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
            startActivity(new Intent(LogActivity.this, PasswordActivity.class));
        } else if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if(item.getItemId() == R.id.action_change_server) {
            View viewInflated = LayoutInflater.from(LogActivity.this)
                    .inflate(R.layout.dialog_url, (ViewGroup)findViewById(android.R.id.content), false);
            final EditText url = (EditText)viewInflated.findViewById(R.id.input_url);
            String inputUrl = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE)
                    .getString(Constants.BASE_URL_TAG, "");
            url.setText(inputUrl);

            AlertDialog.Builder builder = new AlertDialog.Builder(LogActivity.this);
            builder.setView(viewInflated);
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!url.getText().toString().isEmpty()) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).edit();
                        String inputUrl = url.getText().toString();
                        inputUrl = inputUrl.replace("http://", "");
                        String lastChar = inputUrl.substring(inputUrl.length() - 1);
                        if(lastChar.equals("/")) {
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

    private void showDatePickerDialog() {
        DialogFragment dateFragment = new DatePickerFragment();
        ((DatePickerFragment)dateFragment).setContext(LogActivity.this);
        dateFragment.show(getFragmentManager(), "datePicker");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.ACTION_DATE_SELECTED)) {
                String dateString = intent.getStringExtra(Constants.EXTRA_DATE);
                Log.d(TAG, "ACTION_DATE_SELECTED received\n" + dateString);
                mDateString = dateString.substring(1);

                StringBuilder sb = new StringBuilder();
                boolean first = true;
                sb.append(dateString);
                if(mHouseID > -1) {
                    sb.append("?key2=" + mHouseID);
                    first = false;
                }

                if(mElementID > -1) {
                    if(first) {
                        sb.append("?key1=" + mElementID);
                    } else {
                        sb.append("&key1=" + mElementID);
                    }
                }
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
                String url = "http://" + savedUrl + "/api/logs" + sb.toString();
                new DoWebRequest().execute(url);
                //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_LOGS + sb.toString());
            }
        }
    };

    private void refreshContents(ArrayList<HouseEvent> events) {
        if(mElementID == 6 || mElementID == 7 || mElementID == 8) {
            if (mAdapter == null) {
                mAdapter = new EnvironmentIndexAdapter(newList, LogActivity.this);
                mRecList.setAdapter(mAdapter);
            } else {
                if(mAdapter instanceof EnvironmentIndexAdapter) {
                    ((EnvironmentIndexAdapter) mAdapter).updateData(newList);
                } else {
                    mAdapter = new EnvironmentIndexAdapter(newList, LogActivity.this);
                    mRecList.setAdapter(mAdapter);
                }
            }
        } else {
            if (mAdapter == null) {
                mAdapter = new EventsAdapter(events, LogActivity.this);
                mRecList.setAdapter(mAdapter);
            } else {
                if(mAdapter instanceof EventsAdapter) {
                    ((EventsAdapter) mAdapter).updateData(events);
                } else {
                    mAdapter = new EventsAdapter(events, LogActivity.this);
                    mRecList.setAdapter(mAdapter);
                }
            }
        }
    }

    private class DoWebRequest extends AsyncTask<String, Void, ArrayList<HouseEvent>> {
        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<HouseEvent> doInBackground(String... params) {
            //DEBUG_STR
            boolean DEBUG = false;
            if(!DEBUG) {
                Log.d("url: ", "> " + params[0]);
                String jsonStr = new WebRequest().makeWebServiceCall(params[0], WebRequest.GETRequest);
                Log.d("Response: ", "> " + jsonStr);
                JsonParser parser = new JsonParser(LogActivity.this);
                ArrayList<HouseEvent> results = parser.parseEvents(jsonStr, mHouseID, mElementID, mFilterType);
                Collections.reverse(results);

                if(mElementID == 6 || mElementID == 7 || mElementID == 8) {
                    populateEnvironmentList(results);
                }
                /*
                if(mElementID == 9) {
                    ArrayList<HouseEvent> am_count = new ArrayList<>();
                    ArrayList<HouseEvent> pm_count = new ArrayList<>();
                    ArrayList<HouseEvent> day_cout = new ArrayList<>();
                    boolean isMorning = false;
                    int globalYear = -1;
                    int globalDayOfYear = -1;
                    for (HouseEvent event : results) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date mDate = sdf.parse(event.getTimeString());
                            long eventTimeInMilliseconds = mDate.getTime();

                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(eventTimeInMilliseconds);

                            int year = c.get(Calendar.YEAR);
                            //int month = c.get(Calendar.MONTH) + 1;
                            //int day = c.get(Calendar.DAY_OF_MONTH);
                            int date = c.get(Calendar.DAY_OF_YEAR);
                            if(globalYear == -1) {
                                globalYear = year;
                            }
                            if(globalDayOfYear == -1) {
                                globalDayOfYear = date;
                            }

                            int hours = c.get(Calendar.HOUR_OF_DAY);
                            //int minutes = c.get(Calendar.MINUTE);
                            //int seconds = c.get(Calendar.SECOND);
                            if(globalYear == year && globalDayOfYear == date) {
                                if (hours < 12) {
                                    am_count.remove(0);
                                    am_count.add(0, event);
                                    // exit
                                } else {
                                    pm_count.remove(0);
                                    pm_count.add(0, event);
                                    // entry
                                }
                            } else {
                                globalYear = year;
                                globalDayOfYear = date;
                                if (hours < 12) {
                                    am_count.add(0, event);
                                    // exit
                                } else {
                                    pm_count.add(0, event);
                                    // entry
                                }
                            }
                        } catch (ParseException e) {
                        }
                    }
                }
                */

                return results;
            } else {
                JsonParser parser = new JsonParser(LogActivity.this);
                //events = parser.parse(Constants.DEBUG_STR);
                //parser.parseSettings(houses.get(0).getSettingString(), houses.get(0).getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HouseEvent> results) {
            super.onPostExecute(results);
            if(results.isEmpty()) {
                HouseEvent e = new HouseEvent();
                e.setTimeString(mDateString);
                e.setDescription(getString(R.string.no_event));
                e.setElementKey(mElementID);
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

        private void populateEnvironmentList(ArrayList<HouseEvent> results) {
            newList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            int globalYear = -1;
            int globalDayOfYear = -1;
            int globalHour = -1;

            EnvironmentIndex eIndex = new EnvironmentIndex();
            for (HouseEvent event : results) {
                if(eIndex.getTimeString().isEmpty()) {
                    //eIndex = new EnvironmentIndex();
                    try {
                        Date mDate = sdf.parse(event.getTimeString());
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(mDate.getTime());

                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH) + 1;
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        int date = c.get(Calendar.DAY_OF_YEAR);
                        if (globalYear == -1) {
                            globalYear = year;
                        }
                        if (globalDayOfYear == -1) {
                            globalDayOfYear = date;
                        }

                        int hours = c.get(Calendar.HOUR_OF_DAY);
                        //int minutes = c.get(Calendar.MINUTE);
                        //int seconds = c.get(Calendar.SECOND);
                        if (globalHour == -1) {
                            globalHour = hours;
                        }
                        eIndex.setTimeString(year + "/"
                                + new DecimalFormat("00").format(month) + "/"
                                + new DecimalFormat("00").format(day) + " "
                                + new DecimalFormat("00").format(hours)
                                + ":00"
                        );

                        if(event.getEventValue().toLowerCase().contains("c")) {
                            String tempString = event.getEventValue().replace("C", (char)0x00B0 + "C");
                            eIndex.setTemperature(tempString);
                            newList.add(eIndex);
                        } else if(event.getEventValue().contains("%")) {
                            eIndex.setHumidity(event.getEventValue());
                            newList.add(eIndex);
                        } else if(event.getEventValue().toLowerCase().contains("ppm")) {
                            eIndex.setNh3(event.getEventValue());
                            newList.add(eIndex);
                        }
                    } catch (ParseException e) {
                    }
                } else {
                    try {
                        Date mDate = sdf.parse(event.getTimeString());
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(mDate.getTime());
                        int year = c.get(Calendar.YEAR);
                        int date = c.get(Calendar.DAY_OF_YEAR);
                        int hours = c.get(Calendar.HOUR_OF_DAY);

                        if(globalYear == year && globalDayOfYear == date && globalHour == hours) {
                            int index = newList.lastIndexOf(eIndex);
                            eIndex = newList.get(index);
                            if(event.getEventValue().toLowerCase().contains("c")) {
                                if(eIndex.getTemperature().isEmpty()) {
                                    String tempString = event.getEventValue().replace("C", (char)0x00B0 + "C");
                                    eIndex.setTemperature(tempString);
                                    //eIndex.setTemperature(event.getEventValue());
                                    newList.set(index, eIndex);
                                }
                            } else if(event.getEventValue().contains("%")) {
                                if(eIndex.getHumidity().isEmpty()) {
                                    eIndex.setHumidity(event.getEventValue());
                                    newList.set(index, eIndex);
                                }
                            } else if(event.getEventValue().toLowerCase().contains("ppm")) {
                                if(eIndex.getNh3().isEmpty()) {
                                    eIndex.setNh3(event.getEventValue());
                                    newList.set(index, eIndex);
                                }
                            }
                        } else {
                            eIndex = new EnvironmentIndex();
                            globalYear = year;
                            globalDayOfYear = date;
                            globalHour = hours;
                            eIndex.setTimeString(year + "/"
                                    + new DecimalFormat("00").format(c.get(Calendar.MONTH) + 1) + "/"
                                    + new DecimalFormat("00").format(c.get(Calendar.DAY_OF_MONTH)) + " "
                                    + new DecimalFormat("00").format(hours) + ":00"
                            );

                            if (event.getEventValue().toLowerCase().contains("c")) {
                                String tempString = event.getEventValue().replace("C", (char)0x00B0 + "C");
                                eIndex.setTemperature(tempString);
                                //eIndex.setTemperature(event.getEventValue());
                                newList.add(eIndex);
                            } else if (event.getEventValue().contains("%")) {
                                eIndex.setHumidity(event.getEventValue());
                                newList.add(eIndex);
                            } else if (event.getEventValue().toLowerCase().contains("ppm")) {
                                eIndex.setNh3(event.getEventValue());
                                newList.add(eIndex);
                            }
                        }
                    } catch (ParseException e) {
                    }
                }
            }
        }
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

    private void initConstantViews() {
        //toolbar setup
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerview setup
        mProgressView = findViewById(R.id.loading_progress);
        mRecList = (RecyclerView)findViewById(R.id.recycler);
        mRecList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecList.setLayoutManager(llm);

        //local broadcast listener for date update
        IntentFilter filter = new IntentFilter(Constants.ACTION_DATE_SELECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        //static bottom controls
        ImageButton hotButton1, hotButton2, hotButton3, hotButton4, hotButton5;
        LinearLayout applyLayout = (LinearLayout)findViewById(R.id.hot_button0);
        applyLayout.setVisibility(View.GONE);

        LinearLayout refreshLayout = (LinearLayout)findViewById(R.id.hot_button1);
        refreshLayout.setVisibility(View.VISIBLE);
        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_1);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_refresh, null);
        hotButton1.setImageDrawable(drawable);
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                StringBuilder sb = new StringBuilder();
                if(mElementID == 9) {
                    sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                            + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1));
                    //+ "/" + String.valueOf(calendar.get(Calendar.DATE));
                } else {
                    sb.append("/" + String.valueOf(calendar.get(Calendar.YEAR))
                            + "/" + new DecimalFormat("00").format(calendar.get(Calendar.MONTH) + 1)
                            + "/" + new DecimalFormat("00").format(calendar.get(Calendar.DATE))
                    );
                }
                Log.d(TAG, "sb=" + sb.toString());
                mDateString = sb.substring(1);

                boolean first = true;
                if(mHouseID > -1) {
                    sb.append("?key2=" + mHouseID);
                    first = false;
                }

                if(mElementID > -1) {
                    if(first) {
                        sb.append("?key1=" + mElementID);
                    } else {
                        sb.append("&key1=" + mElementID);
                    }
                }

                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
                String url = "http://" + savedUrl + "/api/logs" + sb.toString();
                new DoWebRequest().execute(url);
                //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_LOGS + sb.toString());
            }
        });

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogActivity.this, EntryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                //overridePendingTransition(0, 0);
                //finish();
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogActivity.this, OverviewMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                //overridePendingTransition(0, 0);
                //finish();
            }
        });

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        Picasso.with(this).load(R.drawable.total_on).into(hotButton4);
        hotButton4.setActivated(true);

        LinearLayout dateLayout = (LinearLayout)findViewById(R.id.hot_button6);
        dateLayout.setVisibility(View.VISIBLE);
        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_6);
        hotButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mElementID == 9) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.dialog_numberpickers,
                            (ViewGroup)findViewById(android.R.id.content),
                            false);

                    final NumberPicker yearPicker = (NumberPicker)layout.findViewById(R.id.numberPicker);
                    final NumberPicker monthPicker = (NumberPicker)layout.findViewById(R.id.numberPicker2);
                    TextView titleTV = (TextView)layout.findViewById(R.id.fake_dialog_title);
                    TextView yearTV = (TextView)layout.findViewById(R.id.hourTextView);
                    TextView monthTV = (TextView)layout.findViewById(R.id.minuteTextView);
                    titleTV.setText(getString(R.string.action_dateselect));
                    yearTV.setText(R.string.year);
                    monthTV.setText(R.string.month);

                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    yearPicker.setMinValue(2016);
                    yearPicker.setMaxValue(year);
                    yearPicker.setValue(year);

                    monthPicker.setMinValue(1);
                    monthPicker.setMaxValue(12);
                    monthPicker.setValue(c.get(Calendar.MONTH) + 1);

                    AlertDialog.Builder builder = new AlertDialog.Builder(LogActivity.this);
                    builder.setView(layout);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int year = yearPicker.getValue();
                            int month = monthPicker.getValue();

                            StringBuilder sb = new StringBuilder();
                            sb.append("/" + String.valueOf(year)
                                    + "/" + new DecimalFormat("00").format(month + 1)
                            );

                            Intent intent = new Intent(Constants.ACTION_DATE_SELECTED);
                            intent.putExtra(Constants.EXTRA_DATE, sb.toString());
                            LocalBroadcastManager.getInstance(LogActivity.this).sendBroadcast(intent);
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
                    showDatePickerDialog();
                }
            }
        });
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_date, null);
        hotButton5.setImageDrawable(drawable);
        LinearLayout settingLayout = (LinearLayout)findViewById(R.id.hot_button5);
        settingLayout.setVisibility(View.GONE);
    }
}
