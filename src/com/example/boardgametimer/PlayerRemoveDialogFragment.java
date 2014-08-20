package com.example.boardgametimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class PlayerRemoveDialogFragment  extends DialogFragment {

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
        String message = getResources().getString(R.string.confirm_player_remove) + " " +  player.getName() + "?";
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
                ( (GameFragment) getTargetFragment() ).removePlayer(player);
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

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        ( (GameFragment) getTargetFragment() ).onDismissDialog(dialog);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}