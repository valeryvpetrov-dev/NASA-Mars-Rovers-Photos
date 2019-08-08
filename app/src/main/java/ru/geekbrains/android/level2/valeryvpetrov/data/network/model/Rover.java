package ru.geekbrains.android.level2.valeryvpetrov.data.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rover implements Parcelable {

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

    public Rover(List<Camera> cameras, long id, Date landingDate, Date launchDate, Date maxDate, long maxSol, String name, String status, long totalPhotos) {
        this.cameras = cameras;
        this.id = id;
        this.landingDate = landingDate;
        this.launchDate = launchDate;
        this.maxDate = maxDate;
        this.maxSol = maxSol;
        this.name = name;
        this.status = status;
        this.totalPhotos = totalPhotos;
    }

    public Rover(Parcel in) {
        this.cameras = in.createTypedArrayList(Camera.CREATOR);
        this.id = in.readLong();
        this.landingDate = (Date) in.readSerializable();
        this.launchDate = (Date) in.readSerializable();
        this.maxDate = (Date) in.readSerializable();
        this.maxSol = in.readLong();
        this.name = in.readString();
        this.status = in.readString();
        this.totalPhotos = in.readLong();
    }

    public static final Creator<Rover> CREATOR = new Creator<Rover>() {
        @Override
        public Rover createFromParcel(Parcel in) {
            return new Rover(in);
        }

        @Override
        public Rover[] newArray(int size) {
            return new Rover[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(cameras);
        parcel.writeLong(id);
        parcel.writeSerializable(landingDate);
        parcel.writeSerializable(launchDate);
        parcel.writeSerializable(maxDate);
        parcel.writeLong(maxSol);
        parcel.writeString(name);
        parcel.writeString(status);
        parcel.writeLong(totalPhotos);
    }
}
