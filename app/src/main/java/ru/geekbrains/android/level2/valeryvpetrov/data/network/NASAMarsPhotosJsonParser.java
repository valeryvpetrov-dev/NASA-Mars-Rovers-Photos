package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Photo;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;

public class NASAMarsPhotosJsonParser {

    static final String DATE_FORMAT = "yyyy-MM-dd";

    private Gson gson;

    private static NASAMarsPhotosJsonParser INSTANCE;

    private NASAMarsPhotosJsonParser() {
        gson = new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();
    }

    public static NASAMarsPhotosJsonParser getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NASAMarsPhotosJsonParser();
        }
        return INSTANCE;
    }

    @Nullable
    public Object deserialize(Class objectClass, String objectJson, @Nullable String rootName) {
        try {
            JSONObject responseJSON = new JSONObject(objectJson);

            Type typeObject = null;
            if (objectClass.equals(Rover.class))
                typeObject = new TypeToken<Rover>() {}.getType();
            else if (objectClass.equals(Photo.class))
                typeObject = new TypeToken<Photo>() {}.getType();

            Object o;
            if (rootName != null)
                o = gson.fromJson(responseJSON.getString(rootName), typeObject);
            else
                o = gson.fromJson(responseJSON.toString(), typeObject);

            return o;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public List deserializeList(Class itemClass, String listJson, @Nullable String rootName) {
        try {
            JSONObject responseJSON = new JSONObject(listJson);

            Type typeList = null;
            if (itemClass.equals(Rover.class))
                typeList = new TypeToken<List<Rover>>() {}.getType();
            else if (itemClass.equals(Photo.class))
                typeList = new TypeToken<List<Photo>>() {}.getType();

            List<Object> list;
            if (rootName != null)
                list = gson.fromJson(responseJSON.getString(rootName), typeList);
            else
                list = gson.fromJson(responseJSON.toString(), typeList);

            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
