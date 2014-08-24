package com.example.boardgametimer.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.R;

public class TimeSelectorDialogFragment extends RetainedDialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = makeDefaultBuilder(R.string.select_game_time_per_player);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_time, null);
		builder.setView(v);
		
		final NumberPicker hourPicker = (NumberPicker)v.findViewById(R.id.hourPicker);
		hourPicker.setValue(0);
		hourPicker.setMaxValue(5);
		final NumberPicker minutePicker = (NumberPicker)v.findViewById(R.id.minutePicker);
		minutePicker.setValue(55);
		minutePicker.setMaxValue(59);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
                ( (GameFragment) getTargetFragment() ).setTime(hourPicker.getValue(), minutePicker.getValue());
			}
		});
		return builder.create();
	}
}
