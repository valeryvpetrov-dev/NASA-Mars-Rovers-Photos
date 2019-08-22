package ru.geekbrains.android.level2.valeryvpetrov.data.network.dbpedia;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DBPediaRoverAPI {

    @GET("data/{roverName}_(rover).json")
    @NonNull
    Call<LatLng> getRoverLatLng(@Path("roverName") @NonNull String roverName);

}
