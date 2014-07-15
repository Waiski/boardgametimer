package com.example.boardgametimer;

import java.util.ArrayList;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class Game {
	private static final String TAG = "Game";
	private static final int COUNTDOWN_INTERVAL=100;
	private static final int FIRST_ROUND=1;
	private ArrayList<Player> players;
	private Player firstPlayer;
	private Player lastAdded;
	private long time;
	private int round;
	private boolean onBreak;
	
	public Game(long timeInMillis) {
		this.time = timeInMillis;
		this.players = new ArrayList<Player>();
		this.lastAdded = null;
		this.firstPlayer = null;
		this.round = FIRST_ROUND;
		this.onBreak = true;
	}
	
	public Game addPlayer(String name, Context context, LinearLayout root)
	{
		GameTimerView timerView = new GameTimerView(context);
		root.addView(timerView);
		Player player = new Player(name, this.time, COUNTDOWN_INTERVAL, timerView, this);
		//set the first added player as the first player
		if(players.isEmpty())
			this.setFirstPlayer(player);
		players.add(player);
		if(this.lastAdded!=null){
			this.lastAdded.setNext(player);
		}
		this.lastAdded = player;
		return this;
	}
	
	public Game start() {
		this.lastAdded.setNext(this.firstPlayer);
		this.onBreak = false;
		return this;
	}
	
	public Game resume() {
		this.onBreak = false;
		return this;
	}
	
	public void nextRound() {
		this.round++;
		for(Player player : this.players)
			player.reset();
		this.firstPlayer.setActive();
		this.onBreak = true;
	}
	
	public boolean isOnBreak() {
		return this.onBreak;
	}
	
	public void setFirstPlayer(Player player) {
		player.setFirst();
		this.firstPlayer = player;
	}
	
	public Player getFirstPlayer() {
		return this.firstPlayer;
	}
	
	public int getRound() {
		return this.round;
	}
	
	public void setTime(long timeInMillis) {
		this.time = timeInMillis;
		for(Player player : this.players)
			player.setTime(timeInMillis);
	}
	
	/**
	 * This method is called anytime a player passes, before the player
	 * is marked as passed.
	 * The first player to pass becomes the new first player.
	 * @param player
	 * @return true if the player was last to pass.
	 */
	public boolean resolvePass(Player player) {
		boolean wasFirst = true;
		boolean wasLast = true;
		//check for first pass condition
		for(Player p : this.players) {
			if(p.hasPassed()) {
				wasFirst = false;
				break;
			}
		}
		//check for last pass condition
		for(Player p : this.players) {
			if(!p.hasPassed() && p != player) {
				wasLast = false;
				break;
			}
		}
		if(wasFirst){
			Log.i(TAG, "First passer: " + player.getName());
			this.setFirstPlayer(player);
		}
			
		if(wasLast) {
			Log.i(TAG, "Last passer: " + player.getName() + "\nThe round " + this.round + " ends.");
		}
		return wasLast;
	}

}
