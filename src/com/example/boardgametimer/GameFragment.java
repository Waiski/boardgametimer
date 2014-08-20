package com.example.boardgametimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GameFragment extends Fragment {
	
	private View view;
	private Button timerButton, passButton;
    private ImageButton pauseButton;
	private TextView roundView;
    private LinearLayout pauseOverlay;
	private Game game;
	private Player currentPlayer;
    private Player interruptedPlayer;
	private PlayerArrayAdapter playersAdapter;
    private ListView playersView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) { 
        if (view != null)
            return view;
        view = inflater.inflate(R.layout.fragment_main, container, false);
		playersView = (ListView) view.findViewById(R.id.playerList);
		playersView.setAdapter(playersAdapter);
        registerForContextMenu(playersView);
		timerButton = (Button)view.findViewById(R.id.timerButton);
        passButton = (Button)view.findViewById(R.id.passButton);
        pauseButton = (ImageButton)view.findViewById(R.id.pauseButton);
        roundView = (TextView)view.findViewById(R.id.roundView);
        pauseOverlay = (LinearLayout)view.findViewById(R.id.pauseOverlay);
        
        //set listeners for buttons
        timerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                if (!game.hasPlayers())
                    return;
				if (currentPlayer != null && currentPlayer.isRunning()) {
                    if (interruptedPlayer == null)
					    currentPlayer = currentPlayer.endAction();
                    else
                        currentPlayer = currentPlayer.endAction(interruptedPlayer);
                    interruptedPlayer = null;
                    showCurrentPlayer();
				} else {
                    // Begin a new round
                    currentPlayer = game.getFirstPlayer();
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
                if (!game.hasPlayers())
                    return;
                if (interruptedPlayer == null)
                    currentPlayer = currentPlayer.passTurn();
                else
                    currentPlayer = currentPlayer.passTurn(interruptedPlayer);
                interruptedPlayer = null;
                showCurrentPlayer();
				if (game.isOnBreak()) {
                    // End round
                    currentPlayer = null;
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

    /**
     * Scrolls the list to the position of the current player
     */
    public void showCurrentPlayer() {
        if (currentPlayer != null)
            playersView.smoothScrollToPosition(playersAdapter.getPosition(currentPlayer));
    }
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
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
    }
    
    public void setTime(int hours, int minutes) {
    	long timeInMillis = 60*60*1000*hours + 60*1000*minutes;
    	game.setTime(timeInMillis);
        // Resume the game if it was previously paused by something other than the pause button
        if (!game.isOnBreak() && game.isPaused() && pauseOverlay.getVisibility() != View.VISIBLE)
            game.resume();
    }

    public void activatePlayer(Player newActivePlayer) {
        if (!game.isOnBreak() && newActivePlayer != currentPlayer) {
            interruptedPlayer = currentPlayer.interrupt();
            currentPlayer = newActivePlayer.resume();
        }
    }

    /**
     * Removes all traces of the player, so that the object is eligible for garbage collection
     * @param player
     */
    public void removePlayer(Player player) {
        if (player == interruptedPlayer)
            interruptedPlayer = player.getNext();
        // If removing the running (current) player, pass so that the game can resolve it's state properly
        // This also unsets currentPlayer if this was the last player, and the game is put on break
        if (player.isRunning())
            passButton.performClick();

        // Removing player from the game removes it from the adapter too, as they reference the same ArrayList<Player>
        game.removePlayer(player);
        // Notify the adapter to update the view
        playersAdapter.notifyDataSetChanged();
    }

    public void removePlayerDialog(Player player) {
        if (!this.game.isOnBreak())
            this.game.pause();
        PlayerRemoveDialogFragment dialog = new PlayerRemoveDialogFragment();
        dialog.setPlayer(player);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "remove_player_fragment");
    }

    public void changeTimeDialogForPlayer(Player player) {
        if (!this.game.isOnBreak())
            this.game.pause();
        PlayerTimeAdjustDialogFragment dialog = new PlayerTimeAdjustDialogFragment();
        dialog.setPlayer(player);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "adjust_time_fragment");
    }

    public void onDismissDialog(DialogInterface dialog) {
        if (!game.isOnBreak() && game.isPaused() && pauseOverlay.getVisibility() != View.VISIBLE)
            game.resume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mInflater = getActivity().getMenuInflater();
        mInflater.inflate(R.menu.player_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Player selectedPlayer = playersAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.set_player_active:
                activatePlayer(selectedPlayer);
                return true;
            case R.id.add_time:
                changeTimeDialogForPlayer(selectedPlayer);
                return true;
            case R.id.remove_player:
                removePlayerDialog(selectedPlayer);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	super.onCreateOptionsMenu(menu, inflater);
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
        df.setTargetFragment(this, 0);
        df.show(getFragmentManager(), "dialog");
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
