package ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Camera implements Parcelable {

    long id;
    @SerializedName("rover_id")
    long roverId;
    @SerializedName("full_name")
    String fullName;
    String name;

    public Camera(long id, long roverId, String fullName, String name) {
        this.id = id;
        this.roverId = roverId;
        this.fullName = fullName;
        this.name = name;
    }

    public Camera(Parcel in) {
        id = in.readLong();
        roverId = in.readLong();
        fullName = in.readString();
        name = in.readString();
    }

    public static final Creator<Camera> CREATOR = new Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(roverId);
        parcel.writeString(fullName);
        parcel.writeString(name);
    }
}
