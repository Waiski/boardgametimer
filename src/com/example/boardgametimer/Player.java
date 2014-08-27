package com.example.boardgametimer;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


public class Player {
    private String name;
    private long totalCountDown;
    private long countDownInterval;
    private GameTimerView timerView;
    
    private boolean isRunning;
    private boolean hasPassed;
    private boolean isOutOfTime;
    private static final String TAG = "Player";
    private long timeUsedTotal;
    private long turnStartTime;
    private long timeAdjustment;
    private long timeUsedThisTurn;
    
    public Player(String name, long totalCountDown, long countDownInterval) {
        this.name = name;
        this.totalCountDown = totalCountDown;
        this.countDownInterval = countDownInterval;
        totalReset();
    }

    public Player totalReset() {
        this.timeUsedTotal = 0;
        this.timeAdjustment = 0;
        this.timeUsedThisTurn = 0;
        this.timerView = null;
        this.isRunning = false;
        this.hasPassed = false;
        handler.removeMessages(MSG);
        return this;
    }

    public void setTimerView(GameTimerView timer) {
        this.timerView = timer;
        this.timerView.setName(this.name);
        updateTimer();
        this.timerView.setInactive();
    }

    public GameTimerView getTimerView() {
        return this.timerView;
    }
    
    public void setActive()
    {
        timerView.setActive();
    }

    public void setInactive() {
        timerView.setInactive();
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
    
    /**
     * This is called in the end of the round.
     * @return Player
     */
    public Player reset() {
        this.hasPassed = false;
        this.timerView.setInactive();
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

    public final Player setPassed() {
        hasPassed = true;
        timerView.setPassed();
        return this;
    }

    public final Player interrupt() {
        if (hasPassed())
            timerView.setPassed();
        else
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
