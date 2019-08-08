package ru.geekbrains.android.level2.valeryvpetrov.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import ru.geekbrains.android.level2.valeryvpetrov.service.RoverNewLaunchInfoService;

public class RoverNewLaunchInfoAlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = RoverNewLaunchInfoAlarmReceiver.class.getSimpleName();

    public static final String INTENT_ACTION = RoverNewLaunchInfoAlarmReceiver.class.getName();
    public static final int REQUEST_CODE = 111;

    private static final long INTERVAL_MILLIS = AlarmManager.INTERVAL_DAY;

    public static void scheduleAlarmReceiver(@NonNull Context packageContext,
                                             @NonNull Context context) {
        Intent intent = new Intent(packageContext, RoverNewLaunchInfoAlarmReceiver.class);
        intent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                RoverNewLaunchInfoAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    firstMillis,
                    INTERVAL_MILLIS,
                    pendingIntent);
            Log.d(LOG_TAG, "scheduleAlarmReceiver(). Service is scheduled");
        } else
            Log.d(LOG_TAG, "scheduleAlarmReceiver(). Service is not scheduled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Objects.equals(action, INTENT_ACTION)) {
            Intent roverNewLaunchInfoService = new Intent(context, RoverNewLaunchInfoService.class);
            roverNewLaunchInfoService.setAction(action);
            RoverNewLaunchInfoService.enqueueWork(context, roverNewLaunchInfoService);
            Log.d(LOG_TAG, "onReceive(). Service is started");
        }
    }

}
