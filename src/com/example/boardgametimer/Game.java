package com.example.boardgametimer;

import java.util.ArrayList;
import android.util.Log;

public class Game {
	private static final String TAG = "Game";
	private static final int COUNTDOWN_INTERVAL=100;
	private static final int FIRST_ROUND=1;
	private ArrayList<Player> players;
    private ArrayList<Player> passingOrder;
	private Player pausedPlayer;
	private long time;
	private int round;
	private boolean onBreak;
    private boolean paused;

	public Game(long timeInMillis) {
		this.time = timeInMillis;
        this.players = new ArrayList<Player>();
		reset();
	}

    /**
     * Resets everything to initial state (except game settings such as player list and game time)
     * @return this game
     */
    public Game reset() {
        this.pausedPlayer = null;
        this.round = FIRST_ROUND;
        this.onBreak = true;
        this.paused = false;
        this.passingOrder = new ArrayList<Player>();
        return this;
    }
	
	public Game addPlayer(String name)
	{
		Player player = new Player(name, this.time, COUNTDOWN_INTERVAL, this);
		players.add(player);
		updateNextChain();
		return this;
	}

    /**
     *
     * @param player
     * @return this game
     */
    public Game removePlayer(Player player) {
        passingOrder.remove(player);
        // This removes the player from the adapter too
        players.remove(player);
        // If this was the only player, reset this game
        if (players.isEmpty())
            reset();
        updateNextChain();
        return this;
    }

    /**
     * Reset next player for each player in the game
     * @return this game
     */
    public Game updateNextChain() {
        if (!players.isEmpty()) {
            // Loop everything but the last one
            for (int position = 0; position < players.size() - 1; position++)
                players.get(position).setNext(players.get(position + 1));
            // Set the first one as next for the last one
            players.get(players.size() - 1).setNext(players.get(0));
        }
        return this;
    }
	
	public Game resume() {
        if (players.isEmpty())
            return this;
		this.onBreak = false;
        if (this.paused)
            this.pausedPlayer.resume();
        else {
            this.getFirstPlayer().resume();
            // Reset the passing order on new round start
            // Note: this must be done _after_ getFirstPlayer().resume(), since it relies on the passing order
            this.passingOrder.clear();
        }
        this.paused = false;
		return this;
	}

    public Game pause() {
        this.paused = true;
        for(Player player : this.players) {
            if (player.isRunning()) {
                this.pausedPlayer = player.pause();
                break;
            }
        }
        return this;
    }
	
	public void nextRound() {
		this.round++;
		for(Player player : this.players)
			player.reset();
		this.getFirstPlayer().setActive();
		this.onBreak = true;
	}
	
	public boolean isOnBreak() {
		return this.onBreak;
	}

    public boolean isPaused() {
        return this.paused;
    }

    public boolean hasPlayers() {
        return !this.players.isEmpty();
    }
	
	public Player getFirstPlayer() {
        if (players.isEmpty())
            return null;
        if (passingOrder.isEmpty())
            return players.get(0);
        return passingOrder.get(0);
	}
	
	public int getRound() {
		return this.round;
	}

    public ArrayList<Player> getPlayers() {
        return this.players;
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
		boolean wasLast = false;

        if (passingOrder.isEmpty())
            Log.i(TAG, "First passer: " + player.getName());
        if (passingOrder.size() == players.size() - 1)
            wasLast = true;

        // Make sure that the same player won't be added to the passer list twice
        if (passingOrder.indexOf(player) == -1)
            passingOrder.add(player);

		if (wasLast)
			Log.i(TAG, "Last passer: " + player.getName() + "\nThe round " + this.round + " ends.");

		return wasLast;
	}

}
