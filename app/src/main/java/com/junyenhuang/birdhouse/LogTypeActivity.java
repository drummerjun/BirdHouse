package com.junyenhuang.birdhouse;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Calendar;

public class LogTypeActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = LogTypeActivity.class.getSimpleName();
    private static int mHouseId = -1;
    private static String mTitle = "";
    private static String mSettingString;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LogTypeActivity.this, OverviewMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_type2);

        Intent intent = getIntent();
        mHouseId = intent.getIntExtra(Constants.JSON_ID, -1);
        mTitle = intent.getStringExtra(Constants.JSON_NAME);
        mSettingString = intent.getStringExtra(Constants.JSON_SETTINGS_STRING);
        initViews();
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
        Intent intent = new Intent(LogTypeActivity.this, LogActivity.class);
        int key1 = -1;
        switch (v.getId()) {
            case R.id.sw_button_1:
                key1 = 1;
                break;
            case R.id.sw_button_2:
                key1 = 6;
                break;
            case R.id.sw_button_3:
                key1 = 3;
                break;
            case R.id.sw_button_4:
                key1 = 4;
                break;
            case R.id.sw_button_5:
                key1 = 5;
                break;
            case R.id.sw_button_6:
                key1 = 9;
                break;
        }
        intent.putExtra(Constants.JSON_ID, mHouseId);
        intent.putExtra(Constants.JSON_NAME, mTitle);
        intent.putExtra("ELEMENT_ID", key1);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton hotButton1, hotButton2, hotButton3, hotButton4, hotButton5;

        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_1);
        Picasso.with(this).load(R.drawable.refresh_none).into(hotButton1);
        hotButton1.setEnabled(false);

        hotButton2 = (ImageButton)findViewById(R.id.hot_button_image_2);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_event, null);
        hotButton2.setImageDrawable(drawable);
        hotButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogTypeActivity.this, EntryActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0 ,0);
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogTypeActivity.this, OverviewMainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0 ,0);
            }
        });

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        Picasso.with(this).load(R.drawable.total_on).into(hotButton4);
        hotButton4.setEnabled(false);

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        //Picasso.with(this).load(R.drawable.setting_none).into(hotButton5);
        //hotButton5.setEnabled(false);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_setting, null);
        hotButton5.setImageDrawable(drawable);
        hotButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogTypeActivity.this, SettingActivity.class);
                intent.putExtra(Constants.JSON_ID, mHouseId);
                intent.putExtra(Constants.JSON_NAME, mTitle);
                intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingString);
                startActivity(intent);
                finish();
                overridePendingTransition(0 ,0);
            }
        });

        try {
            TextView headerTV, shadowTV;
            headerTV = (TextView) findViewById(R.id.header_date);
            shadowTV = (TextView) findViewById(R.id.header_shadow);
            Calendar c = Calendar.getInstance();
            StringBuilder sb = new StringBuilder();
            sb.append(c.get(Calendar.YEAR)).append("/")
                    .append(new DecimalFormat("00").format(c.get(Calendar.MONTH) + 1)).append("/")
                    .append(new DecimalFormat("00").format(c.get(Calendar.DAY_OF_MONTH)));
            headerTV.setText(sb.toString());
            shadowTV.setText(sb.toString());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6;
        imageView1 = (ImageView)findViewById(R.id.sw_report);
        imageView2 = (ImageView)findViewById(R.id.hourly_report);
        imageView3 = (ImageView)findViewById(R.id.entry_report);
        imageView4 = (ImageView)findViewById(R.id.intrusion_report);
        imageView5 = (ImageView)findViewById(R.id.power_report);
        imageView6 = (ImageView)findViewById(R.id.count_report);
        Picasso.with(this).load(R.drawable.on_off).into(imageView1);
        Picasso.with(this).load(R.drawable.enviro).into(imageView2);
        Picasso.with(this).load(R.drawable.door_big).into(imageView3);
        Picasso.with(this).load(R.drawable.thief_big).into(imageView4);
        Picasso.with(this).load(R.drawable.power_on_big).into(imageView5);
        Picasso.with(this).load(R.drawable.birds).into(imageView6);

        ViewGroup sButton1, sButton2, sButton3, sButton4, sButton5, sButton6;
        LinearLayout footer = (LinearLayout)findViewById(R.id.footer);
        footer.setBackgroundResource(R.drawable.selector_footer_background);
        try {
            sButton1 = (RelativeLayout) findViewById(R.id.sw_button_1);
            sButton2 = (RelativeLayout) findViewById(R.id.sw_button_2);
            sButton3 = (RelativeLayout) findViewById(R.id.sw_button_3);
            sButton4 = (RelativeLayout) findViewById(R.id.sw_button_4);
            sButton5 = (RelativeLayout) findViewById(R.id.sw_button_5);
            sButton6 = (RelativeLayout) findViewById(R.id.sw_button_6);
        } catch (ClassCastException ex) {
            sButton1 = (LinearLayout) findViewById(R.id.sw_button_1);
            sButton2 = (LinearLayout) findViewById(R.id.sw_button_2);
            sButton3 = (LinearLayout) findViewById(R.id.sw_button_3);
            sButton4 = (LinearLayout) findViewById(R.id.sw_button_4);
            sButton5 = (LinearLayout) findViewById(R.id.sw_button_5);
            sButton6 = (LinearLayout) findViewById(R.id.sw_button_6);
        }

        sButton1.setOnClickListener(this);
        sButton2.setOnClickListener(this);
        sButton3.setOnClickListener(this);
        sButton4.setOnClickListener(this);
        sButton5.setOnClickListener(this);
        sButton6.setOnClickListener(this);
    }
}
