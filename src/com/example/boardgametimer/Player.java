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
	private boolean isPaused;
	private boolean hasPassed;
	private static final String TAG = "Player";
	private long stopTimeInFuture;
	private long timeLeftWhenPaused;
    private long timeUsedTotal;
    private long turnStartTime;
	
	public Player(String name, long totalCountDown, long countDownInterval, Game game) {
		this.name = name;
		this.totalCountDown = totalCountDown;
		this.timeLeftWhenPaused = totalCountDown;
		this.countDownInterval = countDownInterval;
        this.timeUsedTotal = 0;
		this.game = game;
		
		this.isRunning = false;
		this.isPaused = false;
		this.hasPassed = false;
	}

    public void setTimerView(GameTimerView timer) {
        this.timerView = timer;
        this.timerView.setName(this.name);
        this.timerView.setTime(totalCountDown);
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
		this.totalCountDown = timeInMillis - this.timeUsedTotal;
		this.timeLeftWhenPaused = timeInMillis - this.timeUsedTotal;
		this.timerView.setTime(timeInMillis - this.timeUsedTotal);
		return this;
	}
	
	public Player endAction() {
		this.pause();
		//only set as inactive if not passed
		if(!this.hasPassed)
			this.timerView.setInactive();
		//if the next player has already passed, try the next one
		if (this.next.hasPassed)
			return this.next.endAction();
			
		if (this.next.isPaused()) {
			this.next.resume();
		}
		else 
			this.next.resume();
		return this.next;
	}
	
	public Player passTurn() {
		boolean lastPass = this.game.resolvePass(this);
		if(!lastPass) {
			this.hasPassed = true;
			this.timerView.setPassed();
			return this.endAction();
		}
		else {
			this.game.nextRound();
			return this.game.getFirstPlayer();
		}
	}
	
	/**
	 * This is called in the end of the round.
	 * @return
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

	public final synchronized Player start() {
		this.isRunning = true;
		this.isPaused = false;
		this.timerView.setActive();
		stopTimeInFuture = SystemClock.elapsedRealtime() + totalCountDown;
        turnStartTime = SystemClock.elapsedRealtime();
		Log.i(TAG, this.name + " starting from " + totalCountDown);
		handler.sendMessage(handler.obtainMessage(MSG));
		return this;
	}
	
	public final synchronized Player resume() {
		this.isRunning = true;
		this.isPaused = false;
		this.timerView.setActive();
		stopTimeInFuture = SystemClock.elapsedRealtime() + timeLeftWhenPaused;
        turnStartTime = SystemClock.elapsedRealtime();
		Log.i(TAG, this.name +" resuming from " + timeLeftWhenPaused);
		handler.sendMessage(handler.obtainMessage(MSG));
		return this;
	}
	
	public final synchronized Player pause() {
		this.isRunning = false;
		this.isPaused = true;
        return this;
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void onTick(long millisUntilFinished)
	{
		this.timerView.setTime(millisUntilFinished);
	}
	
	public void onFinish() {
		this.timerView.setTime(0);
		this.isRunning = false;
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
			final long millisLeft = player.stopTimeInFuture - SystemClock.elapsedRealtime();
            long timeUsedThisTurn = SystemClock.elapsedRealtime() - player.turnStartTime;
			if(player.isPaused()) {
				player.timeLeftWhenPaused = millisLeft;
                player.timeUsedTotal += timeUsedThisTurn;
				Log.i(TAG, player.getName() + " paused at " + player.timeLeftWhenPaused);
				return;
			}
			if (millisLeft <= 0) {
				player.onFinish();
			}
			else if (millisLeft < player.countDownInterval)
				sendMessageDelayed(obtainMessage(MSG), millisLeft);
			else {
				long lastTickStart = SystemClock.elapsedRealtime();
				player.onTick(millisLeft);
				
				long delay = lastTickStart + player.countDownInterval - SystemClock.elapsedRealtime();
				while (delay < 0) delay += player.countDownInterval;
				sendMessageDelayed(obtainMessage(MSG), delay);
			}
		}
	}
	
	private Handler handler = new TimerHandler(this);

	public String getName() {
		return this.name;
	}
	
}
