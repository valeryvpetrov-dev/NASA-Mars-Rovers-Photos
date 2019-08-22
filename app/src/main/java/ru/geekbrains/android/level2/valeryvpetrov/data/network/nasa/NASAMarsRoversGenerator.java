package ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa;

import com.google.gson.GsonBuilder;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.geekbrains.android.level2.valeryvpetrov.BuildConfig;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NetworkAPIGenerator;

public class NASAMarsRoversGenerator
        extends NetworkAPIGenerator {

    static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/";

    private static NASAMarsRoversGenerator instance;

    private NASAMarsRoversGenerator() {
        builder = new Builder(BASE_URL) {
            @Override
            public Builder setGson() {
                NASAMarsRoversGenerator.this.gson = new GsonBuilder()
                        .setDateFormat(DATE_FORMAT)
                        .create();
                return this;
            }

            @Override
            public Builder setOkHttpClient() {
                Interceptor interceptorLogging = new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY);

                Interceptor interceptorApiKey = chain -> {
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

                NASAMarsRoversGenerator.this.okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(interceptorApiKey)
                        .addInterceptor(interceptorLogging) // add as the last to log info from previously added interceptors
                        .build();
                return this;
            }
        };
    }

    public static NASAMarsRoversGenerator getInstance() {
        if (instance == null) {
            instance = new NASAMarsRoversGenerator();
        }
        return instance;
    }
}
