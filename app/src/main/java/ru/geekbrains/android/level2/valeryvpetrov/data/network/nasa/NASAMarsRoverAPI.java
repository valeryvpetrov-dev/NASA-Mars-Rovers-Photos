package ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model.RoverDetailsResponse;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model.RoverListResponse;

public interface NASAMarsRoverAPI {

    @GET("rovers/{name}")
    @NonNull
    Call<RoverDetailsResponse> getRoverDetails(@Path("name") @NonNull String name);

    @GET("rovers/")
    @NonNull
    Call<RoverListResponse> getRoverList();

}
