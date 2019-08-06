
package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Rover {

    List<Camera> cameras;
    long id;
    @SerializedName("landing_date")
    Date landingDate;
    @SerializedName("launch_date")
    Date launchDate;
    @SerializedName("max_date")
    Date maxDate;
    @SerializedName("max_sol")
    long maxSol;
    String name;
    String status;
    @SerializedName("total_photos")
    long totalPhotos;

}
