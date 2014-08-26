package com.example.boardgametimer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.util.Log;

public class Game {
	private static final String TAG = "Game";
	private static final int COUNTDOWN_INTERVAL=100;
	private static final int FIRST_ROUND=1;
	private ArrayList<Player> players;
	private long time;
	private int round;
	private boolean onBreak;
    private boolean paused;
    public static TurnHandler turns;

	public Game(long timeInMillis) {
		this.time = timeInMillis;
        this.players = new ArrayList<Player>();
        turns = new TurnHandler(this);
		this.reset();
	}

    /**
     * Resets everything to initial state (except game settings such as player list and game time)
     * @return this game
     */
    public Game reset() {
        round = FIRST_ROUND;
        onBreak = true;
        paused = false;
        for (Player player : players)
            player.totalReset();
        turns.reset();
        return this;
    }
	
	public Player addPlayer(String name) {
		Player player = new Player(name, this.time, COUNTDOWN_INTERVAL);
		players.add(player);
		return player;
	}

    public Game removePlayer(Player player) {
        turns.detach(player);
        return this;
    }
	
	public Game resume() {
        turns.resume();
        paused = false;
		return this;
	}

    public Game pause() {
        turns.pause();
        paused = true;
        return this;
    }
	
	public void nextRound() {
		this.round++;
		for(Player player : this.players)
			player.reset();
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

    public static class TurnHandler  {

        private WeakReference<Game> game;
        private Player currentPlayer;
        private Player interruptedPlayer;
        private ArrayList<Player> passingOrder;
        public TurnHandler(Game game) {
            this.game = new WeakReference<Game>(game);
            this.reset();
        }

        public void reset() {
            currentPlayer = null;
            interruptedPlayer = null;
            if (passingOrder != null)
                passingOrder.clear();
            else
                passingOrder = new ArrayList<Player>();
        }

        public void next() {
            Game theGame = game.get();
            if (!theGame.hasPlayers())
                return;
            if (!theGame.isOnBreak()) {
                // Always calling pass() on already-passed players makes sure the game is put on break if necessary
                if (currentPlayer.hasPassed()) {
                    pass();
                    return;
                }
                currentPlayer.pause().setInactive();
            }
            currentPlayer = findNextPlayer().resume();
            // Passing order must be cleared only after the next player has been found
            if (theGame.isOnBreak()) {
                passingOrder.clear();
                theGame.onBreak = false;
            }
            interruptedPlayer = null;
        }

        public void pass() {
            Game theGame = game.get();
            if (!theGame.hasPlayers() || theGame.isOnBreak())
                return;
            currentPlayer.setPassed().pause();
            if (passingOrder.indexOf(currentPlayer) == -1)
                passingOrder.add(currentPlayer);
            if (passingOrder.size() == theGame.players.size()) {
                Log.i(TAG, "Last passer: " + currentPlayer.getName() + "\nThe round " + theGame.round + " ends.");
                currentPlayer = null;
                theGame.nextRound();
                passingOrder.get(0).setActive();
            } else {
                if (passingOrder.size() == 1)
                    Log.i(TAG, "First passer: " + currentPlayer.getName());
                // Set next player as current, but don't resume the game if it's paused
                currentPlayer = findNextPlayer();
                if (!theGame.isPaused())
                    currentPlayer.resume();
            }
            interruptedPlayer = null;
        }

        public void detach(Player player) {
            // Passing inevitably removes the player from being current
            if (player == currentPlayer)
                pass();
            // If removing the first passer, who isn't the only player, during a break
            if (game.get().isOnBreak() && passingOrder.indexOf(player) == 0 && passingOrder.size() > 1)
                passingOrder.get(1).setActive();
            passingOrder.remove(player);
            // This removes the player from the adapter too, as they reference the same ArrayList
            game.get().players.remove(player);
            // Since it's been removed from players list, findNextNonPassed cannot find this player
            if (player == interruptedPlayer)
                interruptedPlayer = findNextNonPassedFrom(interruptedPlayer);
        }

        public void jumpTo(Player player) {
            if (game.get().isOnBreak() || player == currentPlayer)
                return;
            // If some player was already interrupted, the turn will return to him, otherwise:
            if (interruptedPlayer == null)
                interruptedPlayer = currentPlayer;
            currentPlayer.interrupt();
            currentPlayer = player.resume();
        }

        public void pause() {
            if (!game.get().isOnBreak())
                currentPlayer.pause();
        }

        public void resume() {
            if (game.get().isOnBreak())
                next();
            else if (game.get().isPaused())
                currentPlayer.resume();
        }

        public Player getActivePlayer() {
            if (!game.get().isOnBreak())
                return currentPlayer;
            return getRoundStarter();
        }

        private Player findNextPlayer() {
            // This function only finds the next player, never sets any field values.
            Game theGame = game.get();
            if (!theGame.hasPlayers())
                return null;
            if (interruptedPlayer != null)
                return interruptedPlayer;
            if (theGame.isOnBreak())
                return getRoundStarter();
            return findNextNonPassedFrom(currentPlayer);
        }

        private Player findNextNonPassedFrom(Player player) {
            // Loop through players to find the next non-passed one
            ArrayList<Player> thePlayers = game.get().players;
            int startPos = thePlayers.indexOf(player);
            int size = thePlayers.size();
            int pointer;
            Player nextPlayer;
            for (int i = startPos + 1; i <= size + startPos; i++) {
                pointer = i % size;
                nextPlayer = thePlayers.get(pointer);
                if (!nextPlayer.hasPassed())
                    return nextPlayer;
            }
            return null;
        }

        private Player getRoundStarter() {
            if (passingOrder.isEmpty())
                return game.get().players.get(0);
            return passingOrder.get(0);
        }
    }
}
