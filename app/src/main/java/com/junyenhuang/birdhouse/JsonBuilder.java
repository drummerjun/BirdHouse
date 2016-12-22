package com.junyenhuang.birdhouse;

import android.content.Context;
import android.util.Log;

import com.junyenhuang.birdhouse.database.DBHandler;
import com.junyenhuang.birdhouse.items.FrontIrs;
import com.junyenhuang.birdhouse.items.IrsSetting;
import com.junyenhuang.birdhouse.items.Mp3Info;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.junyenhuang.birdhouse.items.SwitchSetting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonBuilder {
    private static final String TAG = JsonBuilder.class.getSimpleName();
    public String buildString(SettingGroup settingGroup, Context context) {
        //JSONObject top = new JSONObject();
        JSONObject settings = new JSONObject();
        int id = settingGroup.getHouseId();
        try {
            String name = settingGroup.getHouseName();
            String phone = settingGroup.getPhoneNumber();
            int limit = settingGroup.getNh3Limit();
            boolean off = settingGroup.getPowerOff();
            boolean on = settingGroup.getPowerOn();
            int powerOutValue;

            settings.put(Constants.JSON_NAME, name);
            JSONObject nh3Obj = new JSONObject();
            nh3Obj.put(Constants.JSON_VALUE, limit);
            settings.put(Constants.JSET_NH3_LIMIT, nh3Obj);

            if(off) {
                if(on) {
                    powerOutValue = 3;
                } else {
                    powerOutValue = 1;
                }
            } else {
                if(on) {
                    powerOutValue = 2;
                } else {
                    powerOutValue = 0;
                }
            }
            JSONObject powerOut = new JSONObject();
            powerOut.put(Constants.JSET_ACTION, powerOutValue);
            powerOut.put(Constants.JSET_PHONE, phone);
            settings.put(Constants.JSET_POWER_OUT, powerOut);

            //JSONArray smsTelArray = buildSMSNumberArray(settingGroup.getSmsNumbers());
            //JSONArray smsTelArray = new JSONArray(settingGroup.getSmsNumbers());
            List<String> smsTelArray = new ArrayList<>();
            for (String s : settingGroup.getSmsNumbers()) {
                smsTelArray.add(s);
            }
            if(smsTelArray != null) {
                settings.put(Constants.JSET_SMS_TEL, new JSONArray(smsTelArray));
            }

            JSONArray frontArray = buildFrontIrsArray(settingGroup.front);
            if(frontArray != null) {
                settings.put(Constants.JSET_FRONT_IRS, frontArray);
            }

            JSONArray irsArray = buildIrsArray(settingGroup.irs, settingGroup.getPhoneNumber());
            if(irsArray != null) {
                settings.put(Constants.JSET_IRS, irsArray);
            }

            DBHandler db = new DBHandler(context);
            JSONArray mp3Array = buildMp3Array(db.getTracklist(id));
            if(mp3Array == null) {
                Log.w(TAG, "mp3Array null");
                return null;
            }
            settings.put(Constants.JSET_MP3, mp3Array);
            db.close();

            JSONArray switchArray = buildSwitchArray(settingGroup.switches);
            if(switchArray == null) {
                return null;
            }
            settings.put(Constants.JSET_SWITCHES, buildSwitchArray(settingGroup.switches));
            //top.put("settings", settings);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return settings.toString();
    }

    private JSONArray buildSMSNumberArray(String[] tel) {
        JSONArray smsArray = new JSONArray();
        if(tel == null) {
            return null;
        } else {
            for (String s : tel) {
                JSONObject irsObj = new JSONObject();
                try {
                    irsObj.put(Constants.JSET_PHONE, s);
                    Log.d(TAG, irsObj.toString());
                    smsArray.put(irsObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return smsArray;
    }

    private JSONArray buildFrontIrsArray(ArrayList<FrontIrs> setting) {
        JSONArray irs = new JSONArray();
        if(setting == null) {
            return null;
        } else {
            for (int i = 0; i < setting.size(); i++) {
                JSONObject irsObj = new JSONObject();
                try {
                    irsObj.put(Constants.JSET_ACTION, setting.get(i).getAction());
                    if(i == 0) {
                        irsObj.put(Constants.JSET_START, "08:00");
                        irsObj.put(Constants.JSET_STOP, 599);
                    } else if(i == 1) {
                        irsObj.put(Constants.JSET_START, "18:00");
                        irsObj.put(Constants.JSET_STOP, 839);
                    }
                    Log.d(TAG, irsObj.toString());
                    irs.put(irsObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return irs;
    }

    private JSONArray buildIrsArray(ArrayList<IrsSetting> setting, String phone) {
        JSONArray irs = new JSONArray();
        if(setting == null) {
            return null;
        } else {
            for (int i = 0; i < setting.size(); i++) {
                JSONObject irsObj = new JSONObject();
                try {
                    irsObj.put(Constants.JSET_ACTION, setting.get(i).getAction());
                    irsObj.put(Constants.JSET_PHONE, phone);
                    Log.d(TAG, irsObj.toString());
                    irs.put(irsObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return irs;
    }

    private JSONArray buildMp3Array(ArrayList<Mp3Info> setting) {
        JSONArray mp3s = new JSONArray();
        if(setting == null) {
            Log.w(TAG, "buildMp3Array null");
            return null;
        } else {
            for (int i = 0; i < setting.size(); i++) {
                JSONObject mp3Obj = new JSONObject();
                try {
                    Mp3Info song = setting.get(i);
                    mp3Obj.put(Constants.JSET_MP3_ID, song.getSongId());
                    mp3Obj.put(Constants.JSET_START, song.getStart());
                    //mp3Obj.put("stop_time", song.getStop());
                    mp3Obj.put(Constants.JSET_STOP, song.getTotalDurationMinutes());
                    mp3Obj.put(Constants.JSET_VOLUME, song.getVol());
                    mp3s.put(mp3Obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return mp3s;
    }

    private JSONArray buildSwitchArray(ArrayList<SwitchSetting> setting) {
        JSONArray switches = new JSONArray();
        if(setting == null) {
            return null;
        } else {
            for (int i = 0; i < setting.size(); i++) {
                JSONObject swObj = new JSONObject();
                SwitchSetting sw = setting.get(i);
                try {
                    swObj.put(Constants.JSET_HUM_MAX, sw.getHumidityMax());
                    swObj.put(Constants.JSET_HUM_MIN, sw.getHumidityMin());
                    swObj.put(Constants.JSET_SW_HOUR, sw.getInterval());
                    swObj.put(Constants.JSET_SW_MIN, sw.getDuration());
                    swObj.put(Constants.JSET_START, sw.getStart());
                    //swObj.put("stop_time", sw.getStop());
                    swObj.put(Constants.JSET_STOP, sw.getTotalDurationMinutes());
                    swObj.put(Constants.JSET_SWITCH_MODE, sw.getMode());
                    switches.put(swObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return switches;
    }
}