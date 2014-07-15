package com.example.boardgametimer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;



public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private Button timerButton, passButton;
	private TextView roundView;
	private Game game;
	private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout playerListView = (LinearLayout)findViewById(R.id.player_list);
        
        game = new Game(60*60*1000);
        game.addPlayer("Matti V.", getApplicationContext(), playerListView);
        game.addPlayer("Ilari A.", getApplicationContext(), playerListView);
        game.addPlayer("Kristian S.", getApplicationContext(), playerListView);
        
        currentPlayer = game.getFirstPlayer();
        game.start();
        timerButton = (Button)findViewById(R.id.timerButton);
        passButton = (Button)findViewById(R.id.passButton);
        roundView = (TextView)findViewById(R.id.roundView);

        timerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentPlayer.isRunning()) {
					currentPlayer = currentPlayer.endAction();
				}
				else {
					timerButton.setText("Next");
					roundView.setText(getResources().getString(R.string.roundNo)+" "+game.getRound());
					passButton.setVisibility(View.VISIBLE);
					game.resume();
					currentPlayer.resume();
				}
			}
		});
        passButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				currentPlayer = currentPlayer.passTurn();
				if (game.isOnBreak()) {
					timerButton.setText("Start");
					passButton.setVisibility(View.INVISIBLE);
				}
			}
		});
    }
    
    public void setTime(int hours, int minutes) {
    	long timeInMillis = 60*60*1000*hours + 60*1000*minutes;
    	game.setTime(timeInMillis);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        TimeSelectorDialogFragment df = new TimeSelectorDialogFragment();
        df.show(getFragmentManager(), "dialog");
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
