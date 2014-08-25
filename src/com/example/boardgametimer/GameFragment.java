package com.example.boardgametimer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
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

import com.example.boardgametimer.dialogfragment.GameResetDialogFragment;
import com.example.boardgametimer.dialogfragment.PlayerAddDialogFragment;
import com.example.boardgametimer.dialogfragment.PlayerRemoveDialogFragment;
import com.example.boardgametimer.dialogfragment.PlayerTimeAdjustDialogFragment;
import com.example.boardgametimer.dialogfragment.RetainedDialogFragment;
import com.example.boardgametimer.dialogfragment.TimeSelectorDialogFragment;

public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";
	private View view;
	private Button timerButton, passButton;
    private ImageButton pauseButton;
	private TextView roundView;
    private LinearLayout pauseOverlay;
	private Game game;
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
				if (game.isOnBreak())
                    startRound();
                game.turns.next();
                showPlayer(game.turns.getActivePlayer());
			}
		});
        passButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                if (!game.hasPlayers())
                    return;
                game.turns.pass();
                showPlayer(game.turns.getActivePlayer());
				if (game.isOnBreak())
                    endRound();
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
     * Resets the game and the view to initial state while
     * keeping game settings, such as players list and game time.
     */
    public void resetGame() {
        view = null;
        game.reset();
        // Re-attaching this re-creates the view, while keeping the instance retained
        getFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    private void startRound() {
        timerButton.setText(getResources().getString(R.string.next));
        roundView.setText(getResources().getString(R.string.roundNo) + " " + game.getRound());
        passButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void endRound() {
        roundView.append(" " + getResources().getString(R.string.ended));
        timerButton.setText(getResources().getString(R.string.start_round)+" "+game.getRound());
        passButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Scrolls the list to the position of the player
     */
    public void showPlayer(Player player) {
        playersView.smoothScrollToPosition(playersAdapter.getPosition(player));
    }
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
        game = new Game(60*60*1000);
        
        playersAdapter = new PlayerArrayAdapter(
        		getActivity().getApplicationContext(),
                game.getPlayers()
        );
    }
    
    public void setTime(int hours, int minutes) {
    	long timeInMillis = 60*60*1000*hours + 60*1000*minutes;
    	game.setTime(timeInMillis);
    }
    
    public void addPlayer(String name) {
        game.addPlayer(name);
        playersAdapter.notifyDataSetChanged();
    }

    /**
     * Removes all traces of the player, so that the object is eligible for garbage collection
     * @param player
     */
    public void removePlayer(Player player) {
        Log.i(TAG, "Removing player: " + player.getName());
        boolean wasOnBreak = game.isOnBreak();
        // Removing player from the game removes it from the adapter too, as they reference the same ArrayList<Player>
        game.removePlayer(player);
        // Notify the adapter to update the view
        playersAdapter.notifyDataSetChanged();
        // Deduce changes to view states from game states
        if (!game.hasPlayers())
            resetGame();
        else if (game.isOnBreak() && !wasOnBreak)
            endRound();
    }

    public final static String REMOVE_DIALOG_NAME = "player_remove_df";
    public final static String PLAYER_TIME_ADJUST_DIALOG_NAME = "adjust_time_df";
    public final static String TIME_SELECTOR_DIALOG_NAME = "game_time_selector_df";
    public final static String GAME_RESET_DIALOG_NAME = "game_reset_df";
    public final static String ADD_PLAYER_DIALOG_NAME = "add_player_df";

    public void showDialog(String dialogName, Player player) {
        if (!game.isOnBreak())
            game.pause();
        RetainedDialogFragment dialog;
        if (dialogName.equals(REMOVE_DIALOG_NAME))
            dialog = new PlayerRemoveDialogFragment();
        else if (dialogName.equals(PLAYER_TIME_ADJUST_DIALOG_NAME))
            dialog = new PlayerTimeAdjustDialogFragment();
        else if (dialogName.equals(TIME_SELECTOR_DIALOG_NAME))
            dialog = new TimeSelectorDialogFragment();
        else if (dialogName.equals(GAME_RESET_DIALOG_NAME))
            dialog = new GameResetDialogFragment();
        else if (dialogName.equals(ADD_PLAYER_DIALOG_NAME))
            dialog = new PlayerAddDialogFragment();
        else
            return;
        if (player != null)
            dialog.setPlayer(player);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), dialogName);
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
                if (!selectedPlayer.isRunning())
                    Game.turns.jumpTo(selectedPlayer);
                return true;
            case R.id.add_time:
                showDialog(PLAYER_TIME_ADJUST_DIALOG_NAME, selectedPlayer);
                return true;
            case R.id.remove_player:
                showDialog(REMOVE_DIALOG_NAME, selectedPlayer);
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
        if (id == R.id.time)
            showDialog(TIME_SELECTOR_DIALOG_NAME, null);
        else if (id == R.id.reset)
            showDialog(GAME_RESET_DIALOG_NAME, null);
        else if (id == R.id.add_player)
            showDialog(ADD_PLAYER_DIALOG_NAME, null);
        else if (id == R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }

}
