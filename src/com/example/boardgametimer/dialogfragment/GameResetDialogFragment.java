package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.R;

public class GameResetDialogFragment extends RetainedDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = makeDefaultBuilder(R.string.confirm_reset);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ( (GameFragment) getTargetFragment() ).resetGame();
            }
        });
        return builder.create();
    }
}