package com.example.boardgametimer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
	private Button timerButton, passButton;
    private ImageButton pauseButton;
	private TextView roundView;
    private LinearLayout pauseOverlay;
	private Game game;
	private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        game = new Game(60*60*1000);

        game.addPlayer("Matti V.");
        game.addPlayer("Ilari A.");
        game.addPlayer("Kristian S.");
        game.addPlayer("Valtter V.");

        PlayerArrayAdapter playersAdapter = new PlayerArrayAdapter(
                getApplicationContext(),
                game.getPlayers()
        );

        ListView playersView = (ListView) findViewById(R.id.playerList);
        playersView.setAdapter(playersAdapter);

        currentPlayer = game.getFirstPlayer();
        game.start();
        timerButton = (Button)findViewById(R.id.timerButton);
        passButton = (Button)findViewById(R.id.passButton);
        pauseButton = (ImageButton)findViewById(R.id.pauseButton);
        roundView = (TextView)findViewById(R.id.roundView);
        pauseOverlay = (LinearLayout)findViewById(R.id.pauseOverlay);

        timerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentPlayer.isRunning()) {
					currentPlayer = currentPlayer.endAction();
				}
				else {
					timerButton.setText("Next");
					roundView.setText(getResources().getString(R.string.roundNo) + " " + game.getRound());
					passButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
					game.resume();
				}
			}
		});
        passButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				currentPlayer = currentPlayer.passTurn();
				if (game.isOnBreak()) {
                    roundView.append(" ended");
					timerButton.setText("Start round "+game.getRound());
					passButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
				}
			}
		});
        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!game.isOnBreak() && !game.isPaused()) {
                    game.pause();
                    pauseOverlay.setVisibility(View.VISIBLE);
                }
            }
        });
        pauseOverlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pauseOverlay.setVisibility(View.INVISIBLE);
                if (!game.isOnBreak())
                    game.resume();
            }
        });
    }
    
    public void setTime(int hours, int minutes) {
    	long timeInMillis = 60*60*1000*hours + 60*1000*minutes;
    	game.setTime(timeInMillis);
        // Resume the game if it was previously paused by something other than the pause button
        if (!game.isOnBreak() && game.isPaused() && pauseOverlay.getVisibility() != View.VISIBLE)
            game.resume();
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
        if (!this.game.isOnBreak())
            this.game.pause();
        df.show(getFragmentManager(), "dialog");
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
