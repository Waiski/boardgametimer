package com.example.boardgametimer;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


//TODO: implement end-of-time logic

public class Player {
	private String name;
	private long totalCountDown;
	private long countDownInterval;
	private GameTimerView timerView;
	private Player next;
	private Game game;
	
	private boolean isRunning;
	private boolean hasPassed;
    private boolean isOutOfTime;
	private static final String TAG = "Player";
    private long timeUsedTotal;
    private long turnStartTime;
    private long timeAdjustment;
    private long timeUsedThisTurn;
	
	public Player(String name, long totalCountDown, long countDownInterval, Game game) {
		this.name = name;
		this.totalCountDown = totalCountDown;
		this.countDownInterval = countDownInterval;
        this.timeUsedTotal = 0;
        this.timeAdjustment = 0;
        this.timeUsedThisTurn = 0;
		this.game = game;
		
		this.isRunning = false;
		this.hasPassed = false;
	}

    public void setTimerView(GameTimerView timer) {
        this.timerView = timer;
        this.timerView.setName(this.name);
        updateTimer();
        this.timerView.setInactive();
    }
	
	public void setActive()
	{
		this.timerView.setActive();
	}
	
	public Player setNext(Player next) {
		this.next = next;
		return this;
	}
	
	public Player setTime(long timeInMillis) {
		this.totalCountDown = timeInMillis;
        updateTimer();
		return this;
	}

    public Player adjustTime(long timeInMillis) {
        this.timeAdjustment += timeInMillis;
        updateTimer();
        return this;
    }
	
	public Player endAction() {
		return endAction(this.next);
	}

    public Player endAction(Player nextPlayer) {
        this.pause();
        //only set as inactive if not passed
        if (!this.hasPassed)
            this.timerView.setInactive();
        else
            this.timerView.setPassed();
        //if the next player has already passed, try the next one
        if (nextPlayer.hasPassed)
            return nextPlayer.endAction();

        nextPlayer.resume();
        return nextPlayer;
    }
	
	public Player passTurn() {
		return passTurn(this.next);
	}

    public Player passTurn(Player nextPlayer) {
        boolean lastPass = this.game.resolvePass(this);
        if(!lastPass) {
            this.hasPassed = true;
            this.timerView.setPassed();
            return this.endAction(nextPlayer);
        }
        else {
            this.game.nextRound();
            return this.game.getFirstPlayer();
        }
    }
	
	/**
	 * This is called in the end of the round.
	 * @return Player
	 */
	public Player reset() {
		this.hasPassed = false;
		this.timerView.setInactive();
		this.pause();
		return this;
	}
	
	public boolean hasPassed() {
		return hasPassed;
	}
	
	public final synchronized Player resume() {
		this.isRunning = true;
		this.timerView.setActive();
        turnStartTime = SystemClock.elapsedRealtime();
		Log.i(TAG, this.name +" resuming from " + millisLeft());
		handler.sendMessage(handler.obtainMessage(MSG));
		return this;
	}
	
	public final synchronized Player pause() {
        if (isRunning) {
            timeUsedTotal += SystemClock.elapsedRealtime() - turnStartTime;
            timeUsedThisTurn = 0;
            Log.i(TAG, name + " paused at " + millisLeft());
            isRunning = false;
        }
        handler.removeMessages(MSG);
        return this;
	}

    public final synchronized Player interrupt() {
        timerView.setInactive();
        return pause();
    }
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void onTick()
	{
        updateTimer();
	}

    public long millisLeft() {
        return totalCountDown + timeAdjustment - timeUsedTotal - timeUsedThisTurn;
    }

    public void updateTimer() {
        checkOutOfTime();
        this.timerView.setTime(millisLeft());
    }

    public void checkOutOfTime() {
        if (millisLeft() <= 0 && !isOutOfTime) {
            this.timerView.setOutOfTime();
            isOutOfTime = true;
            return;
        }
        if (millisLeft() > 0 && isOutOfTime) {
            this.timerView.setNotOutOfTime();
            isOutOfTime = false;
        }
        return;
    }
	
	private static final int MSG = 1;
	
	private static class TimerHandler extends Handler {
		private final WeakReference<Player> player;
		
		public TimerHandler(Player player) {
			this.player = new WeakReference<Player>(player);
		}
		@Override
		public synchronized void handleMessage(Message message) {
			Player player = this.player.get();
            player.timeUsedThisTurn = SystemClock.elapsedRealtime() - player.turnStartTime;
            long lastTickStart = SystemClock.elapsedRealtime();
            player.onTick();
            // Take onTick execution time into account
            long delay = lastTickStart + player.countDownInterval - SystemClock.elapsedRealtime();
            // If onTick has lasted longer than countDownInterval
            while (delay < 0) delay += player.countDownInterval;
            sendMessageDelayed(obtainMessage(MSG), delay);
		}
	}
	
	private Handler handler = new TimerHandler(this);

	public String getName() {
		return this.name;
	}
	
}
