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
               ( (GameFragment) getTargetFragment() ).addPlayer(nameField.getText().toString());
            }
        });
        builder.setNeutralButton(R.string.next, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ( (GameFragment) getTargetFragment() ).addPlayer(nameField.getText().toString());
                ( (GameFragment) getTargetFragment() ).showDialog(GameFragment.ADD_PLAYER_DIALOG_NAME, null);
                
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
