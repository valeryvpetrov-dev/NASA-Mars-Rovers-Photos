package ru.geekbrains.android.level2.valeryvpetrov.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsRoverAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsRoversGenerator;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.RoverListResponse;
import ru.geekbrains.android.level2.valeryvpetrov.receiver.ConnectivityChangeReceiver;
import ru.geekbrains.android.level2.valeryvpetrov.receiver.RoverNewLaunchInfoAlarmReceiver;
import ru.geekbrains.android.level2.valeryvpetrov.ui.MainActivity;

import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_KEY_FLAG_IS_WAITING_CONNECTIVITY_CHANGE;
import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_KEY_ROVER_LAST_REGISTERED_LAUNCH_FORMAT;
import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_NAME;
import static ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter.dateToString;
import static ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter.stringToDate;

@WorkerThread
public class RoverNewLaunchInfoService
        extends JobIntentService {

    public static final String EXTRA_NEW_LAUNCH_ROVER = "newLaunchRover";

    private static final String LOG_TAG = RoverNewLaunchInfoService.class.getSimpleName();

    private static final String CHANNEL_ROVER_INFO_ID = "roverNewLaunchInfoChannel";
    private static final int jobId = 1;
    private int notificationId = 1;

    private NASAMarsRoverAPI nasaMarsRoverAPI;

    private boolean isWaitingForConnectivityChange;  // flag that indicates connectivity loss

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, RoverNewLaunchInfoService.class, jobId, intent);
    }

    @Override
    public void onCreate() {
        nasaMarsRoverAPI = NASAMarsRoversGenerator.createService(NASAMarsRoverAPI.class);
        isWaitingForConnectivityChange = getFlag(SHARED_PREFERENCES_KEY_FLAG_IS_WAITING_CONNECTIVITY_CHANGE);
        createNotificationChannel();
        super.onCreate();
    }

    /***
     * Executes when application component need to request rover new launch info.
     *
     * Because it is method of IntentService class:
     * - Runs in worker thread (network request is done synchronously)
     * - Each incoming intent is handled sequentially (intents are handled separately)
     *
     * Can be triggered by:
     * - RoverNewLaunchInfoAlarmReceiver
     * - ConnectivityChangeReceiver
     *
     * @param intent - work initiator.
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if (Objects.equals(action, RoverNewLaunchInfoAlarmReceiver.INTENT_ACTION)) {
            handleAlarmReceiverIntent(intent);
        } else if (Objects.equals(action, ConnectivityChangeReceiver.INTENT_ACTION)) {
            handleConnectivityChangeReceiverIntent(intent);
        }
    }

    private void handleAlarmReceiverIntent(@NonNull Intent intent) {
        if (!isWaitingForConnectivityChange)
            requestRoverNewLaunchInfo();
    }

    private void handleConnectivityChangeReceiverIntent(@NonNull Intent intent) {
        if (isWaitingForConnectivityChange) {
            boolean isNetworkAvailable = intent
                    .getBooleanExtra(ConnectivityChangeReceiver.EXTRA_IS_NETWORK_AVAILABLE,
                            false);
            if (isNetworkAvailable)
                requestRoverNewLaunchInfo();
        }
    }

    private void requestRoverNewLaunchInfo() {
        try {
            RoverListResponse responseBodyRoverList = nasaMarsRoverAPI.getRoverList().execute().body();
            if (responseBodyRoverList != null) {
                List<Rover> roverList = responseBodyRoverList.getRovers();
                for (Rover rover : roverList) {
                    Date lastLaunch = rover.getMaxDate();
                    Date lastRegisteredLaunch = getLastRegisteredLaunch(rover.getName());
                    if (lastRegisteredLaunch != null) {
                        if (lastLaunch.after(lastRegisteredLaunch)) {
                            updateLastRegisteredLaunch(rover);
                            sendRoverNewLaunchInfoNotification(rover);
                        }
                    } else {
                        updateLastRegisteredLaunch(rover);
                        sendRoverNewLaunchInfoNotification(rover);
                    }
                }
                Log.d(LOG_TAG, String.format("onHandleWork(). Rover list size: %d", roverList.size()));
            }
            setFlag(SHARED_PREFERENCES_KEY_FLAG_IS_WAITING_CONNECTIVITY_CHANGE, false);
        } catch (IOException e) {
            Log.d(LOG_TAG, String.format("onHandleWork(). IOException: %s", e.getMessage()));
            if (!ConnectivityChangeReceiver.isNetworkAvailable(this)) {
                setFlag(SHARED_PREFERENCES_KEY_FLAG_IS_WAITING_CONNECTIVITY_CHANGE, true);
            }
        }
    }

    @Nullable
    private Date getLastRegisteredLaunch(@NonNull String roverName) {
        SharedPreferences sharedPreferences =
                getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        try {
            String key = String
                    .format(SHARED_PREFERENCES_KEY_ROVER_LAST_REGISTERED_LAUNCH_FORMAT, roverName);
            return stringToDate(sharedPreferences.getString(key, null));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateLastRegisteredLaunch(@NonNull Rover rover) {
        SharedPreferences sharedPreferences =
                getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = String
                .format(SHARED_PREFERENCES_KEY_ROVER_LAST_REGISTERED_LAUNCH_FORMAT, rover.getName());
        editor.putString(key, dateToString(rover.getMaxDate()));
        editor.apply();
    }

    private boolean getFlag(@NonNull String flagKey) {
        // data persistence between service work sessions
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(flagKey, false);
    }

    private void setFlag(@NonNull String flagKey, boolean flag) {
        // data persistence between service work sessions
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(flagKey, flag);
        editor.apply();
    }

    private void sendRoverNewLaunchInfoNotification(@NonNull Rover rover) {
        Intent newLaunchInfo = new Intent(this, MainActivity.class);
        newLaunchInfo.putExtra(EXTRA_NEW_LAUNCH_ROVER, rover);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(newLaunchInfo);
        PendingIntent pendingIntentLoadLatestPhotos = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = String.format("%s rover new launch info.",
                rover.getName());
        String contentText = String.format("There are some new photos captured on %s.",
                dateToString(rover.getMaxDate()));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ROVER_INFO_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contentTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_slideshow,
                        getString(R.string.action_load_latest_photos),
                        pendingIntentLoadLatestPhotos);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationId++, builder.build()); // suppress multiple notifications send
    }

    private void createNotificationChannel() {
        // Notification channel is used only on API 26+
        // It is safe to rerun this code because existing channel won't be recreated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = getString(R.string.channel_rover_info_name);
            String channelDescription = getString(R.string.channel_rover_info_description);
            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ROVER_INFO_ID,
                    channelName,
                    channelImportance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

}
