
package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Rover {

    public static final String SERIALIZED_NAME_FILED_ID = "id";
    public static final String SERIALIZED_NAME_FILED_NAME = "name";
    public static final String SERIALIZED_NAME_FILED_LANDING_DATE = "landing_date";
    public static final String SERIALIZED_NAME_FILED_LAUNCH_DATE = "launch_date";
    public static final String SERIALIZED_NAME_FILED_STATUS = "status";
    public static final String SERIALIZED_NAME_FILED_MAX_SOL = "max_sol";
    public static final String SERIALIZED_NAME_FILED_MAX_DATE = "max_date";
    public static final String SERIALIZED_NAME_FILED_TOTAL_PHOTOS = "total_photos";
    public static final String SERIALIZED_NAME_FILED_CAMERAS = "cameras";

    @SerializedName(SERIALIZED_NAME_FILED_ID)
    public int id;
    @SerializedName(SERIALIZED_NAME_FILED_NAME)
    public String name;
    @SerializedName(SERIALIZED_NAME_FILED_LANDING_DATE)
    public Date landingDate;
    @SerializedName(SERIALIZED_NAME_FILED_LAUNCH_DATE)
    public Date launchDate;
    @SerializedName(SERIALIZED_NAME_FILED_STATUS)
    public String status;
    @SerializedName(SERIALIZED_NAME_FILED_MAX_SOL)
    public int maxSol;
    @SerializedName(SERIALIZED_NAME_FILED_MAX_DATE)
    public Date maxDate;
    @SerializedName(SERIALIZED_NAME_FILED_TOTAL_PHOTOS)
    public int totalPhotos;
    @SerializedName(SERIALIZED_NAME_FILED_CAMERAS)
    public List<Camera_> cameras = null;
}
