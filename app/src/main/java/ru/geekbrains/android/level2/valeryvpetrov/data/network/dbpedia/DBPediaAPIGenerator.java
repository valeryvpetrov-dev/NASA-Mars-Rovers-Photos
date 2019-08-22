package ru.geekbrains.android.level2.valeryvpetrov.data.network.dbpedia;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NetworkAPIGenerator;

public class DBPediaAPIGenerator
        extends NetworkAPIGenerator {

    private static final String BASE_URL = "http://dbpedia.org/";

    private static DBPediaAPIGenerator instance;

    private DBPediaAPIGenerator() {
        builder = new Builder(BASE_URL) {
            @Override
            public Builder setGson() {
                DBPediaAPIGenerator.this.gson = new GsonBuilder()
                        .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
                        .create();
                return this;
            }

            @Override
            public Builder setOkHttpClient() {
                Interceptor interceptorLogging = new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY);

                DBPediaAPIGenerator.this.okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(interceptorLogging)
                        .build();
                return this;
            }
        };
    }

    public static DBPediaAPIGenerator getInstance() {
        if (instance == null) {
            instance = new DBPediaAPIGenerator();
        }
        return instance;
    }

    public static class LatLngTypeAdapter implements JsonDeserializer<LatLng> {

        private static final String PATTERN_ROVER_INFO =
                "http://dbpedia.org/resource/(\\w+)_\\(rover\\)";

        private static final String ROVER_INFO_KEY_LAT =
                "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
        private static final String ROVER_INFO_KEY_LONG =
                "http://www.w3.org/2003/01/geo/wgs84_pos#long";

        private Pattern pattern;

        public LatLngTypeAdapter() {
            pattern = Pattern.compile(PATTERN_ROVER_INFO);
        }

        @Override
        public LatLng deserialize(JsonElement json,
                                  Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException {
            final Set<Map.Entry<String, JsonElement>> entries = json.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String key = entry.getKey();
                if (pattern.matcher(key).matches()) {
                    JsonObject roverInfo = entry.getValue().getAsJsonObject();
                    JsonObject roverInfoLat = roverInfo.get(ROVER_INFO_KEY_LAT).getAsJsonArray()
                            .get(0).getAsJsonObject();
                    JsonObject roverInfoLng = roverInfo.get(ROVER_INFO_KEY_LONG).getAsJsonArray()
                            .get(0).getAsJsonObject();

                    return new LatLng(roverInfoLat.get("value").getAsFloat(),
                            roverInfoLng.get("value").getAsFloat());
                }
            }
            return null;
        }
    }
}
