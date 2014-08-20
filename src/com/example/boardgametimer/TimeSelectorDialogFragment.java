package com.example.boardgametimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class TimeSelectorDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_time, null);
		builder.setView(v);
		
		final NumberPicker hourPicker = (NumberPicker)v.findViewById(R.id.hourPicker);
		hourPicker.setValue(0);
		hourPicker.setMaxValue(5);
		final NumberPicker minutePicker = (NumberPicker)v.findViewById(R.id.minutePicker);
		minutePicker.setValue(55);
		minutePicker.setMaxValue(59);
		
		builder.setMessage(R.string.select_game_time_per_player);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
				
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
                ( (GameFragment) getTargetFragment() ).setTime(hourPicker.getValue(), minutePicker.getValue());
			}
		});
		return builder.create();
	}

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        ( (GameFragment) getTargetFragment() ).onDismissDialog(dialog);
    }

}
