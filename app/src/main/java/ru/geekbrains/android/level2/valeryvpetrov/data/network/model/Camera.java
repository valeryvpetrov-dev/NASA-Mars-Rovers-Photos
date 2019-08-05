
package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

public class Camera {

    @SerializedName("id")
    public int id;
    @SerializedName("name")
    public String name;
    @SerializedName("rover_id")
    public int roverId;
    @SerializedName("full_name")
    public String fullName;
}
