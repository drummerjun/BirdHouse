package com.junyenhuang.birdhouse.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.EntryActivity;
import com.junyenhuang.birdhouse.JsonBuilder;
import com.junyenhuang.birdhouse.JsonParser;
import com.junyenhuang.birdhouse.LogTypeActivity;
import com.junyenhuang.birdhouse.R;
import com.junyenhuang.birdhouse.carol.OverviewMainActivity;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GeneralSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = GeneralSettingActivity.class.getSimpleName();
    private ImageButton hotButton1;
    private static SettingGroup mSettings;
    private static int mHouseId = -1;
    private static String mSettingsString;
    private String mTitle;
    private boolean mSaved = true;

    @Override
    public void onBackPressed() {
        if(mSaved) {
          super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingActivity.this);
            builder.setTitle(R.string.upload_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSaved = true;
                    uploadData(mHouseId, mSettings);
                    GeneralSettingActivity.super.onBackPressed();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                    PreferenceManager.getDefaultSharedPreferences(GeneralSettingActivity.this).edit()
                            .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                    GeneralSettingActivity.super.onBackPressed();
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_setting);

        Intent intent = getIntent();
        mHouseId = intent.getIntExtra(Constants.JSON_ID, -1);
        mTitle = intent.getStringExtra(Constants.JSON_NAME);
        mSettingsString = intent.getStringExtra(Constants.JSON_SETTINGS_STRING);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                JsonParser parser = new JsonParser(getApplicationContext());
                mSettings = parser.parseSettings(mSettingsString, mHouseId, false);
                return null;
            }
        }.execute();
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
        switch (v.getId()) {
            case R.id.sw_button_1:
                showSetPhoneDialog(v);
                break;
            case R.id.sw_button_2:
                showIR1Dialog();
                break;
            case R.id.sw_button_3:
                showPowerDialog();
                break;
            case R.id.sw_button_4:
                showSetSMSPhoneDialog(v);
                break;
            case R.id.sw_button_5:
                showIR2Dialog(v);
                break;
            case R.id.sw_button_6:
                showNH3Dialog();
                break;
        }
    }

    private void initViews() {
        LinearLayout sButton1, sButton2, sButton3, sButton4, sButton5, sButton6;

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sButton1 = (LinearLayout)findViewById(R.id.sw_button_1);
        sButton2 = (LinearLayout)findViewById(R.id.sw_button_2);
        sButton3 = (LinearLayout)findViewById(R.id.sw_button_3);
        sButton4 = (LinearLayout)findViewById(R.id.sw_button_4);
        sButton5 = (LinearLayout)findViewById(R.id.sw_button_5);
        sButton6 = (LinearLayout)findViewById(R.id.sw_button_6);

        sButton1.setOnClickListener(this);
        sButton2.setOnClickListener(this);
        sButton3.setOnClickListener(this);
        sButton4.setOnClickListener(this);
        sButton5.setOnClickListener(this);
        sButton6.setOnClickListener(this);

        ImageButton hotButton2, hotButton3, hotButton4, hotButton5;

        LinearLayout applyLayout = (LinearLayout)findViewById(R.id.hot_button0);
        applyLayout.setVisibility(View.VISIBLE);

        LinearLayout refreshLayout = (LinearLayout)findViewById(R.id.hot_button1);
        refreshLayout.setVisibility(View.GONE);

        hotButton1 = (ImageButton)findViewById(R.id.hot_button_image_0);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_apply, null);
        hotButton1.setImageDrawable(drawable);
        if(mSaved) {
            hotButton1.setEnabled(false);
        } else {
            hotButton1.setEnabled(true);
        }
        hotButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingActivity.this);
                builder.setTitle(R.string.upload_title);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSaved = true;
                        hotButton1.setEnabled(false);
                        uploadData(mHouseId, mSettings);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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
                if(mSaved) {
                    Intent intent = new Intent(GeneralSettingActivity.this, EntryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSaved = true;
                            uploadData(mHouseId, mSettings);
                            Intent intent = new Intent(GeneralSettingActivity.this, EntryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                            PreferenceManager.getDefaultSharedPreferences(GeneralSettingActivity.this).edit()
                                    .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                            Intent intent = new Intent(GeneralSettingActivity.this, EntryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                }
            }
        });

        hotButton3 = (ImageButton)findViewById(R.id.hot_button_image_3);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_realtime, null);
        hotButton3.setImageDrawable(drawable);
        hotButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSaved) {
                    Intent intent = new Intent(GeneralSettingActivity.this, OverviewMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSaved = true;
                            uploadData(mHouseId, mSettings);
                            Intent intent = new Intent(GeneralSettingActivity.this, OverviewMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                            PreferenceManager.getDefaultSharedPreferences(GeneralSettingActivity.this).edit()
                                    .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                            Intent intent = new Intent(GeneralSettingActivity.this, OverviewMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                }
            }
        });

        hotButton4 = (ImageButton)findViewById(R.id.hot_button_image_4);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_logs, null);
        hotButton4.setImageDrawable(drawable);
        hotButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSaved) {
                    //Intent intent = new Intent(GeneralSettingActivity.this, LogActivity.class);
                    Intent intent = new Intent(GeneralSettingActivity.this, LogTypeActivity.class);
                    intent.putExtra(Constants.JSON_ID, mHouseId);
                    intent.putExtra(Constants.JSON_NAME, mTitle);
                    intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingActivity.this);
                    builder.setTitle(R.string.upload_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSaved = true;
                            uploadData(mHouseId, mSettings);
                            //Intent intent = new Intent(GeneralSettingActivity.this, LogActivity.class);
                            Intent intent = new Intent(GeneralSettingActivity.this, LogTypeActivity.class);
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
                            mSettingsString = getIntent().getStringExtra(Constants.JSON_SETTINGS_STRING);
                            PreferenceManager.getDefaultSharedPreferences(GeneralSettingActivity.this).edit()
                                    .putString(Constants.JSON_SETTINGS_STRING + mHouseId, mSettingsString).apply();
                            //Intent intent = new Intent(GeneralSettingActivity.this, LogActivity.class);
                            Intent intent = new Intent(GeneralSettingActivity.this, LogTypeActivity.class);
                            intent.putExtra(Constants.JSON_ID, mHouseId);
                            intent.putExtra(Constants.JSON_NAME, mTitle);
                            intent.putExtra(Constants.JSON_SETTINGS_STRING, mSettingsString);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            //finish();
                        }
                    });
                    builder.create().show();
                }
            }
        });

        hotButton5 = (ImageButton)findViewById(R.id.hot_button_image_5);
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.selector_setting, null);
        hotButton5.setImageDrawable(drawable);
        hotButton5.setActivated(true);

        ImageView imageView1, imageView2, imageView3, imageView4, imageView5, imageView6;
        imageView1 = (ImageView)findViewById(R.id.keyin);
        imageView2 = (ImageView)findViewById(R.id.door_big);
        imageView3 = (ImageView)findViewById(R.id.power_on_big);
        imageView4 = (ImageView)findViewById(R.id.sms_tel);
        imageView5 = (ImageView)findViewById(R.id.thief_big);
        imageView6 = (ImageView)findViewById(R.id.nh3_big);
        Picasso.with(this).load(R.drawable.keyin).into(imageView1);
        Picasso.with(this).load(R.drawable.door_big).into(imageView2);
        Picasso.with(this).load(R.drawable.power_on_big).into(imageView3);
        Picasso.with(this).load(R.drawable.message5).into(imageView4);
        Picasso.with(this).load(R.drawable.thief_big).into(imageView5);
        Picasso.with(this).load(R.drawable.nh3_big).into(imageView6);
    }

    private void uploadData(final int houseId, SettingGroup settings) {
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                StringBuilder url = new StringBuilder();
                String savedUrl = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE)
                        .getString(Constants.BASE_URL_TAG, "");
                url.append("").append(savedUrl).append("");

                //url.append(Constants.BASE_URL + Constants.POST_SETTINGS);
                url.append(String.valueOf(params[0]));
                if(params[1] == null) {
                    return null;
                }
                String jsonString = new JsonBuilder()
                        .buildString((SettingGroup)params[1], GeneralSettingActivity.this);
                if(jsonString == null) {
                    return null;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put("", jsonString);
                new WebRequest().makeWebServiceCall(
                        url.toString(), WebRequest.POSTRequest, map);
                return null;
            }
        }.execute(houseId, settings);
    }

    private void showSetPhoneDialog(View v) {
        View viewInflated = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_tel, (ViewGroup)findViewById(android.R.id.content), false);
        final EditText inputName = (EditText) viewInflated.findViewById(R.id.houseName);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        inputName.setText(mTitle);
        input.setText(mSettings.getPhoneNumber());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, input.getText().toString());
                mSettings.setHouseName(inputName.getText().toString());
                mSettings.setPhoneNumber(input.getText().toString());
                if(!mSettings.getHouseName().equals(mTitle)) {
                    getSupportActionBar().setTitle(mSettings.getHouseName());
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString(Constants.ACTION_NAME_CHANGE + mHouseId, mSettings.getHouseName());
                    editor.apply();
                    mTitle = mSettings.getHouseName();
                }
                updateSettings(mHouseId, mSettings);
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

    private void showIR1Dialog() {
        final String[] ir1_values = getResources().getStringArray(R.array.shifts_titles);
        int currentAM = mSettings.front.get(0).getAction();
        int currentPM = mSettings.front.get(1).getAction();
        final ArrayList<Boolean> mSelected = new ArrayList<>();
        boolean[] checkedItems = {currentAM == 1, currentPM == 1};

        mSelected.add(currentAM == 1);
        mSelected.add(currentPM == 1);
        DialogInterface.OnMultiChoiceClickListener mDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    mSelected.set(which, true);
                } else {
                    mSelected.set(which, false);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ir1_notify) + " (" + getString(R.string.ir2_sms) + ")");
        builder.setMultiChoiceItems(ir1_values, checkedItems, mDialogListener);
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettings.front.get(0).setAction(mSelected.get(0) ? 1 : 0);
                mSettings.front.get(1).setAction(mSelected.get(1) ? 1 : 0);
                updateSettings(mHouseId, mSettings);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showPowerDialog() {
        final String[] power_values = {getString(R.string.power_off_sms), getString(R.string.power_back_sms)};
        final ArrayList<Boolean> mSelected = new ArrayList<>();
        final boolean currentPowerOff = mSettings.getPowerOff();
        final boolean currentPowerOn = mSettings.getPowerOn();
        boolean[] checkedItems = {currentPowerOff, currentPowerOn};

        mSelected.add(currentPowerOff);
        mSelected.add(currentPowerOn);
        DialogInterface.OnMultiChoiceClickListener mDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    mSelected.set(which, true);
                } else {
                    mSelected.set(which, false);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.pref_cat_powerout) + " (" + getString(R.string.ir2_sms) + ")");
        builder.setMultiChoiceItems(power_values, checkedItems, mDialogListener);
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettings.setPowerOff(mSelected.get(0));
                mSettings.setPowerOn(mSelected.get(1));
                updateSettings(mHouseId, mSettings);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showSetSMSPhoneDialog(View v) {
        View viewInflated = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_sms_tel, (ViewGroup)findViewById(android.R.id.content), false);
        final EditText tel1 = (EditText) viewInflated.findViewById(R.id.tel1);
        final EditText tel2 = (EditText) viewInflated.findViewById(R.id.tel2);
        final EditText tel3 = (EditText) viewInflated.findViewById(R.id.tel3);
        final EditText tel4 = (EditText) viewInflated.findViewById(R.id.tel4);
        final EditText tel5 = (EditText) viewInflated.findViewById(R.id.tel5);
        String[] smsNumbers = mSettings.getSmsNumbers();
        try {
            tel1.setText(smsNumbers[0]);
            tel2.setText(smsNumbers[1]);
            tel3.setText(smsNumbers[2]);
            tel4.setText(smsNumbers[3]);
            tel5.setText(smsNumbers[4]);
        } catch(IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewInflated).setTitle(R.string.sms_tel);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String smsTel1 = tel1.getText().toString();
                String smsTel2 = tel2.getText().toString();
                String smsTel3 = tel3.getText().toString();
                String smsTel4 = tel4.getText().toString();
                String smsTel5 = tel5.getText().toString();

                String[] smsNums = new String[] {smsTel1, smsTel2, smsTel3, smsTel4, smsTel5};
                mSettings.setSmsNumbers(smsNums);

                updateSettings(mHouseId, mSettings);
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

    private void showIR2Dialog(View v) {
        View viewInflated = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_intrusion, (ViewGroup)findViewById(android.R.id.content), false);

        final RadioButton callButton = (RadioButton) viewInflated.findViewById(R.id.choice_call);
        final RadioButton smsButton = (RadioButton) viewInflated.findViewById(R.id.choice_sms);
        final CheckBox alarmButton = (CheckBox)viewInflated.findViewById(R.id.select_alarm);

        final int currentSetting = mSettings.irs.get(1).getAction();

        switch(currentSetting) {
            case 0: // no alarm + call
                callButton.setChecked(true);
                alarmButton.setChecked(false);
                break;
            case 1: // no alarm + sms
                smsButton.setChecked(true);
                alarmButton.setChecked(false);
                // call out
                break;
            case 2: // alarm + call
                callButton.setChecked(true);
                alarmButton.setChecked(true);
                break;
            case 3: // alarm + sms
                smsButton.setChecked(true);
                alarmButton.setChecked(true);
                // send sms
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pref_title_ir2);
        builder.setView(viewInflated);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(alarmButton.isChecked()) {
                    if(callButton.isChecked()) {
                        mSettings.irs.get(1).setAction(2);
                    } else if(smsButton.isChecked()) {
                        mSettings.irs.get(1).setAction(3);
                    }
                } else {
                    if(callButton.isChecked()) {
                        mSettings.irs.get(1).setAction(0);
                    } else if(smsButton.isChecked()) {
                        mSettings.irs.get(1).setAction(1);
                    }
                }
                updateSettings(mHouseId, mSettings);
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

    private void showNH3Dialog() {
        final String[] nh3_values = getResources().getStringArray(R.array.pref_nh3_list);
        int highlightIndex = Arrays.asList(nh3_values).indexOf(String.valueOf(mSettings.getNh3Limit()));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pref_title_ammonia_level);
        builder.setSingleChoiceItems(nh3_values, highlightIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettings.setNh3Limit(Integer.parseInt(nh3_values[which]));
                updateSettings(mHouseId, mSettings);
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

    private void updateSettings(int houseId, SettingGroup settings) {
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                if(params[1] == null) {
                    return null;
                }
                mSettingsString = new JsonBuilder()
                        .buildString((SettingGroup)params[1], GeneralSettingActivity.this);
                if(mSettingsString == null) {
                    return null;
                }
                PreferenceManager.getDefaultSharedPreferences(GeneralSettingActivity.this).edit()
                        .putString(Constants.JSON_SETTINGS_STRING + params[0], mSettingsString).apply();
                return null;
            }
        }.execute(houseId, settings);
        mSaved = false;
        hotButton1.setEnabled(true);
    }
}
