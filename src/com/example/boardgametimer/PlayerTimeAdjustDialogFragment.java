package com.example.boardgametimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

public class PlayerTimeAdjustDialogFragment extends DialogFragment {

    private Player player;

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        String message = getResources().getString(R.string.adjust_player_time) + " " +  player.getName();
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
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

    /**
     * This fixes a bug that closes the dialog on screen rotation
     * See: http://stackoverflow.com/questions/14657490/how-to-properly-retain-a-dialogfragment-through-rotation#answer-15444485
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
