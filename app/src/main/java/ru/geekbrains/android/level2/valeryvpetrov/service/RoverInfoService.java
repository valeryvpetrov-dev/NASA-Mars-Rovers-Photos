package ru.geekbrains.android.level2.valeryvpetrov.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosJsonParser;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;
import ru.geekbrains.android.level2.valeryvpetrov.ui.MainActivity;

public class RoverInfoService extends JobIntentService implements Callback {

    public static final String EXTRA_LATEST_SOL = "latestSol";
    public static final String EXTRA_ROVER_NAME = "roverName";

    private static final String CHANNEL_ROVER_INFO_ID = "roverInfoChannel";
    private static final int JOB_ID = 1;

    private NASAMarsPhotosAPI nasaMarsPhotosAPI;
    private NASAMarsPhotosJsonParser nasaMarsPhotosJsonParser;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, RoverInfoService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        nasaMarsPhotosAPI = NASAMarsPhotosAPI.getInstance();
        nasaMarsPhotosJsonParser = NASAMarsPhotosJsonParser.getInstance();
        createNotificationChannel();
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String roverName = intent.getStringExtra(EXTRA_ROVER_NAME);
        if (roverName != null) {
            nasaMarsPhotosAPI.getRoverByName(roverName).enqueue(this);
        }
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Rover rover = (Rover) nasaMarsPhotosJsonParser.deserialize(Rover.class,
                response.body().string(),
                NASAMarsPhotosAPI.JSON_ROOT_NAME_ROVER);
        if (rover != null) {
            sendNotification(rover);
        }
    }

    private void sendNotification(Rover rover) {
        Intent intentLoadLatestPhotos = new Intent(this, MainActivity.class);
        intentLoadLatestPhotos.putExtra(EXTRA_ROVER_NAME, rover.name);
        intentLoadLatestPhotos.putExtra(EXTRA_LATEST_SOL, rover.maxSol);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(intentLoadLatestPhotos);
        PendingIntent pendingIntentLoadLatestPhotos = taskStackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentTitle = String.format("%s rover info.",
                rover.name);
        String contentText = String.format("Last launch was on %s.",
                TypeConverter.dateToString(rover.maxDate));
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
            notificationManager.notify(0, builder.build()); // suppress multiple notifications send
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
