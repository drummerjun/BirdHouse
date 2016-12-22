package com.junyenhuang.birdhouse.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.items.Mp3Info;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {
    private static final String TAG = DBHandler.class.getSimpleName();
    private static final int DB_VER = 1;
    public static final String TABLE_NAME = "mp3s";    //table name
    //column names
    private static final String KEY_ID = "_id";
    private static final String KEY_MP3_ID = "mp3_id";
    private static final String KEY_START = "start";
    private static final String KEY_DURATION = "duration";
    //private static final String KEY_STOP = "stop";
    private static final String KEY_VOL = "vol";
    private static final String KEY_HOUSE_ID = "house_id";

    public DBHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MP3_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MP3_ID + " INTEGER,"
                + KEY_START + " TEXT,"
                + KEY_DURATION + " INTEGER,"
                //+ KEY_STOP + " TEXT,"
                + KEY_VOL + " INTEGER,"
                + KEY_HOUSE_ID + " INTEGER"
                + ")";
        db.execSQL(CREATE_MP3_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addTrack(Mp3Info mp3) {
        long new_id;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MP3_ID, mp3.getSongId());
        values.put(KEY_START, mp3.getStart());
        //values.put(KEY_STOP, mp3.getStop());
        values.put(KEY_DURATION, mp3.getTotalDurationMinutes());
        values.put(KEY_VOL, mp3.getVol());
        values.put(KEY_HOUSE_ID, mp3.getHouseId());
        new_id = db.insert(TABLE_NAME, null, values);
        db.close();
        return new_id;
    }

    public void updateTrack(Mp3Info mp3) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MP3_ID, mp3.getSongId());
        values.put(KEY_START, mp3.getStart());
        //values.put(KEY_STOP, mp3.getStop());
        values.put(KEY_DURATION, mp3.getTotalDurationMinutes());
        values.put(KEY_VOL, mp3.getVol());
        db.update(TABLE_NAME, values, "_id=" + mp3.getId(), null);
        db.close();
    }

    public Mp3Info getTrack(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Mp3Info mp3 = new Mp3Info();
        Cursor c = db.query(TABLE_NAME, new String[] {
                KEY_ID, // 0
                KEY_MP3_ID, // 1
                KEY_START, // 2
                //KEY_STOP, // 3
                KEY_DURATION, // 3
                KEY_VOL, // 4
                KEY_HOUSE_ID //5
                }, KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);

        if(c != null) {
            c.moveToFirst();
            mp3.setId(Integer.parseInt(c.getString(0)));
            mp3.setSongId(Integer.parseInt(c.getString(1)));
            mp3.setStart(c.getString(2));
            //mp3.setStop(c.getString(3));
            mp3.setTotalDurationMinutes(c.getInt(3));
            mp3.setVol(Integer.parseInt(c.getString(4)));
            mp3.setHouseId(Integer.parseInt(c.getString(5)));
            c.close();
        }
        db.close();
        return mp3;
    }

    public void setTracklist(int house_id, ArrayList<Mp3Info> list) {
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                deleteTrack((Integer)params[0]);
                for(Mp3Info m : (ArrayList<Mp3Info>)params[1]) {
                    addTrack(m);
                }
                return null;
            }
        }.execute(house_id, list);
    }

    public ArrayList<Mp3Info> getTracklist(int house_id) {
        ArrayList<Mp3Info> tracklist = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + KEY_HOUSE_ID + "=" + String.valueOf(house_id);

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()) {
            do {
                Mp3Info mp3 = new Mp3Info();
                mp3.setId(Integer.parseInt(c.getString(0)));
                mp3.setSongId(Integer.parseInt(c.getString(1)));
                mp3.setStart(c.getString(2));
                //mp3.setStop(c.getString(3));
                mp3.setTotalDurationMinutes(c.getInt(3));
                mp3.setVol(Integer.parseInt(c.getString(4)));
                mp3.setHouseId(Integer.parseInt(c.getString(5)));
                tracklist.add(mp3);
            } while(c.moveToNext());
        }
        //db.close();
        c.close();
        return tracklist;
    }

    public int getTrackCount(int house_id) {
        String countQuery = "SELECT  * FROM " + TABLE_NAME
                + " WHERE " + KEY_HOUSE_ID + "=" + String.valueOf(house_id);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(countQuery, null);
        int count = c.getCount();
        c.close();
        db.close();
        return count;
    }

    public void deleteTrack(Mp3Info mp3) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[] { String.valueOf(mp3.getId()) });
        db.close();
    }

    private void deleteTrack(int house_id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, KEY_HOUSE_ID + " = ?",
                new String[] { String.valueOf(house_id) });
        db.close();
    }
}
