package ru.geekbrains.android.level2.valeryvpetrov.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = ConnectivityChangeReceiver.class.getSimpleName();

    private NetworkCallback networkCallback;

    public interface NetworkCallback {
        void onAvailable();
    }

    public ConnectivityChangeReceiver(@NonNull NetworkCallback networkCallback) {
        this.networkCallback = networkCallback;
    }

    /**
     * Method to handle broadcast message receiving
     * - Runs on thread that registered receiver
     *
     * @param context: execution context
     * @param intent: execution initiator
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable(context)) {
            networkCallback.onAvailable();
        }
    }

    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isNetworkAvailable = networkInfo != null && networkInfo.isConnectedOrConnecting();
            Log.d(LOG_TAG, String.format("isNetworkAvailable(): %s", String.valueOf(isNetworkAvailable)));
            return isNetworkAvailable;
        } else
            return false;
    }

}
