package ru.geekbrains.android.level2.valeryvpetrov.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = ConnectivityChangeReceiver.class.getSimpleName();

    private static ConnectivityChangeReceiver connectivityChangeReceiver;          // used versions less than N

    private NetworkCallback networkCallback;

    private static boolean isRegistered;

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
     * @param intent:  execution initiator
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

    public static void registerConnectivityChangeReceiver(@NonNull Context context,
                                                          @NonNull ConnectivityManager.NetworkCallback networkCallbackGTEN,
                                                          @NonNull NetworkCallback networkCallbackLTN) {
        // https://developer.android.com/about/versions/nougat/android-7.0-changes.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                // https://developer.android.com/reference/android/net/ConnectivityManager.html#registerDefaultNetworkCallback
                connectivityManager.registerDefaultNetworkCallback(networkCallbackGTEN);
                isRegistered = true;
            }
        } else {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            connectivityChangeReceiver = new ConnectivityChangeReceiver(networkCallbackLTN);
            Handler serviceHandle = new Handler();  // binds to service thread
            // https://developer.android.com/reference/android/content/Context#registerReceiver(android.content.BroadcastReceiver,%2520android.content.IntentFilter,%2520java.lang.String,%2520android.os.Handler)
            context.registerReceiver(connectivityChangeReceiver, filter, null, serviceHandle);
            isRegistered = true;
        }
    }

    public static void unregisterConnectivityChangeReceiver(@NonNull Context context,
                                                            @NonNull ConnectivityManager.NetworkCallback networkCallbackGTEN) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null && isRegistered) {
                connectivityManager.unregisterNetworkCallback(networkCallbackGTEN);
                isRegistered = false;
            }
        } else {
            if (connectivityChangeReceiver != null && isRegistered) {
                context.unregisterReceiver(connectivityChangeReceiver);
                isRegistered = false;
            }
        }
    }

}
