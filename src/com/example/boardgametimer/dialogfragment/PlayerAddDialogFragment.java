package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.WindowManager.LayoutParams;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.R;

public class PlayerAddDialogFragment extends RetainedDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = makeDefaultBuilder(R.string.add_player);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add, null);
        builder.setView(v);
        
        final EditText nameField = (EditText)v.findViewById(R.id.nameField);
        
        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!nameField.getText().toString().matches(""))
                    ( (GameFragment) getTargetFragment() ).addPlayer(nameField.getText().toString());
            }
        });
        builder.setNeutralButton(R.string.next, null);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        // Override button click handler on dialog show to disable dialog dismissal when selecting "Next"
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface doNotOverrideDialogVariable) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (!nameField.getText().toString().matches(""))
                            ( (GameFragment) getTargetFragment() ).addPlayer(nameField.getText().toString());
                        nameField.setText(null);
                    }
                });
            }
        });
        return dialog;
    }
}
