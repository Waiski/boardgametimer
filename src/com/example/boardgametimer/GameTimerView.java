package com.example.boardgametimer;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameTimerView extends RelativeLayout {

	private TextView nameView;
	private TextView timerView;
	public GameTimerView(Context context) {
		super(context);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.timer_view, this);
		
		loadViews();
	}

	public GameTimerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.timer_view, this);
		
		loadViews();
	}

	public GameTimerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.timer_view, this);
		
		loadViews();
	}
	
	private void loadViews() {
		nameView = (TextView)findViewById(R.id.nameView);
		timerView = (TextView)findViewById(R.id.timerView);
	}
	
	public void setTime(long timeInMillis) {
		timerView.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis))
        ));
	}
	
	public void setName(String name) {
		nameView.setText(name);
	}
	
	public void setActive() {
		this.nameView.setTextColor(Color.GREEN);
	}
	
	public void setInactive() {
		nameView.setTextColor(Color.BLACK);
	}
	
	public void setPassed() {
		nameView.setTextColor(Color.LTGRAY);
	}

}
