package com.example.boardgametimer;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PlayerArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    final int INVALID_ID = -1;
    
    HashMap<Player, Integer> idMap = new HashMap<Player, Integer>();
    
    public PlayerArrayAdapter(Context context, ArrayList<Player> values) {
        super(context, R.layout.timer_view, values);
        this.context = context;
        for (int i = 0; i < values.size(); i++) {
            idMap.put(values.get(i), i);
        }
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
    
    //Really ugly hack that exposes internal functionality via API.
    //should figure out a more sensible way to update the id map when adding objects to the array
    public void addToMap(Player player) {
        int maximumId=0;
        if(!idMap.isEmpty())
            maximumId = Collections.max(idMap.values());
        
        idMap.put(player, maximumId+1);
    }
    
    public void removeFromMap(Player player) {
        idMap.remove(player);
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
