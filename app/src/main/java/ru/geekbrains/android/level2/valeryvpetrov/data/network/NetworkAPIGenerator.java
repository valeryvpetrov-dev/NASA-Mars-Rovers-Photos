package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class NetworkAPIGenerator {

    protected Gson gson;
    protected OkHttpClient okHttpClient;
    @Getter
    protected Retrofit retrofit;
    protected Builder builder;

    public abstract class Builder {

        private String baseUrl;

        protected Builder(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public abstract Builder setGson();

        public abstract Builder setOkHttpClient();

        public NetworkAPIGenerator build() {
            NetworkAPIGenerator.this.retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            return NetworkAPIGenerator.this;
        }
    }

    @NonNull
    public <S> S createService(@NonNull Class<S> serviceClass) {
        return builder
                .setGson()
                .setOkHttpClient()
                .build()
                .getRetrofit().create(serviceClass);
    }

}
