package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.geekbrains.android.level2.valeryvpetrov.BuildConfig;

public class NASAMarsRoversGenerator {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/";

    private static Gson gson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create();

    private static Retrofit.Builder builderRetrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson));

    private static Interceptor interceptorApiKey = chain -> {
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

    private static Interceptor interceptorLogging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder builderOkHttp = new OkHttpClient.Builder()
            .addInterceptor(interceptorApiKey)
            .addInterceptor(interceptorLogging);    // add as the last to log info from previously added interceptors

    private static Retrofit retrofit = builderRetrofit
            .client(builderOkHttp.build())
            .build();

    @NonNull
    public static <S> S createService(@NonNull Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
