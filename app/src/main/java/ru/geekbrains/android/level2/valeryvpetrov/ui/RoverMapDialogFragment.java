package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.material.snackbar.Snackbar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.dbpedia.DBPediaAPIGenerator;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.dbpedia.DBPediaRoverAPI;
import ru.geekbrains.android.level2.valeryvpetrov.receiver.ConnectivityChangeReceiver;

import static android.content.Context.MODE_PRIVATE;
import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_KEY_ROVER_LANDING_LAT;
import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_KEY_ROVER_LANDING_LNG;
import static ru.geekbrains.android.level2.valeryvpetrov.NasaRoversApplication.SHARED_PREFERENCES_NAME;

/**
 * Based on example:
 * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/TileOverlayDemoActivity.java
 */
public class RoverMapDialogFragment
        extends DialogFragment
        implements OnMapReadyCallback {

    static final String TAG = RoverMapDialogFragment.class.getName();

    private static final String[] MARS_MAP_URL_FORMAT = new String[]{
            "http://mw1.google.com/mw-planetary/mars/visible/",
            "http://mw1.google.com/mw-planetary/mars/infrared/",
            "http://mw1.google.com/mw-planetary/mars/elevation/"};

    @Nullable
    private String selectedMapType;

    private GoogleMap googleMap;

    @NonNull
    private String roverName;
    @Nullable
    private LatLng landingLatLng;
    private boolean noLandingInfo;

    @NonNull
    private ConnectivityChangeReceiver.NetworkCallback networkCallbackLTN;  // used versions less than N
    @NonNull
    private ConnectivityManager.NetworkCallback networkCallbackGTEN;        // used versions and greater then or equal N
    @NonNull
    private DBPediaRoverAPI dbPediaRoverAPI;
    @NonNull
    private Callback<LatLng> roverLatLngResponseCallback;

    @NonNull
    private Handler handlerUI;

    @BindView(R.id.map_type)
    Spinner mapType;

    RoverMapDialogFragment(@NonNull String roverName) {
        this.roverName = roverName;
        this.noLandingInfo = false; // assume that info exists
        this.handlerUI = new Handler(Looper.getMainLooper());

        dbPediaRoverAPI = DBPediaAPIGenerator.getInstance().createService(DBPediaRoverAPI.class);
        roverLatLngResponseCallback = new Callback<LatLng>() {
            @MainThread
            @Override
            public void onResponse(@NonNull Call<LatLng> call,
                                   @NonNull Response<LatLng> response) {
                if (googleMap == null) return;
                if (response.body() == null) {
                    if (getContext() != null)
                        showSnackbar(getContext().getString(R.string.error_get_landing_location));
                    return;
                }
                if (getActivity() != null)
                    ConnectivityChangeReceiver.unregisterConnectivityChangeReceiver(getActivity(),
                            networkCallbackGTEN);

                landingLatLng = response.body();
                saveLandingLatLngToPreferences(roverName, landingLatLng);
                noLandingInfo = false;
                refreshMap();
            }

            @MainThread
            @Override
            public void onFailure(@NonNull Call<LatLng> call,
                                  @NonNull Throwable t) {
                if (getContext() != null)
                    showSnackbar(getContext().getString(R.string.error_get_landing_location));
                if (getActivity() != null)
                    ConnectivityChangeReceiver.registerConnectivityChangeReceiver(getActivity(),
                            networkCallbackGTEN, networkCallbackLTN);
                noLandingInfo = true;   // there is no landing info so not to call api again
            }
        };
        networkCallbackGTEN = new ConnectivityManager.NetworkCallback() {
            @WorkerThread
            @Override
            public void onAvailable(@NonNull Network network) {
                handlerUI.post(() -> callRoverLatLng());
                super.onAvailable(network);
            }
        };
        networkCallbackLTN = () -> handlerUI.post(this::callRoverLatLng);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        this.landingLatLng = loadLandingLatLngFromPreferences(roverName);
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rover_map, container, false);
        ButterKnife.bind(this, view);
        onCreateViewMap();
        onCreateViewMapType();
        return view;
    }

    private void onCreateViewMap() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            SupportMapFragment mapFragment = new SupportMapFragment();

            mapFragment.getMapAsync(this);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }
    }

    private void onCreateViewMapType() {
        if (getActivity() == null) return;

        String[] mapTypes = getResources().getStringArray(R.array.map_types);
        BaseAdapter mapTypeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                mapTypes);
        ((ArrayAdapter) mapTypeAdapter).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapType.setAdapter(mapTypeAdapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        refreshMap();
        addLandingMarker(landingLatLng);
    }

    private void refreshMap() {
        callRoverLatLng();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                // Based on example:
                // https://github.com/bilal-karim/gmaps-samples-v3/blob/master/planetary-maptypes/planetary-maptypes.html
                return getHorizontallyRepeatingTileUrl(x, y, zoom);
            }
        };

        TileOverlay tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
        tileOverlay.setFadeIn(true);
    }

    private void callRoverLatLng() {
        if (landingLatLng == null && !noLandingInfo)
            dbPediaRoverAPI.getRoverLatLng(roverName).enqueue(roverLatLngResponseCallback);
    }

    @Nullable
    private URL getHorizontallyRepeatingTileUrl(int x, int y, int zoom) {
        // tile range in one direction range is dependent on zoom level
        // 0 = 1 tile, 1 = 2 tiles, 2 = 4 tiles, 3 = 8 tiles, etc
        int tileRange = 1 << zoom;

        // don't repeat across y-axis (vertically)
        if (y < 0 || y >= tileRange) {
            return null;
        }

        // repeat across x-axis
        if (x < 0 || x >= tileRange) {
            x = (x % tileRange + tileRange) % tileRange;
        }

        return selectedMapType != null ?
                getMarsTileUrl(getMarsMapUrl(selectedMapType), x, y, zoom) :
                null;
    }

    @NonNull
    private String getMarsMapUrl(@NonNull String selectedMapType) {
        for (String marsMapUrl : MARS_MAP_URL_FORMAT) {
            if (marsMapUrl.contains(selectedMapType))
                return marsMapUrl;
        }
        return MARS_MAP_URL_FORMAT[0];
    }

    @Nullable
    private URL getMarsTileUrl(@NonNull String baseUrl,
                               int x, int y, int zoom) {
        double bound = Math.pow(2, zoom);
        List<String> quads = new ArrayList<>();
        quads.add("t");

        for (int z = 0; z < zoom; z++) {
            bound = bound / 2;
            if (y < bound) {
                if (x < bound) {
                    quads.add("q");
                } else {
                    quads.add("r");
                    x -= bound;
                }
            } else {
                if (x < bound) {
                    quads.add("t");
                    y -= bound;
                } else {
                    quads.add("s");
                    x -= bound;
                    y -= bound;
                }
            }
        }

        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < quads.size(); i++) {
                stringBuilder.append(quads.get(i));

                if (i < quads.size() - 1)
                    stringBuilder.append("");
            }

            return new URL(baseUrl + stringBuilder.toString() + ".jpg");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addLandingMarker(@Nullable LatLng landingLatLng) {
        if (landingLatLng == null) return;

        googleMap.addMarker(new MarkerOptions().position(landingLatLng)
                .title(String.format("%s landing location", roverName)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(landingLatLng));
    }

    @Nullable
    private LatLng loadLandingLatLngFromPreferences(@NonNull String roverName) {
        if (getActivity() == null) return null;

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        float landingLat = sharedPreferences
                .getFloat(String.format(SHARED_PREFERENCES_KEY_ROVER_LANDING_LAT, roverName), 91);
        float landingLng = sharedPreferences
                .getFloat(String.format(SHARED_PREFERENCES_KEY_ROVER_LANDING_LNG, roverName), 181);

        // latitude belongs to [-90; +90], longitude belongs to [-180; +180]
        if (landingLat == 91 && landingLng == 181) return null;

        return new LatLng(landingLat, landingLng);
    }

    private void saveLandingLatLngToPreferences(@NonNull String roverName,
                                                @NonNull LatLng landingLatLng) {
        if (getActivity() == null) return;

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(String.format(SHARED_PREFERENCES_KEY_ROVER_LANDING_LAT, roverName),
                (float) landingLatLng.latitude);
        editor.putFloat(String.format(SHARED_PREFERENCES_KEY_ROVER_LANDING_LNG, roverName),
                (float) landingLatLng.longitude);
        editor.apply();
    }

    @OnItemSelected(R.id.map_type)
    void onMapTypeSelected(@NonNull Spinner spinner) {
        selectedMapType = spinner.getSelectedItem().toString().toLowerCase();
        refreshMap();
    }

    private void showSnackbar(@NonNull String message) {
        if (getView() != null) {
            Snackbar.make(getView(),
                    message,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

}
