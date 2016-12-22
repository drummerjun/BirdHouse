package com.junyenhuang.birdhouse.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.junyenhuang.birdhouse.Constants;
import com.junyenhuang.birdhouse.service.PollingIntentService;

public class ServiceReceiver extends WakefulBroadcastReceiver {
    private AlarmManager am;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, PollingIntentService.class);
        int user_id = context.getSharedPreferences(Constants.PREF_LOGIN, Context.MODE_PRIVATE)
                .getInt(Constants.USER_ID, 0);
        service.putExtra(Constants.USER_ID, user_id);
        startWakefulService(context, service);
    }

    public void set(Context context) {
        am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 1000, 5000, alarmIntent); //REFRESH_INTERVAL
        ComponentName receiver = new ComponentName(context, ServiceReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancel(Context context) {
        if (am!= null) {
            am.cancel(alarmIntent);
        }
        ComponentName receiver = new ComponentName(context, ServiceReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
