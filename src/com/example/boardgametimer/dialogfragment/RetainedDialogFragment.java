package com.example.boardgametimer.dialogfragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.boardgametimer.GameFragment;
import com.example.boardgametimer.Player;
import com.example.boardgametimer.R;

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
        if (getTargetFragment() != null)
            ( (GameFragment) getTargetFragment() ).onDismissDialog(dialog);
    }

    protected AlertDialog.Builder makeDefaultBuilder(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder;
    }

    protected AlertDialog.Builder makeDefaultBuilder(int messageId) {
        return makeDefaultBuilder(getResources().getString(messageId));
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
