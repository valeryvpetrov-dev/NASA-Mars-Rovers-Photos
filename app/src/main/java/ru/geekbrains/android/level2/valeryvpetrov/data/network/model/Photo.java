
package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Photo {

    @SerializedName("id")
    public int id;
    @SerializedName("sol")
    public int sol;
    @SerializedName("camera")
    public Camera camera;
    @SerializedName("img_src")
    public String imgSrc;
    @SerializedName("earth_date")
    public Date earthDate;
}
