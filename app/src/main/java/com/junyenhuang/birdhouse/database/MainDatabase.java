package com.junyenhuang.birdhouse.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.items.House;

import java.util.ArrayList;

public class MainDatabase extends SQLiteOpenHelper {
    private static final String TAG = MainDatabase.class.getSimpleName();
    private static final int DB_VER = 1;
    public static final String TABLE_NAME = "houses";    //table name
    //column names
    private static final String KEY_ID = "_id";
    private static final String KEY_HOUSE_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ONLINE = "online";
    private static final String KEY_MAC = "mac";
    //private static final String KEY_ELEMENT_SIZE = "elementsize";
    private static final String KEY_ELEMENT = "elements";
    private static final String KEY_SETTING = "setting";

    public MainDatabase(Context context) {
        super(context, Constants.DATABASE_MAIN, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HOUSE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_HOUSE_ID + " INTEGER,"
                + KEY_NAME + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_ONLINE + " INTEGER,"
                + KEY_MAC + " TEXT,"
                //+ KEY_ELEMENT_SIZE + " INTEGER,"
                + KEY_ELEMENT + " TEXT,"
                + KEY_SETTING + " TEXT"
                + ")";
        db.execSQL(CREATE_HOUSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addHouse(House house) {
        long new_id;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_HOUSE_ID, house.getId());
        values.put(KEY_NAME, house.getName());
        values.put(KEY_TYPE, house.getType());
        values.put(KEY_ONLINE, house.isOnline());
        values.put(KEY_MAC, house.getMac());
        //values.put(KEY_ELEMENT_SIZE, house.getElements().size());
        values.put(KEY_ELEMENT, house.getElementString());
        values.put(KEY_SETTING, house.getSettingString());

        new_id = db.insert(TABLE_NAME, null, values);
        return new_id;
    }

    public void updateHouse(House house) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, house.getName());
        //values.put(KEY_ELEMENT_SIZE, house.getElements().size());
        values.put(KEY_ELEMENT, house.getElementString());
        values.put(KEY_SETTING, house.getSettingString());

        db.update(TABLE_NAME, values, "mac=" + house.getMac(), null);
        db.close();
    }

    public House getHouse(int id) {
        SQLiteDatabase db = getReadableDatabase();
        House house = new House();
        Cursor c = db.query(TABLE_NAME, new String[] {
                KEY_ID, // 0
                KEY_HOUSE_ID, // 1
                KEY_NAME, // 2
                KEY_TYPE, // 3
                KEY_ONLINE, // 4
                KEY_MAC, // 5
                //KEY_ELEMENT_SIZE, // 6
                KEY_ELEMENT, // 6
                KEY_SETTING // 7
        }, KEY_HOUSE_ID + "=?", new String[] {String.valueOf(id)}, null, null, null, null);

        if(c != null && c.moveToFirst()) {
            house.setId(c.getInt(1));
            house.setName(c.getString(2));
            house.setType(c.getString(3));
            house.setOnline(c.getInt(4) == 1);
            house.setMac(c.getString(5));
            house.setElementString(c.getString(6));
            house.setSettingString(c.getString(7));
            c.close();
        }
        db.close();
        return house;
    }

    public ArrayList<House> getHouseList() {
        ArrayList<House> houses = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()) {
            do {
                House house = new House();
                house.setId(Integer.parseInt(c.getString(1)));
                house.setName(c.getString(2));
                house.setType(c.getString(3));
                house.setMac(c.getString(4));
                house.setElementString(c.getString(6));
                house.setSettingString(c.getString(7));
                Log.d(TAG, "ID=" + house.getId());
                Log.d(TAG, "Name=" + house.getName());
                Log.d(TAG, "Type=" + house.getType());
                Log.d(TAG, "MAC=" + house.getMac());
                Log.d(TAG, "Elements=" + house.getElementString());
                Log.d(TAG, "Settings=" + house.getSettingString());

                houses.add(house);
            } while(c.moveToNext());
        }
        //db.close();
        c.close();
        return houses;
    }
}
