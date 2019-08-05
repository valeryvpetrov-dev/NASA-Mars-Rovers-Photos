package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import ru.geekbrains.android.level2.valeryvpetrov.BuildConfig;

public class NASAMarsPhotosAPI {

    public static final String JSON_ROOT_NAME_ROVER_LIST = "rovers";
    public static final String JSON_ROOT_NAME_PHOTO_LIST = "photos";
    public static final String JSON_ROOT_NAME_ROVER = "rover";

    private static final String API_ROOT = "https://api.nasa.gov/mars-photos/api/v1";

    private Interceptor interceptorApiKey;
    private OkHttpClient okHttpClient;

    private static NASAMarsPhotosAPI INSTANCE;

    private NASAMarsPhotosAPI() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        interceptorApiKey = chain -> {
            Request newRequest = chain.request();
            HttpUrl newUrl = newRequest
                    .url()
                    .newBuilder()
                    .addQueryParameter("api_key", BuildConfig.NasaOpenApiKey)
                    .build();
            newRequest = newRequest
                    .newBuilder()
                    .url(newUrl)
                    .build();
            return chain.proceed(newRequest);
        };
        okHttpClient = builder
                .addInterceptor(interceptorApiKey)
                .build();
    }

    public static NASAMarsPhotosAPI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NASAMarsPhotosAPI();
        }
        return INSTANCE;
    }

    public Call getRoverByName(String roverName) {
        return makeCall(String.format("%s/rovers/%s/", API_ROOT, roverName), null);
    }

    public Call getRoverList() {
        return makeCall(String.format("%s/rovers/", API_ROOT), null);
    }

    public Call getPhotosFromRoverBySol(@NonNull String roverName, int sol, int page) {
        Map<String, String> queryParameterMap = new HashMap<>();
        queryParameterMap.put("sol", String.valueOf(sol));
        queryParameterMap.put("page", String.valueOf(page));
        return makeCall(String.format("%s/rovers/%s/photos", API_ROOT, roverName), queryParameterMap);
    }

    private Call makeCall(@NonNull String urlString, @Nullable Map<String, String> queryParameterMap) {
        HttpUrl httpUrl = HttpUrl.parse(urlString);
        if (httpUrl != null) {
            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();

            if (queryParameterMap != null) {
                for (String key : queryParameterMap.keySet())
                    urlBuilder.addQueryParameter(key, queryParameterMap.get(key));
            }

            HttpUrl url = urlBuilder.build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            return okHttpClient.newCall(request);
        }
        return null;
    }
}
