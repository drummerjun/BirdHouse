package com.junyenhuang.birdhouse.carol;

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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.EntryActivity;
import com.junyenhuang.birdhouse.JsonParser;
import com.junyenhuang.birdhouse.LogTypeActivity;
import com.junyenhuang.birdhouse.PasswordActivity;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.SettingActivity;
import com.junyenhuang.birdhouse.adapters.EntryItemAdapter;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.House;
import com.junyenhuang.birdhouse.receiver.ServiceReceiver;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OverviewMainActivity extends AppCompatActivity{
    private static final String TAG = OverviewMainActivity.class.getSimpleName();
    private ServiceReceiver mScheduler = new ServiceReceiver();
    private DialogFragment dateFragment;
    private BroadcastReceiver mReceiver;
    private CarolOverviewAdapter mAdapter;
    private View mProgressView;
    private ImageButton hotButton1, hotButton2, hotButton3, hotButton4, hotButton5;
    private RelativeLayout mPageCounterParent;
    public static TextView mPageCounter, mPagerShadow;
    public ViewPager mPager;
    public ArrayList<House> mHouses;
    public static int mSelectedPage = 0;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.carol_overview);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);
        mProgressView = findViewById(R.id.loading_progress);

        mHouses = new ArrayList<>();
        mReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_UPDATE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction("com.jun.houseOnline");
        intentFilter.addAction("com.jun.houseOffline");
        registerReceiver(mReceiver, intentFilter);
        mPageCounterParent = (RelativeLayout)findViewById(R.id.page_index_parent);
        mPageCounter = (TextView) findViewById(R.id.page_index);
        mPagerShadow = (TextView)findViewById(R.id.page_index_shadow);
        mPager = (ViewPager) findViewById(R.id.myviewpager);
        if(mPager == null) {
            Log.e(TAG, "pager null");
            return;
        }

        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_1);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_refresh, null);
        hotButton1.setImageDrawable(drawable);
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");

                StringBuilder url = new StringBuilder();
                //int userID = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).getInt(Constants.USER_ID, 0);
                //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                int userID = prefs.getInt(Constants.USER_ID, 0);
                url.append("http://").append(savedUrl).append("/api/");
                url.append(Constants.GET_DEVICES).append(String.valueOf(userID));

                new DoWebRequest().execute(url.toString());
                //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_DEVICES + String.valueOf(userID));
            }
        });

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), EntryActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        Picasso.with(this).load(R.drawable.status_on).into(hotButton3);
        hotButton3.setActivated(true);

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_logs, null);
        hotButton4.setImageDrawable(drawable);
        hotButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = 0;
                try {
                    if (mPager != null) {
                        index = mPager.getCurrentItem();
                    }
                    House house = mHouses.get(index);
                    Intent intent = new Intent(OverviewMainActivity.this, LogTypeActivity.class);
                    intent.putExtra(Constants.JSON_ID, house.getId());
                    intent.putExtra(Constants.JSON_NAME, house.getName());
                    intent.putExtra(Constants.JSON_SETTINGS_STRING, house.getSettingString());
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_setting, null);
        hotButton5.setImageDrawable(drawable);
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        String userClearance = prefs.getString(Constants.USER_TYPE, "");
        if(userClearance.equals(Constants.SUPERUSER)) {
            hotButton5.setEnabled(true);
            hotButton5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = 0;
                    try {
                        if (mPager != null) {
                            index = mPager.getCurrentItem();
                        }
                        House house = mHouses.get(index);
                        Intent intent = new Intent(OverviewMainActivity.this, SettingActivity.class);
                        intent.putExtra(Constants.JSON_ID, house.getId());
                        intent.putExtra(Constants.JSON_NAME, house.getName());
                        intent.putExtra(Constants.JSON_SETTINGS_STRING, house.getSettingString());
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            hotButton5.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
        int userID = prefs.getInt(Constants.USER_ID, 0);
        new DoWebRequest().execute("http://" + savedUrl + "/api/"
                + Constants.GET_DEVICES + String.valueOf(userID));

        //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
        //int userID = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).getInt(Constants.USER_ID, 0);
        //new DoWebRequest().execute(Constants.BASE_URL + Constants.GET_DEVICES + String.valueOf(userID));
        //DEBUG_STR
        boolean DEBUG = false;
        if(!DEBUG) {
            mScheduler.set(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //DEBUG_STR
        boolean DEBUG = false;
        if(!DEBUG) {
            mScheduler.cancel(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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
            startActivity(new Intent(OverviewMainActivity.this, PasswordActivity.class));
        } else if(item.getItemId() == R.id.action_change_server) {
            View viewInflated = LayoutInflater.from(OverviewMainActivity.this)
                    .inflate(R.layout.dialog_url, (ViewGroup)findViewById(android.R.id.content), false);
            final EditText url = (EditText)viewInflated.findViewById(R.id.input_url);
            String inputUrl = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE)
                    .getString(Constants.BASE_URL_TAG, "");
            url.setText(inputUrl);

            AlertDialog.Builder builder = new AlertDialog.Builder(OverviewMainActivity.this);
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

    private class DoWebRequest extends AsyncTask<String, Void, List<Fragment>> {
        ArrayList<House> houses = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            showProgress(true);
            super.onPreExecute();
        }

        @Override
        protected List<Fragment> doInBackground(String... params) {
            //DEBUG_STR
            boolean DEBUG = false;

            if(!DEBUG) {
                Log.d("url: ", "> " + params[0]);
                String jsonStr = new WebRequest().makeWebServiceCall(params[0], WebRequest.GETRequest);
                Log.d("Response: ", "> " + jsonStr);
                PreferenceManager.getDefaultSharedPreferences(OverviewMainActivity.this).edit().clear().apply();
                JsonParser parser = new JsonParser(OverviewMainActivity.this);
                houses = parser.parse(jsonStr);
            } else {
                JsonParser parser = new JsonParser(OverviewMainActivity.this);
                houses = parser.parse(Constants.DEBUG_STR);
                //parser.parseSettings(houses.get(0).getSettingString(), houses.get(0).getId());
            }
            return buildFragments(houses);
        }

        @Override
        protected void onPostExecute(List<Fragment> resultFragments) {
            super.onPostExecute(resultFragments);
            refreshContents(houses, resultFragments);
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            super.onCancelled();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mPageCounterParent.setVisibility(show ? View.GONE : View.VISIBLE);
            //mPageCounter.setVisibility(show ? View.GONE : View.VISIBLE);
            //mPagerShadow.setVisibility(show ? View.GONE : View.VISIBLE);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPageCounterParent.setVisibility(show ? View.GONE : View.VISIBLE);
                    //mPageCounter.setVisibility(show ? View.GONE : View.VISIBLE);
                    //mPagerShadow.setVisibility(show ? View.GONE : View.VISIBLE);
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mPageCounterParent.setVisibility(show ? View.GONE : View.VISIBLE);
            //mPageCounter.setVisibility(show ? View.GONE : View.VISIBLE);
            //mPagerShadow.setVisibility(show ? View.GONE : View.VISIBLE);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshContents(ArrayList<House> list, List<Fragment> frags) {
        if(mHouses == null || frags == null) {
            return;
        }

        mHouses.clear();
        mHouses.addAll(list);
        if(mAdapter == null) {
            mAdapter = new CarolOverviewAdapter(this, getSupportFragmentManager(), frags, list);
            mPager.setAdapter(mAdapter);
            mPager.addOnPageChangeListener(mAdapter);
        } else {
            mAdapter.updateData(list, frags);
            mPager.setAdapter(mAdapter);
            try {
                mPager.setCurrentItem(mSelectedPage);
            } catch(NullPointerException e) {
                mPager.setCurrentItem(0);
            } finally {
                mSelectedPage = 0;
            }
        }

        if(list == null) {
            mPageCounter.setText(getString(R.string.error_msg));
            mPagerShadow.setText(getString(R.string.error_msg));
        } else {
            mPageCounter.setText(String.valueOf(mPager.getCurrentItem()+1) + "/" + list.size());
            mPagerShadow.setText(String.valueOf(mPager.getCurrentItem()+1) + "/" + list.size());
            String entry = getIntent().getStringExtra(Constants.ENTRY_TAG);
            if(entry != null && entry.equals(EntryItemAdapter.class.getSimpleName())) {
                int id = getIntent().getIntExtra(Constants.JSON_ID, -1);
                String name = getIntent().getStringExtra(Constants.JSON_NAME);
                for(int i = 0; i < mHouses.size(); i++) {
                    House house = mHouses.get(i);
                    if(house.getId() == id){
                        if (mPager != null) {
                            try {
                                mSelectedPage = i;
                                mPager.setCurrentItem(i);
                            } catch(IndexOutOfBoundsException e) {
                                mPager.setCurrentItem(0);
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private List<Fragment> buildFragments(ArrayList<House> list) {
        if(list == null) {
            return null;
        }

        List<Fragment> fragments = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            //Bundle b = new Bundle();
            //fragments.add(Fragment.instantiate(this, HouseFragment.class.getName(), b));
            fragments.add(HouseFragment.newInstance(this, list.get(i)));
        }
        return fragments;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if(intent.getAction().equals("com.jun.houseOnline")) {
                Log.d(TAG, "houseOnline");
                if(hotButton5 != null) hotButton5.setEnabled(true);
            } else if(intent.getAction().equals("com.jun.houseOffline")) {
                Log.d(TAG, "houseOffline");
                if(hotButton5 != null) hotButton5.setEnabled(false);
            } else {
                //DEBUG_STR
                boolean DEBUG = false;
                String jsonStr;
                if (!DEBUG) {
                    jsonStr = intent.getStringExtra(Constants.EXTRA_JOUTPUT);
                } else {
                    jsonStr = Constants.DEBUG_STR;
                }

                new AsyncTask<String, Void, List<Fragment>>() {
                    ArrayList<House> updatedHouses;

                    @Override
                    protected List<Fragment> doInBackground(String... params) {
                        updatedHouses = new JsonParser(OverviewMainActivity.this).parse(params[0]);
                        return buildFragments(updatedHouses);
                    }

                    @Override
                    protected void onPostExecute(List<Fragment> frags) {
                        super.onPostExecute(frags);
                        refreshContents(updatedHouses, frags);
                        //refreshContents(houses);
                    }
                }.execute(jsonStr);
            }
        }
    }
}
