package com.junyenhuang.birdhouse.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.receiver.ServiceReceiver;

public class PollingIntentService extends IntentService {
    private static final String TAG = PollingIntentService.class.getSimpleName();
    int userID = 0;
    private String mURL = Constants.BASE_URL + Constants.GET_DEVICES;
    private int mRequestType = WebRequest.GETRequest;

    public PollingIntentService() {
        super(TAG);
        mURL = Constants.BASE_URL + Constants.GET_DEVICES;
        mRequestType = WebRequest.GETRequest;
    }

    public PollingIntentService(String url) {
        super(TAG);
        mURL = url;
        mRequestType = WebRequest.GETRequest;
    }

    public PollingIntentService(String url, int type) {
        super(TAG);
        mURL = url;
        mRequestType = type;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        WebRequest webRequest = new WebRequest();
        int userID = intent.getIntExtra(Constants.USER_ID, 0);
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
        String url = "http://" + savedUrl + "/api/" + Constants.GET_DEVICES + userID;

        //String url = Constants.BASE_URL + Constants.GET_DEVICES + userID;
        String jsonStr = webRequest.makeWebServiceCall(url, mRequestType);
        Intent intentUpdate = new Intent(Constants.ACTION_UPDATE);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(Constants.EXTRA_JOUTPUT, jsonStr);
        sendBroadcast(intentUpdate);
        ServiceReceiver.completeWakefulIntent(intent);
    }
}
