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
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifImageView;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.NASAMarsPhotosAPI;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Rover;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.RoverListResponse;

@UiThread
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

    @NonNull
    private NASAMarsPhotosAPI nasaMarsPhotosAPI;
    private List<Rover> roverList;                  // list of all available rovers

    private Rover chosenRover;              // rover user want ot observe
    @Nullable
    private String chosenRoverName;             // previously chosen rover name

    @BindView(R.id.radio_group_rover_names)             RadioGroup radioGroupRoverNames;
    @BindView(R.id.progress_rover_names)                GifImageView viewProgressRoverNames;

    RoverSettingsDialogFragment(@NonNull NASAMarsPhotosAPI nasaMarsPhotosAPI) {
        this.nasaMarsPhotosAPI = nasaMarsPhotosAPI;
    }

    RoverSettingsDialogFragment(@NonNull NASAMarsPhotosAPI nasaMarsPhotosAPI,
                                @NonNull String chosenRoverName) {
        this(nasaMarsPhotosAPI);
        this.chosenRoverName = chosenRoverName;
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
                .setPositiveButton(R.string.button_dialog_settings_save, (dialogInterface, i) -> listener.onSaveClick(chosenRover))
                .setNegativeButton(R.string.button_dialog_settings_cancel, (dialogInterface, i) -> listener.onCancelClick());
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadRoverList();
    }

    private void loadRoverList() {
        viewProgressRoverNames.setVisibility(View.VISIBLE);
        nasaMarsPhotosAPI.getRoverList().enqueue(this);
    }

    @WorkerThread
    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        handlerUI.post(() -> {
            Toast.makeText(getActivity(),
                    getString(R.string.error_network_failure),
                    Toast.LENGTH_LONG).show();
            listener.onCancelClick();
        });
    }

    @WorkerThread
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String responseBodyString = responseBody.string();
            roverList = NASAMarsPhotosAPI.GSON
                    .fromJson(responseBodyString, RoverListResponse.class)
                    .getRovers();
            if (roverList != null) {
                handlerUI.post(() -> {
                    viewProgressRoverNames.setVisibility(View.GONE);
                    inflateRoverNames(roverList);
                });
            }
        }
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
