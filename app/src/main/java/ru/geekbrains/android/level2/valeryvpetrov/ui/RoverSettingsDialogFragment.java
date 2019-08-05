package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosJsonParser;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;

public class RoverSettingsDialogFragment
        extends DialogFragment
        implements Callback {

    static final String TAG = RoverSettingsDialogListener.class.getName();

    public interface RoverSettingsDialogListener {
        void onSaveClick(Rover chosenRover);

        void onCancelClick();
    }

    private RoverSettingsDialogListener listener;

    private Handler handlerUI;

    private NASAMarsPhotosAPI nasaMarsPhotosAPI;
    private NASAMarsPhotosJsonParser nasaMarsPhotosJsonParser;

    private List<Rover> roverList;                  // list of all available rovers
    private Rover chosenRover;              // rover user want ot observe

    private RadioGroup radioGroupRoverNames;
    private GifImageView viewProgressRoverNames;

    RoverSettingsDialogFragment(NASAMarsPhotosAPI nasaMarsPhotosAPI,
                                NASAMarsPhotosJsonParser nasaMarsPhotosJsonParser) {
        this.nasaMarsPhotosAPI = nasaMarsPhotosAPI;
        this.nasaMarsPhotosJsonParser = nasaMarsPhotosJsonParser;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (RoverSettingsDialogListener) getActivity();
        handlerUI = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_rover_settings, null);
        initUI(view);
        builder
                .setView(view)
                .setPositiveButton(R.string.button_dialog_settings_save, (dialogInterface, i) -> listener.onSaveClick(chosenRover))
                .setNegativeButton(R.string.button_dialog_settings_cancel, (dialogInterface, i) -> listener.onCancelClick());
        return builder.create();
    }

    private void initUI(View view) {
        radioGroupRoverNames = view.findViewById(R.id.radio_group_rover_names);
        viewProgressRoverNames = view.findViewById(R.id.progress_rover_names);

        radioGroupRoverNames.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton checkedRadioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            String checkedRoverName = checkedRadioButton.getText().toString();
            for (Rover rover : roverList) {
                if (rover.name.equals(checkedRoverName)) {
                    chosenRover = rover;
                    break;
                }
            }
        });
        loadRoverList();
    }

    private void loadRoverList() {
        viewProgressRoverNames.setVisibility(View.VISIBLE);
        nasaMarsPhotosAPI.getRoverList().enqueue(this);
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        handlerUI.post(() -> {
            Toast.makeText(getContext(),
                    getString(R.string.error_network_failure),
                    Toast.LENGTH_LONG).show();
            listener.onCancelClick();
        });
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        roverList = nasaMarsPhotosJsonParser.deserializeList(Rover.class,
                response.body().string(),
                NASAMarsPhotosAPI.JSON_ROOT_NAME_ROVER_LIST);
        if (roverList != null) {
            handlerUI.post(() -> {
                viewProgressRoverNames.setVisibility(View.GONE);
                inflateRoverNames(roverList);
            });
        }
    }

    private void inflateRoverNames(List<Rover> roverList) {
        for (Rover rover : roverList) {
            RadioButton radioButtonRoverName = new RadioButton(getContext());
            radioButtonRoverName.setText(rover.name);
            radioGroupRoverNames.addView(radioButtonRoverName);
        }
    }
}