package com.example.boardgametimer;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PlayerArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public PlayerArrayAdapter(Context context, ArrayList<Player> values) {
        super(context, R.layout.timer_view, values);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameTimerView playerView = getItem(position).getTimerView();
        if (playerView != null)
            return playerView;
        playerView = new GameTimerView(context);
        getItem(position).setTimerView(playerView);
        return playerView;
    }
}
