package com.example.boardgametimer;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GameFragment extends Fragment {
	private static final String TAG = "GameFragment";
	
	private Button timerButton, passButton;
    private ImageButton pauseButton;
	private TextView roundView;
    private LinearLayout pauseOverlay;
	private Game game;
	private Player currentPlayer;
	private PlayerArrayAdapter playersAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) { 
		final View view = inflater.inflate(R.layout.fragment_main, container, false);
		ListView playersView = (ListView) view.findViewById(R.id.playerList);
		playersView.setAdapter(playersAdapter);
		timerButton = (Button)view.findViewById(R.id.timerButton);
        passButton = (Button)view.findViewById(R.id.passButton);
        pauseButton = (ImageButton)view.findViewById(R.id.pauseButton);
        roundView = (TextView)view.findViewById(R.id.roundView);
        pauseOverlay = (LinearLayout)view.findViewById(R.id.pauseOverlay);
        
        if(!game.isOnBreak()) {
        	roundView.setText(getResources().getString(R.string.roundNo) + " " + game.getRound());
        	passButton.setVisibility(View.VISIBLE);
        	pauseButton.setVisibility(View.VISIBLE);
        }
        else {
        	if(game.getRound()>1) {
        		roundView.setText(getResources().getString(R.string.roundNo) + " " + 
        									(game.getRound()-1) + " " + 
        									getResources().getString(R.string.ended));
        	}
        	else {
        		roundView.setText(getResources().getString(R.string.roundNo) + " " + game.getRound());
        	}
        	timerButton.setText(getResources().getString(R.string.next));
        	passButton.setVisibility(View.INVISIBLE);
        	pauseButton.setVisibility(View.INVISIBLE);
        	timerButton.setText(getResources().getString(R.string.start_round)+" "+game.getRound());
        }
        
        //set listeners for buttons
        timerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentPlayer.isRunning()) {
					currentPlayer = currentPlayer.endAction();
				}
				else {
					timerButton.setText(getResources().getString(R.string.next));
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
                    roundView.append(" " + getResources().getString(R.string.ended));
                    timerButton.setText(getResources().getString(R.string.start_round)+" "+game.getRound());
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
		return view;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRetainInstance(true);
        game = new Game(60*60*1000);

        game.addPlayer("Matti V.");
        game.addPlayer("Ilari A.");
        game.addPlayer("Kristian S.");
        game.addPlayer("Valtter V.");
        
        playersAdapter = new PlayerArrayAdapter(
        		getActivity().getApplicationContext(),
                game.getPlayers()
        );
        
        currentPlayer = game.getFirstPlayer();
        game.start();
        
    }
    
    public void setTime(int hours, int minutes) {
    	long timeInMillis = 60*60*1000*hours + 60*1000*minutes;
    	game.setTime(timeInMillis);
        // Resume the game if it was previously paused by something other than the pause button
        if (!game.isOnBreak() && game.isPaused() && pauseOverlay.getVisibility() != View.VISIBLE)
            game.resume();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.options_menu, menu);
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
