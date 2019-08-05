package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosJsonParser;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Photo;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;

public class MainActivity
        extends AppCompatActivity
        implements SearchView.OnQueryTextListener, RoverSettingsDialogFragment.RoverSettingsDialogListener {

    private Handler handlerUI;

    private NASAMarsPhotosAPI nasaMarsPhotosAPI;
    private NASAMarsPhotosJsonParser nasaMarsPhotosJsonParser;

    private SearchView searchViewPhotos;
    private GifImageView viewProgressPhotos;
    private PhotoAdapter adapterPhotos;
    private List<Photo> photoList;

    private DialogFragment roverSettingsDialogFragment;

    private Rover roverPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlerUI = new Handler(Looper.getMainLooper());

        nasaMarsPhotosAPI = NASAMarsPhotosAPI.getInstance();
        nasaMarsPhotosJsonParser = NASAMarsPhotosJsonParser.getInstance();
        initUI();
        configureActionBar();

        if (roverPreferences != null) {
            showRoverInfo(roverPreferences);
        } else {
            showRoverSettingsDialog();
        }
    }

    private void initUI() {
        viewProgressPhotos = findViewById(R.id.progress_photos);
        RecyclerView recyclerViewPhotos = findViewById(R.id.recycler_view_photos);
        RecyclerView.LayoutManager recyclerViewPhotosLayoutManager = new LinearLayoutManager(this);
        recyclerViewPhotos.setLayoutManager(recyclerViewPhotosLayoutManager);
        photoList = new ArrayList<>();
        adapterPhotos = new PhotoAdapter(photoList);
        recyclerViewPhotos.setAdapter(adapterPhotos);
    }

    private void configureActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
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
    public boolean onQueryTextSubmit(String query) {
        searchViewPhotos.clearFocus();  // hide keyboard after submission
        query = query.trim();
        if (query.length() > 0) {
            if (roverPreferences != null) { // rover is selected
                int sol = Integer.valueOf(query.trim());
                if (sol >= 0 && sol <= roverPreferences.maxSol) {   // sol relates to rover settings
                    viewProgressPhotos.setVisibility(View.VISIBLE);
                    nasaMarsPhotosAPI.getPhotosFromRoverBySol(roverPreferences.name, sol, 1).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            handlerUI.post(() -> {
                                viewProgressPhotos.setVisibility(View.GONE);
                                showToast(getString(R.string.error_network_failure));
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            photoList = nasaMarsPhotosJsonParser.deserializeList(Photo.class,
                                    response.body().string(),
                                    NASAMarsPhotosAPI.JSON_ROOT_NAME_PHOTO_LIST);
                            if (photoList != null) {
                                handlerUI.post(() -> {
                                    viewProgressPhotos.setVisibility(View.GONE);
                                    adapterPhotos.updatePhotoList(photoList);
                                });
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
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onSaveClick(Rover chosenRover) {
        roverPreferences = chosenRover;
        showRoverInfo(chosenRover);
    }

    @Override
    public void onCancelClick() {
        roverSettingsDialogFragment.getDialog().cancel();
    }

    private void showRoverInfo(Rover rover) {
        ((TextView) findViewById(R.id.text_view_name)).setText(rover.name);
        ((TextView) findViewById(R.id.text_view_landing_date)).setText(TypeConverter.dateToString(rover.landingDate));
        ((TextView) findViewById(R.id.text_view_launch_date)).setText(TypeConverter.dateToString(rover.launchDate));
        ((TextView) findViewById(R.id.text_view_status)).setText(rover.status);
        ((TextView) findViewById(R.id.text_view_max_sol)).setText(String.valueOf(rover.maxSol));
        ((TextView) findViewById(R.id.text_view_max_date)).setText(TypeConverter.dateToString(rover.maxDate));
        ((TextView) findViewById(R.id.text_view_total_photos)).setText(String.valueOf(rover.totalPhotos));
    }

    private void showRoverSettingsDialog() {
        roverSettingsDialogFragment = new RoverSettingsDialogFragment(nasaMarsPhotosAPI,
                nasaMarsPhotosJsonParser);
        roverSettingsDialogFragment.show(getSupportFragmentManager(), RoverSettingsDialogFragment.TAG);
    }

    private void showToast(String message) {
        Toast.makeText(this,
                message,
                Toast.LENGTH_LONG)
                .show();
    }
}