package ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model;

import java.util.List;

import lombok.Value;

@Value
public class RoverPhotoListResponse {

    List<Photo> photos;

}
