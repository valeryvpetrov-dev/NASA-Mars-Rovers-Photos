package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifImageView;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Photo;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.RoverPhotoListResponse;
import ru.geekbrains.android.level2.valeryvpetrov.service.RoverInfoService;

import static ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter.dateToString;
import static ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter.stringToDate;

@UiThread
public class MainActivity
        extends AppCompatActivity
        implements SearchView.OnQueryTextListener, RoverSettingsDialogFragment.RoverSettingsDialogListener {

    private static final String SHARED_PREFERENCES_NAME = "ChosenRoverSP";
    private static final String SHARED_PREFERENCES_KEY_ROVER_ID = "id";
    private static final String SHARED_PREFERENCES_KEY_ROVER_NAME = "name";
    private static final String SHARED_PREFERENCES_KEY_ROVER_LANDING_DATE = "landingDate";
    private static final String SHARED_PREFERENCES_KEY_ROVER_LAUNCH_DATE = "launchDate";
    private static final String SHARED_PREFERENCES_KEY_ROVER_STATUS = "status";
    private static final String SHARED_PREFERENCES_KEY_ROVER_MAX_SOL = "maxSol";
    private static final String SHARED_PREFERENCES_KEY_ROVER_MAX_DATE = "maxDate";
    private static final String SHARED_PREFERENCES_KEY_ROVER_TOTAL_PHOTOS = "totalPhotos";

    private Handler handlerUI;

    private NASAMarsPhotosAPI nasaMarsPhotosAPI;

    // application bar functionality
    @BindView(R.id.toolbar)                     Toolbar toolbar;
    @BindView(R.id.text_view_name)              TextView textViewRoverName;
    @BindView(R.id.text_view_landing_date)      TextView textViewLandingDate;
    @BindView(R.id.text_view_launch_date)       TextView textViewLaunchDate;
    @BindView(R.id.text_view_status)            TextView textViewStatus;
    @BindView(R.id.text_view_max_sol)           TextView textViewMaxSol;
    @BindView(R.id.text_view_max_date)          TextView textViewMaxDate;
    @BindView(R.id.text_view_total_photos)      TextView textViewTotalPhotos;

    // search functionality
    SearchView searchViewPhotos;

    // photos recycler view functionality
    @BindView(R.id.progress_photos)             GifImageView viewProgressPhotos;
    @BindView(R.id.recycler_view_photos)        RecyclerView recyclerViewPhotos;
    private PhotoAdapter adapterPhotos;
    @Nullable
    private List<Photo> photoList;

    // rover settings dialog functionality
    private DialogFragment roverSettingsDialogFragment;

    // shared preferences functionality
    @Nullable
    private Rover roverPreferences;

    // service functionality
    private long latestSol;  // assigns via RoverInfoService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        handlerUI = new Handler(Looper.getMainLooper());

        nasaMarsPhotosAPI = NASAMarsPhotosAPI.getInstance();
        initUI();
        configureActionBar();

        roverPreferences = loadRoverFromPreferences();  // load chosen rover from SP
        if (roverPreferences != null) {
            startRoverInfoService();
            showRoverInfo(roverPreferences);
        } else {
            showRoverSettingsDialog();
        }
        handleLaunchIntent(getIntent());
    }

    private void initUI() {
        RecyclerView.LayoutManager recyclerViewPhotosLayoutManager = new LinearLayoutManager(this);
        recyclerViewPhotos.setLayoutManager(recyclerViewPhotosLayoutManager);
        photoList = new ArrayList<>();
        adapterPhotos = new PhotoAdapter(photoList);
        recyclerViewPhotos.setAdapter(adapterPhotos);
    }

    private void startRoverInfoService() {
        if (roverPreferences != null) {
            Intent roverInfoServiceIntent = new Intent(this, RoverInfoService.class);
            roverInfoServiceIntent.putExtra(RoverInfoService.EXTRA_ROVER_NAME, roverPreferences.getName());
            RoverInfoService.enqueueWork(this, roverInfoServiceIntent);
        }
    }

    private void handleLaunchIntent(@Nullable Intent intent) {
        if (intent != null) {
            // -1 means that activity launch intent came not from RoverInfoService
            latestSol = intent.getLongExtra(RoverInfoService.EXTRA_LATEST_SOL, -1);
        }
    }

    private void configureActionBar() {
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        itemSearch.expandActionView();
        searchViewPhotos = (SearchView) itemSearch.getActionView();
        searchViewPhotos.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchViewPhotos.setQueryHint(getString(R.string.search_view_hint_sol));
        searchViewPhotos.setOnQueryTextListener(this);

        if (latestSol != -1) {  // start activity form notification
            // called here because SearchView just initialized
            searchViewPhotos.setQuery(String.valueOf(latestSol), true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showRoverSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(@NonNull String query) {
        searchViewPhotos.clearFocus();  // hide keyboard after submission
        query = query.trim();
        if (query.length() > 0) {
            if (roverPreferences != null) { // rover is selected
                int sol = Integer.valueOf(query.trim());
                if (sol >= 0 && sol <= roverPreferences.getMaxSol()) {   // sol relates to rover settings
                    viewProgressPhotos.setVisibility(View.VISIBLE);
                    nasaMarsPhotosAPI
                            .getPhotosFromRoverBySol(roverPreferences.getName(), sol, 1)
                            .enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    handlerUI.post(() -> {
                                        viewProgressPhotos.setVisibility(View.GONE);
                                        showToast(getString(R.string.error_network_failure));
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    ResponseBody responseBody = response.body();
                                    if (responseBody != null) {
                                        String responseBodyString = responseBody.string();
                                        photoList = NASAMarsPhotosAPI.GSON
                                                .fromJson(responseBodyString, RoverPhotoListResponse.class)
                                                .getPhotos();
                                        if (photoList != null) {
                                            handlerUI.post(() -> {
                                                viewProgressPhotos.setVisibility(View.GONE);
                                                adapterPhotos.updatePhotoList(photoList);
                                            });
                                        }
                                    }
                                }
                            });
                    return true;
                } else {
                    showToast(getString(R.string.error_invalid_sol));
                }
            } else {
                showToast(getString(R.string.error_rover_not_chosen));
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(@NonNull String newText) {
        return false;
    }

    @Override
    public void onSaveClick(@NonNull Rover chosenRover) {
        roverPreferences = chosenRover;
        savePreferences(chosenRover);
        showRoverInfo(chosenRover);
        resetPhotoSearchResult();
    }

    private void resetPhotoSearchResult() {
        adapterPhotos.clearPhotoList();
        searchViewPhotos.setQuery("", false);
        searchViewPhotos.requestFocus();
        InputMethodManager inputMethodManager =
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        if (inputMethodManager != null)
            inputMethodManager
                    .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onCancelClick() {
        roverSettingsDialogFragment.getDialog().cancel();
    }

    @Nullable
    private Rover loadRoverFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        return getRover(sharedPreferences);
    }

    private void savePreferences(@NonNull Rover chosenRover) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        putRover(chosenRover, editor);
        editor.apply();
    }

    private void putRover(@NonNull Rover rover, @NonNull SharedPreferences.Editor editor) {
        editor.putLong(SHARED_PREFERENCES_KEY_ROVER_ID, rover.getId());
        editor.putString(SHARED_PREFERENCES_KEY_ROVER_NAME, rover.getName());
        editor.putString(SHARED_PREFERENCES_KEY_ROVER_LANDING_DATE, dateToString(rover.getLandingDate()));
        editor.putString(SHARED_PREFERENCES_KEY_ROVER_LAUNCH_DATE, dateToString(rover.getLaunchDate()));
        editor.putString(SHARED_PREFERENCES_KEY_ROVER_STATUS, rover.getStatus());
        editor.putLong(SHARED_PREFERENCES_KEY_ROVER_MAX_SOL, rover.getMaxSol());
        editor.putString(SHARED_PREFERENCES_KEY_ROVER_MAX_DATE, dateToString(rover.getMaxDate()));
        editor.putLong(SHARED_PREFERENCES_KEY_ROVER_TOTAL_PHOTOS, rover.getTotalPhotos());
    }

    @Nullable
    private Rover getRover(@NonNull SharedPreferences sharedPreferences) {
        Rover.RoverBuilder roverBuilder = Rover.builder();

        long id = sharedPreferences.getLong(SHARED_PREFERENCES_KEY_ROVER_ID, -1);
        if (id != -1) {
            try {
                return roverBuilder
                        .id(id)
                        .name(sharedPreferences.getString(SHARED_PREFERENCES_KEY_ROVER_NAME, null))
                        .landingDate(stringToDate(sharedPreferences.getString(SHARED_PREFERENCES_KEY_ROVER_LANDING_DATE, null)))
                        .launchDate(stringToDate(sharedPreferences.getString(SHARED_PREFERENCES_KEY_ROVER_LAUNCH_DATE, null)))
                        .status(sharedPreferences.getString(SHARED_PREFERENCES_KEY_ROVER_STATUS, null))
                        .maxSol(sharedPreferences.getLong(SHARED_PREFERENCES_KEY_ROVER_MAX_SOL, -1))
                        .maxDate(stringToDate(sharedPreferences.getString(SHARED_PREFERENCES_KEY_ROVER_MAX_DATE, null)))
                        .totalPhotos(sharedPreferences.getLong(SHARED_PREFERENCES_KEY_ROVER_TOTAL_PHOTOS, -1))
                        .build();
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private void showRoverInfo(@NonNull Rover rover) {
        textViewRoverName.setText(rover.getName());
        textViewLandingDate.setText(dateToString(rover.getLandingDate()));
        textViewLaunchDate.setText(dateToString(rover.getLaunchDate()));
        textViewStatus.setText(rover.getStatus());
        textViewMaxSol.setText(String.valueOf(rover.getMaxSol()));
        textViewMaxDate.setText(dateToString(rover.getMaxDate()));
        textViewTotalPhotos.setText(String.valueOf(rover.getTotalPhotos()));
    }

    private void showRoverSettingsDialog() {
        Rover chosenRover = loadRoverFromPreferences();
        if (chosenRover != null) {
            roverSettingsDialogFragment = new RoverSettingsDialogFragment(nasaMarsPhotosAPI, chosenRover.getName());
        } else {
            roverSettingsDialogFragment = new RoverSettingsDialogFragment(nasaMarsPhotosAPI);
        }
        roverSettingsDialogFragment.show(getSupportFragmentManager(), RoverSettingsDialogFragment.TAG);
    }

    private void showToast(@NonNull String message) {
        Toast.makeText(this,
                message,
                Toast.LENGTH_LONG)
                .show();
    }
}
