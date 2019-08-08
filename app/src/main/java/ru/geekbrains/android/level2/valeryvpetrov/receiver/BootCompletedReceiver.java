package ru.geekbrains.android.level2.valeryvpetrov.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Objects.equals(action, Intent.ACTION_BOOT_COMPLETED) ||
                Objects.equals(action, Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            Log.d(LOG_TAG, "onReceive(). Boot completed");
            RoverNewLaunchInfoAlarmReceiver.scheduleAlarmReceiver(context, context);
        }
    }

}
