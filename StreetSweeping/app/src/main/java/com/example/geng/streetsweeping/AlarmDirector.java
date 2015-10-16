package com.example.geng.streetsweeping;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

/**
 * Created by geng on 10/15/15.
 */
public class AlarmDirector {
    AlarmManager alarmManager;
    //PendingIntent

    public AlarmDirector(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //context.getSharedPreferences()
    }

    public boolean setAlarm(String sweepDate) {

        return true;
    }

    public void removeAlarm() {

    }
}
