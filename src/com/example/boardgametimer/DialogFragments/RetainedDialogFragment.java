package com.example.boardgametimer.DialogFragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.Player;

public class RetainedDialogFragment extends DialogFragment {
    protected Player player;

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
