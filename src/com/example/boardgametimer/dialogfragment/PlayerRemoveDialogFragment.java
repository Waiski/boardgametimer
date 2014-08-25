package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.R;

public class PlayerRemoveDialogFragment extends RetainedDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getResources().getString(R.string.confirm_player_remove) + " " +  player.getName() + "?";
        AlertDialog.Builder builder = makeDefaultBuilder(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ( (GameFragment) getTargetFragment() ).removePlayer(player);
            }
        });
        return builder.create();
    }
}