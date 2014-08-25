package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.Player;
import com.example.boardgametimer.R;

public class PlayerTimeAdjustDialogFragment extends RetainedDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getResources().getString(R.string.adjust_player_time) + " " +  player.getName();
        AlertDialog.Builder builder = makeDefaultBuilder(message);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.player_time_dialog, null);
        builder.setView(v);

        final NumberPicker minutePicker = (NumberPicker)v.findViewById(R.id.minutePicker);
        minutePicker.setValue(0);
        minutePicker.setMaxValue(60);
        final NumberPicker secondPicker = (NumberPicker)v.findViewById(R.id.secondPicker);
        secondPicker.setValue(0);
        secondPicker.setMaxValue(59);
        final ToggleButton plusOrMinus = (ToggleButton)v.findViewById(R.id.plusOrMinus);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                long adjustTime = minutePicker.getValue()*60*1000 + secondPicker.getValue()*1000;
                if (!plusOrMinus.isChecked())
                    adjustTime = adjustTime * -1;
                player.adjustTime(adjustTime);
            }
        });
        return builder.create();
    }
}
