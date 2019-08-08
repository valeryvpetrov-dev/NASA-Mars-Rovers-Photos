package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Value;

@Value
public class Photo {

    Camera camera;
    @SerializedName("earth_date")
    Date earthDate;
    long id;
    @SerializedName("img_src")
    String imgSrc;
    Rover rover;
    long sol;

}
