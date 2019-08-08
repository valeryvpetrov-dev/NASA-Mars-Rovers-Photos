package ru.geekbrains.android.level2.valeryvpetrov.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import ru.geekbrains.android.level2.valeryvpetrov.service.RoverNewLaunchInfoService;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = ConnectivityChangeReceiver.class.getName();
    public static final String EXTRA_IS_NETWORK_AVAILABLE = "isNetworkAvailable";

    private static final String LOG_TAG = ConnectivityChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // TODO DOES NOT CATCH MOBILE DATA STATE CHANGE
        if (Objects.equals(action, ConnectivityManager.CONNECTIVITY_ACTION) ||
                Objects.equals(action, WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            if (isNetworkAvailable(context)) {
                startRoverNewLaunchInfoService(context, true);
            }
        }
    }

    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = false;
        if (connectivityManager != null) {
            for (Network network : connectivityManager.getAllNetworks()) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo != null) {
                    int networkType = networkInfo.getType();
                    if (networkType == ConnectivityManager.TYPE_WIFI ||
                            networkType == ConnectivityManager.TYPE_MOBILE)
                        isNetworkAvailable = true;
                }
            }
        }
        Log.d(LOG_TAG, String.format("isNetworkAvailable(): %s", String.valueOf(isNetworkAvailable)));
        return isNetworkAvailable;
    }

    private void startRoverNewLaunchInfoService(@NonNull Context context,
                                                boolean isNetworkAvailable) {
        Intent roverNewLaunchInfoService = new Intent(context, RoverNewLaunchInfoService.class);
        roverNewLaunchInfoService.setAction(INTENT_ACTION);
        roverNewLaunchInfoService.putExtra(EXTRA_IS_NETWORK_AVAILABLE, isNetworkAvailable);
        RoverNewLaunchInfoService.enqueueWork(context, roverNewLaunchInfoService);
    }

}
