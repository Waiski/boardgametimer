package com.example.boardgametimer;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PlayerArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    private final ArrayList<Player> players;
    /*
    * This holds a view cache so that a new view is not always created on calls to getView().
    * This is necessary to enable making changes like setActive() etc. to the timer view.
    * */
    private final ArrayList<GameTimerView> views;

    public PlayerArrayAdapter(Context context, ArrayList<Player> values) {
        super(context, R.layout.timer_view, values);
        this.context = context;
        this.players = values;
        this.views = new ArrayList<GameTimerView>(values.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameTimerView playerView;
        if (position < views.size()) {
            playerView = views.get(position);
            if (playerView != null)
                return playerView;
        } else {
            while (views.size() < position)
                views.add(null);
        }
        playerView = new GameTimerView(context);
        views.add(position, playerView);
        players.get(position).setTimerView(playerView);
        return playerView;
    }
}
