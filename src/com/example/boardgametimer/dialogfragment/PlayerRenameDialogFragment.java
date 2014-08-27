package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.R;

public class PlayerRenameDialogFragment extends RetainedDialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //reuse the add dialog layout since it's basically the same
        View v = inflater.inflate(R.layout.dialog_add, null);
        final EditText nameField = (EditText)v.findViewById(R.id.nameField);
        
        AlertDialog.Builder builder = makeDefaultBuilder(getResources().getString(R.string.rename_player) + " " + player.getName());
        builder.setView(v);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
               ( (GameFragment) getTargetFragment() ).renamePlayer(player, nameField.getText().toString());
            }
        });
        
        Dialog dialog = builder.create();
        //dialog.set(getResources().getString(R.string.rename_player) + " " + player.getName());
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

}
