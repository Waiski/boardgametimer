package com.example.boardgametimer;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class PlayerArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    final int INVALID_ID = -1;
    
    HashMap<Player, Integer> idMap = new HashMap<Player, Integer>();
    ArrayList<Player> players;
    
    public PlayerArrayAdapter(Context context, ArrayList<Player> values) {
        super(context, R.layout.timer_view, values);
        this.context = context;
        this.players = values;
        buildIdMap();
    }
    
    private void buildIdMap() {
        for (int i = 0; i < players.size(); i++) {
            idMap.put(players.get(i), i);
        }
    }
    
    private void refreshIdMap() {
        //add any new players to idMap
        for(Player player : players) {
            if(!idMap.containsKey(player))
                addToMap(player);
        }
        //remove any entries to deleted players
        Iterator<Player> it = idMap.keySet().iterator();
        while (it.hasNext()) {
            Player player = it.next();
            if(!players.contains(player))
                it.remove();
        }
    }
    
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        refreshIdMap();
    }
    
    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= idMap.size())
            return INVALID_ID;
        Player item = getItem(position);
        return idMap.get(item);
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    private void addToMap(Player player) {
        int maximumId=0;
        if(!idMap.isEmpty())
            maximumId = Collections.max(idMap.values());
        
        idMap.put(player, maximumId+1);
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
