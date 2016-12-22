package com.junyenhuang.birdhouse;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.SparseArray;

import com.junyenhuang.birdhouse.database.DBHandler;
import com.junyenhuang.birdhouse.database.MainDatabase;
import com.junyenhuang.birdhouse.items.Element;
import com.junyenhuang.birdhouse.items.FrontIrs;
import com.junyenhuang.birdhouse.items.House;
import com.junyenhuang.birdhouse.items.HouseEvent;
import com.junyenhuang.birdhouse.items.IrsSetting;
import com.junyenhuang.birdhouse.items.Mp3Info;
import com.junyenhuang.birdhouse.items.SettingGroup;
import com.junyenhuang.birdhouse.items.SwitchSetting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JsonParser {
    private static final String TAG = JsonParser.class.getSimpleName();
    private Context context;

    public JsonParser(Context context) {
        this.context = context;
    }

    public ArrayList<House> parse(String json) {
        if (json != null && !json.isEmpty()) {
            try {
                ArrayList<House> houses = new ArrayList<>();
                SparseArray<String> map = new SparseArray<>();
                JSONArray devices = new JSONArray(json);
                context.deleteDatabase(Constants.DATABASE_MAIN);
                MainDatabase mainDB = new MainDatabase(context);
                for (int i = 0; i < devices.length(); i++) {
                    JSONObject c = devices.getJSONObject(i);
                    String type = c.getString(Constants.JSON_TYPE);
                    if (!type.equals(Constants.TYPE_BIRDHOUSE)) {
                        continue;
                    }
                    House house1 = new House();
                    house1.setId(c.getInt(Constants.JSON_ID));
                    house1.setName(c.getString(Constants.JSON_NAME));
                    house1.setOnline(c.getInt(Constants.JSON_ONLINE) == 1);
                    Log.d(TAG, "name=" + house1.getName() + " online=" + house1.isOnline());

                    JSONObject properties = c.getJSONObject("properties");
                    house1.setMac(properties.getString(Constants.JSON_MAC));

                    String settingString = properties.getString(Constants.JSON_SETTINGS_STRING);
                    Log.d(TAG, "settingString = " + settingString);
                    house1.setSettingString(settingString);
                    house1.setSettingGroup(parseSettings(settingString, house1.getId(), false));

                    int elementSize = properties.getInt(Constants.JSON_SIZE);
                    if(elementSize > 0 && properties.has(Constants.JSON_ELEMENTS)) {
                        JSONArray elementsArray = properties.getJSONArray(Constants.JSON_ELEMENTS);
                        String elementString = elementsArray.toString();
                        house1.setElementString(elementString);
                    }
                    map.put(house1.getId(), house1.getName());
                    mainDB.addHouse(house1);
                    houses.add(house1);
                }

                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString(Constants.EXTRA_HOUSENAMES, stringify(map));
                editor.apply();
                mainDB.close();
                return houses;
            }catch(StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("JsonParser", "No data received from HTTP Request");
            return null;
        }
    }

    // turns a HashMap<String, String> into "key=value|key=value|key=value"
    private String stringify(SparseArray<String> map) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < map.size(); i++) {
            int key = map.keyAt(i);
        //}
        //for(int key : map.keySet()) {
            sb.append(String.valueOf(key)).append((char)0x0061).append(map.get(key)).append((char)0x0124);
            //Log.d("stringify", sb.toString());
        }
        //Log.d("SPARSEARRAY", sb.substring(0, sb.length() - 1));
        return sb.substring(0, sb.length() - 1); // this may be -2, but write a unit test
    }

    public ArrayList<Element> parseElements(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        //Log.d("parseElements", "input json string=" + jsonString);
        try {
            JSONArray elementsArray = new JSONArray(jsonString);
            ArrayList<Element> elements = new ArrayList<>();
            for(int j = 0; j < elementsArray.length(); j++) {
                JSONObject obj = elementsArray.getJSONObject(j);
                Element e = new Element();
                e.setId(obj.getInt(Constants.JSON_ID));
                e.setName(obj.getString(Constants.JSON_NAME));
                String elementType = obj.getString(Constants.JSON_TYPE);
                Object elementValue = obj.get(Constants.JSON_VALUE);
                String elementUnit = obj.getString(Constants.JSON_UNIT);
                if(obj.has("iconID")) {
                    e.setIconID(obj.getInt("iconID"));
                }
                e.setType(elementType);
                e.setValue(elementValue);
                e.setUnit(elementUnit);
                elements.add(e);
            }
            return elements;
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SettingGroup parseSettings(String settingString, int house_id, boolean updateSongs) {
        if (settingString != null && !settingString.isEmpty()) {
            try {
                SettingGroup setting1 = new SettingGroup();
                setting1.setHouseId(house_id);
                JSONObject c = new JSONObject(settingString);

                if(c.has(Constants.JSON_NAME)) {
                    setting1.setHouseName(c.getString(Constants.JSON_NAME));
                    //Log.d("SettingParser", "name=" + setting1.getHouseName());
                }

                if(c.has(Constants.JSET_SMS_TEL)) {
                    JSONArray smsArray = c.getJSONArray(Constants.JSET_SMS_TEL);
                    Log.d(TAG, "smsArray=" + smsArray.toString());
                    String[] smsNumbers = { "", "", "", "", "" };
                    for (int i = 0; i < smsArray.length(); i++) {
                        try {
                            //smsNumbers[i] = smsArray.getJSONObject(i).getString(Constants.JSET_PHONE);
                            smsNumbers[i] = smsArray.getString(i);
                            if(!PhoneNumberUtils.isGlobalPhoneNumber(smsNumbers[i])) {
                                smsNumbers[i] = "";
                            }
                            Log.d(TAG, "smsNumbers[" + i + "]=" + smsNumbers[i]);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            ex.printStackTrace();
                            break;
                        }
                    }
                    setting1.setSmsNumbers(smsNumbers);
                }

                if(c.has(Constants.JSET_NH3_LIMIT)) {
                    JSONObject obj = c.getJSONObject(Constants.JSET_NH3_LIMIT);
                    setting1.setNh3Limit(obj.getInt(Constants.JSON_VALUE));
                    //setting1.setNh3Limit(c.getInt(Constants.JSET_NH3_LIMIT));
                    //Log.d("SettingParser", "NH3LIMIT=" + setting1.getNh3Limit());
                }

                if(c.has(Constants.JSET_POWER_OUT)) {
                    JSONObject obj = c.getJSONObject(Constants.JSET_POWER_OUT);
                    int powerOut = obj.getInt(Constants.JSET_ACTION);
                    //int powerOut = c.getInt(Constants.JSET_POWER_OUT);
                    if(powerOut == 0) {
                        setting1.setPowerOff(false);
                        setting1.setPowerOn(false);
                    } else if(powerOut == 1) {
                        setting1.setPowerOff(true);
                        setting1.setPowerOn(false);
                    } else if(powerOut == 2) {
                        setting1.setPowerOff(false);
                        setting1.setPowerOn(true);
                    } else if(powerOut == 3) {
                        setting1.setPowerOff(true);
                        setting1.setPowerOn(true);
                    }
                    //Log.d("SettingParser", "PowerOFF=" + setting1.getPowerOff());
                    //Log.d("SettingParser", "PowerON=" + setting1.getPowerOn());

                    //setting1.setPhoneNumber(obj.getString(Constants.JSET_PHONE));
                    //Log.d("SettingParser", "PowerOut:phone=" + setting1.getPhoneNumber());
                }

                if(c.has(Constants.JSET_FRONT_IRS)) {
                    JSONArray fronts = c.getJSONArray(Constants.JSET_FRONT_IRS);
                    for (int i = 0; i < fronts.length(); i++) {
                        JSONObject obj = fronts.getJSONObject(i);
                        FrontIrs irs = new FrontIrs();
                        irs.setAction(obj.getInt(Constants.JSET_ACTION));
                        irs.setStartTime(obj.getString(Constants.JSET_START));
                        irs.setDurationTotalMinutes(obj.getInt(Constants.JSET_STOP));
                        setting1.front.add(irs);
                    }
                } else {
                    FrontIrs irs = new FrontIrs();
                    irs.setAction(1);
                    irs.setStartTime("08:00");
                    irs.setDurationTotalMinutes(539);
                    setting1.front.add(irs);

                    FrontIrs irs1 = new FrontIrs();
                    irs1.setAction(1);
                    irs1.setStartTime("18:00");
                    irs1.setDurationTotalMinutes(839);
                    setting1.front.add(irs1);
                }

                JSONArray irs = c.getJSONArray(Constants.JSET_IRS);
                for (int i = 0; i < irs.length(); i++) {
                    JSONObject obj = irs.getJSONObject(i);
                    IrsSetting irsSetting = new IrsSetting();
                    if(setting1.getPhoneNumber().isEmpty()) {
                        setting1.setPhoneNumber(obj.getString(Constants.JSET_PHONE));
                    }
                    //Log.d("SettingParser", "IRS[" + i + "]:phone=" + setting1.getPhoneNumber());
                    irsSetting.setAction(obj.getInt(Constants.JSET_ACTION));
                    //Log.d("SettingParser", "IRS[" + i + "]:action=" + irsSetting.getAction());
                    setting1.irs.add(irsSetting);
                }

                //Log.d("SettingParser", "updateSongs=" + updateSongs);
                if(updateSongs) {
                    JSONArray mp3s = c.getJSONArray(Constants.JSET_MP3);
                    context.deleteDatabase(Constants.DATABASE_NAME);
                    DBHandler db = new DBHandler(context);
                    for (int i = 0; i < mp3s.length(); i++) {
                        Mp3Info song1 = new Mp3Info();
                        JSONObject mp3Song = mp3s.getJSONObject(i);
                        song1.setSongId(mp3Song.getInt(Constants.JSET_MP3_ID));
                        song1.setStart(mp3Song.getString(Constants.JSET_START));
                        //song1.setStop(mp3Song.getString(Constants.JSET_STOP));
                        song1.setTotalDurationMinutes(mp3Song.getInt(Constants.JSET_STOP));
                        song1.setVol(mp3Song.getInt(Constants.JSET_VOLUME));
                        song1.setHouseId(house_id);
                        /*
                        Log.d("SettingParser", "MP3[" + i + "]:ID=" + song1.getId()
                                + " Start=" + song1.getStart()
                                + " for=" + song1.getTotalDurationMinutes()
                                + " Volume=" + song1.getVol());
                                */
                        db.addTrack(song1);
                        //setting1.mp3_list.add(song1);
                    }
                    db.close();
                }

                JSONArray switchArray;
                if(c.has(Constants.JSET_SWITCHES)) {
                    switchArray = c.getJSONArray(Constants.JSET_SWITCHES);
                } else {
                    //Log.w(TAG, "no Switches JSONObj");
                    return setting1;
                }

                for(int i = 0; i < switchArray.length(); i++) {
                    SwitchSetting switch1 = new SwitchSetting();
                    JSONObject switchSetting = switchArray.getJSONObject(i);
                    switch1.setHumidityMax(switchSetting.getInt(Constants.JSET_HUM_MAX));
                    switch1.setHumidityMin(switchSetting.getInt(Constants.JSET_HUM_MIN));
                    switch1.setInterval(switchSetting.getInt(Constants.JSET_SW_HOUR));
                    switch1.setDuration(switchSetting.getInt(Constants.JSET_SW_MIN));
                    switch1.setStart(switchSetting.getString(Constants.JSET_START));
                    //switch1.setStop(switchSetting.getString(Constants.JSET_STOP));
                    switch1.setTotalDurationMinutes(switchSetting.getInt(Constants.JSET_STOP));
                    switch1.setMode(switchSetting.getInt(Constants.JSET_SWITCH_MODE));
                    /*
                    Log.d("SettingParser", "Switch[" + i + "]:Mode=" + switch1.getMode()
                            + " Start=" + switch1.getStart()
                            + " for=" + switch1.getTotalDurationMinutes()
                            //+ " Stop=" + switch1.getStop()
                            + " Max=" + switch1.getHumidityMax()
                            + " Min=" + switch1.getHumidityMin()
                            + " Hour=" + switch1.getInterval()
                            + " Minute=" + switch1.getDuration()
                    );
                    */
                    setting1.switches.add(switch1);
                }
                return setting1;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("JsonParser", "No data received from HTTP Request");
            return null;
        }
    }

    protected ArrayList<HouseEvent> parseEvents(String jsonInput, int house_id, int icon_id, String filterType) {
        if(jsonInput == null) {
            return null;
        }
        ArrayList<HouseEvent> events = new ArrayList<>();
        try {
            JSONObject results = new JSONObject(jsonInput);
            JSONArray recordsArray = results.getJSONArray("Records");
            MainDatabase db = new MainDatabase(context);
            for(int i = 0; i < recordsArray.length() ; i++) {
                int irsTrigger = 0; // 0=no trigger, 1=entry, 2=intrusion
                HouseEvent e = new HouseEvent();
                JSONObject obj = recordsArray.getJSONObject(i);
                e.setHouseKey(Integer.parseInt(obj.getString("key2")));
                /*
                if(house_id > -1) {
                    if(e.getHouseKey() != house_id) {
                        continue;
                    }
                }
                */

                int elementKey = Integer.parseInt(obj.getString("key1"));
                e.setElementKey(elementKey);
                e.setTimeString(obj.getString(Constants.JSON_TIME));
                e.setEventKey(obj.getString(Constants.JSON_KEY));
                e.setDescription(obj.getString(Constants.JSON_DESC));
                e.setEventValue(obj.getString(Constants.JSON_VALUE));
                switch (elementKey) {
                    case 1: {
                        if(filterType != null && !filterType.isEmpty()) {
                            if(!e.getDescription().contains(filterType)) {
                                continue;
                            }
                        }
                        break;
                    }
                    case 4: {
                        irsTrigger = 2;
                        e.setPriority(4);
                        break;
                    }
                    case 5:
                        e.setPriority(3);
                        break;
                    case 3: {
                        irsTrigger = 1;
                        e.setPriority(2);
                        break;
                    }
                    case 6:
                        if(filterType != null && !filterType.isEmpty()) {
                            if(!e.getEventValue().toLowerCase().contains(filterType.toLowerCase())) {
                                continue;
                            }
                        }
                        if(e.getEventValue().toLowerCase().contains("ppm")) {
                            e.setPriority(1);
                        } else {
                            e.setPriority(0);
                        }
                        break;
                    case 9:
                        e.setPriority(0);
                        break;
                    default:
                        e.setPriority(0);
                }

                String houseName = db.getHouse(e.getHouseKey()).getName();
                if(houseName == null || houseName.isEmpty()) {
                } else {
                    if(irsTrigger > 0) {
                        // retrieve today's date
                        Calendar calendar = Calendar.getInstance();
                        int yyyy = calendar.get(Calendar.YEAR);
                        int mm = calendar.get(Calendar.MONTH) + 1;
                        int dd = calendar.get(Calendar.DAY_OF_MONTH);
                        int date = calendar.get(Calendar.DAY_OF_YEAR);
                        StringBuilder sb = new StringBuilder();
                        try {
                            // retrieve event date, if query was made for today's events, then set trigger
                            // otherwise it's old data and there'd be no need to set trigger
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date mDate = sdf.parse(e.getTimeString());
                            Calendar c = Calendar.getInstance();
                            c.setTime(mDate);
                            int event_year = c.get(Calendar.YEAR);
                            int event_date = c.get(Calendar.DAY_OF_YEAR);
                            if(yyyy == event_year && date == event_date) {
                                if (irsTrigger == 2) {
                                    sb.append("INTRUSION_").append(yyyy)
                                            .append(new DecimalFormat("00").format(mm))
                                            .append(new DecimalFormat("00").format(dd));
                                    context.getSharedPreferences("IRS", Context.MODE_PRIVATE).edit()
                                            .putString("INTRUSION_" + e.getHouseKey(), sb.toString()).apply();
                                } else if (irsTrigger == 1) {
                                    sb.append("ENTRY_").append(yyyy)
                                            .append(new DecimalFormat("00").format(mm))
                                            .append(new DecimalFormat("00").format(dd));
                                    context.getSharedPreferences("IRS", Context.MODE_PRIVATE).edit()
                                            .putString("ENTRY_" + e.getHouseKey(), sb.toString()).apply();
                                }
                            }
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    e.setHouseName(houseName);
                    events.add(e);
                }
            }
            db.close();
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return events;
    }
}
