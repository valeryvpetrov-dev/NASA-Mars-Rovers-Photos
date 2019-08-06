
package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import com.google.gson.annotations.SerializedName;

import lombok.Value;

@Value
public class Camera {

    long id;
    @SerializedName("rover_id")
    long roverId;
    @SerializedName("full_name")
    String fullName;
    String name;

}
