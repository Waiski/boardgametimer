package com.example.boardgametimer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends FragmentActivity {
	
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

    


}
