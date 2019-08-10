package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.RoverPhotoListResponse;

public interface NASAMarsRoverPhotoAPI {

    @GET("rovers/{name}/photos")
    Call<RoverPhotoListResponse> getPhotoList(@Path("name") @NonNull String roverName,
                                              @Query("sol") @IntRange(from = 0) int sol,
                                              @Query("page") @IntRange(from = 0) int page);

}
