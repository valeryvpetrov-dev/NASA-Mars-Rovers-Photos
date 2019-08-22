package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.app.Activity;
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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.NASAMarsRoverAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.NASAMarsRoversGenerator;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model.Rover;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model.RoverListResponse;

@UiThread
public class RoverSettingsDialogFragment
        extends DialogFragment {

    static final String TAG = RoverSettingsDialogFragment.class.getName();

    public interface RoverSettingsDialogListener {

        void onSaveClick(Rover chosenRover);

        void onCancelClick();

    }

    private RoverSettingsDialogListener listener;

    private Handler handlerUI;

    @NonNull
    private NASAMarsRoverAPI nasaMarsRoverAPI;
    private Callback<RoverListResponse> roverListResponseCallback;

    private List<Rover> roverList;                  // list of all available rovers

    private Rover chosenRover;              // rover user want ot observe
    @Nullable
    private String chosenRoverName;             // previously chosen rover name

    @BindView(R.id.radio_group_rover_names)
    RadioGroup radioGroupRoverNames;
    @BindView(R.id.progress_rover_names)
    GifImageView viewProgressRoverNames;

    RoverSettingsDialogFragment(@NonNull RoverSettingsDialogListener listener) {
        this.listener = listener;
        nasaMarsRoverAPI = NASAMarsRoversGenerator.getInstance()
                .createService(NASAMarsRoverAPI.class);
        roverListResponseCallback = new Callback<RoverListResponse>() {
            @MainThread
            @Override
            public void onResponse(@NonNull Call<RoverListResponse> call,
                                   @NonNull Response<RoverListResponse> response) {
                if (response.body() != null) {
                    roverList = response.body().getRovers();
                    if (roverList != null) {
                        handlerUI.post(() -> {
                            viewProgressRoverNames.setVisibility(View.GONE);
                            inflateRoverNames(roverList);
                        });
                    }
                }
            }

            @MainThread
            @Override
            public void onFailure(@NonNull Call<RoverListResponse> call,
                                  @NonNull Throwable t) {
                handlerUI.post(() -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity,
                                activity.getString(R.string.error_network_failure),
                                Toast.LENGTH_LONG).show();
                        listener.onCancelClick();
                    }
                });
            }
        };
    }

    RoverSettingsDialogFragment(@NonNull RoverSettingsDialogListener listener,
                                @NonNull String chosenRoverName) {
        this(listener);
        this.chosenRoverName = chosenRoverName;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        handlerUI = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_rover_settings, null);
        ButterKnife.bind(this, view);
        radioGroupRoverNames.setOnCheckedChangeListener(((radioGroup, i) -> {
            RadioButton checkedRadioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
            String checkedRoverName = checkedRadioButton.getText().toString();
            for (Rover rover : roverList) {
                if (rover.getName().equals(checkedRoverName)) {
                    chosenRover = rover;
                    break;
                }
            }
        }));
        builder
                .setView(view)
                .setPositiveButton(R.string.button_dialog_settings_save, (dialogInterface, i) ->
                        listener.onSaveClick(chosenRover))
                .setNegativeButton(R.string.button_dialog_settings_cancel, (dialogInterface, i) ->
                        listener.onCancelClick());
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadRoverList();
    }

    private void loadRoverList() {
        viewProgressRoverNames.setVisibility(View.VISIBLE);
        nasaMarsRoverAPI.getRoverList().enqueue(roverListResponseCallback);
    }

    private void inflateRoverNames(@NonNull List<Rover> roverList) {
        RadioButton radioButtonRoverNameChecked = null;
        for (Rover rover : roverList) {
            RadioButton radioButtonRoverName = new RadioButton(getContext());
            radioButtonRoverName.setText(rover.getName());
            if (chosenRoverName != null && chosenRoverName.equals(rover.getName())) {
                radioButtonRoverNameChecked = radioButtonRoverName;
            }
            radioGroupRoverNames.addView(radioButtonRoverName);
        }

        if (radioButtonRoverNameChecked != null) {
            radioButtonRoverNameChecked.setChecked(true);
        }
    }
}
