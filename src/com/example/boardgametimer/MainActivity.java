package com.example.boardgametimer;

import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;

import com.example.boardgametimer.dialogfragment.ConfirmExitDialogFragment;


public class MainActivity extends Activity {
    
    private GameFragment fragment;
    private static final String TAG_GAME_FRAGMENT="game_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        FragmentManager fm = getFragmentManager();
        fragment = (GameFragment)fm.findFragmentByTag(TAG_GAME_FRAGMENT);
        if (fragment == null) {
            fragment = new GameFragment();
            fm.beginTransaction().add(R.id.fragment_container, (Fragment)fragment, TAG_GAME_FRAGMENT).commit();
        }
    }

    @Override
    public void onBackPressed() {
        ConfirmExitDialogFragment dialog = new ConfirmExitDialogFragment();
        dialog.show(getFragmentManager(), "confirm_exit_df");
    }
}
