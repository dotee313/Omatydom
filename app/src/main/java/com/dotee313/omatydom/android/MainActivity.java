package com.dotee313.omatydom.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native Android front-end. It only renders state and forwards player actions. */
public class MainActivity extends Activity {

    private OmatydomGame game;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean ticking;

    private TextView statusView;
    private TextView rulerView;
    private ListView eventsView;
    private EventAdapter eventAdapter;
    private Button pauseButton;
    private Button speedButton;
    private Button controlButton;
    private Button assassinateButton;
    private Spinner decreeSpinner;
    private Spinner rulerSpinner;
    private ArrayAdapter<String> rulerAdapter;

    private List<Citizen> candidates = new ArrayList<>();
    private int lastCandidateCount = -1;

    private final Runnable tickLoop = new Runnable() {
        @Override
        public void run() {
            if (!ticking) {
                return;
            }
            game.tick();
            refreshUi();
            uiHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = OmatydomGame.create();

        statusView = findViewById(R.id.statusText);
        rulerView = findViewById(R.id.rulerText);
        eventsView = findViewById(R.id.eventsList);
        pauseButton = findViewById(R.id.pauseButton);
        speedButton = findViewById(R.id.speedButton);
        controlButton = findViewById(R.id.controlButton);
        assassinateButton = findViewById(R.id.assassinateButton);
        decreeSpinner = findViewById(R.id.decreeSpinner);
        rulerSpinner = findViewById(R.id.rulerSpinner);

        eventAdapter = new EventAdapter(this);
        eventsView.setAdapter(eventAdapter);

        ArrayAdapter<String> decreeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, decreeNames());
        decreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        decreeSpinner.setAdapter(decreeAdapter);

        rulerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>());
        rulerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rulerSpinner.setAdapter(rulerAdapter);

        pauseButton.setOnClickListener(v -> {
            game.togglePause();
            refreshUi();
        });
        speedButton.setOnClickListener(v -> {
            game.cycleSpeed();
            refreshUi();
        });
        controlButton.setOnClickListener(v -> {
            if (game.isPlayerControlled()) {
                game.releaseControl();
            } else {
                game.takeControl();
            }
            refreshUi();
        });
        assassinateButton.setOnClickListener(v -> {
            if (game.assassinateRuler()) {
                Toast.makeText(this, "The ruler has been assassinated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "There is no ruler to remove.", Toast.LENGTH_SHORT).show();
            }
            refreshUi();
        });
        findViewById(R.id.decreeButton).setOnClickListener(v -> {
            int position = decreeSpinner.getSelectedItemPosition();
            Decree[] decrees = Decree.values();
            if (position >= 0 && position < decrees.length) {
                game.issueDecree(decrees[position]);
                Toast.makeText(this, decrees[position].getDisplayName() + " issued.",
                        Toast.LENGTH_SHORT).show();
                refreshUi();
            }
        });
        findViewById(R.id.chooseButton).setOnClickListener(v -> {
            int position = rulerSpinner.getSelectedItemPosition();
            if (position >= 0 && position < candidates.size()) {
                int id = candidates.get(position).getId();
                if (game.chooseRuler(id)) {
                    Toast.makeText(this, "A new ruler has been appointed.", Toast.LENGTH_SHORT).show();
                    refreshUi();
                }
            }
        });

        refreshUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTicking();
    }

    @Override
    protected void onPause() {
        stopTicking();
        super.onPause();
    }

    private void startTicking() {
        if (!ticking) {
            ticking = true;
            uiHandler.post(tickLoop);
        }
    }

    private void stopTicking() {
        ticking = false;
        uiHandler.removeCallbacks(tickLoop);
    }

    private void refreshUi() {
        World world = game.getWorld();

        statusView.setText(String.format(Locale.getDefault(),
                "Day %d  •  Population %d  •  Stability %.0f/100%nGold %.0f  •  Food %.0f  •  Speed x%d  •  %s",
                world.getDay(), world.getPopulation(), world.getStability(),
                world.getGold(), world.getFood(), game.getSpeed(),
                game.isPaused() ? "PAUSED" : "RUNNING"));

        if (world.getRuler() == null) {
            rulerView.setText("No ruler — the island is waiting for a leader.");
        } else {
            rulerView.setText(String.format(Locale.getDefault(),
                    "Ruler: %s • approval %.0f/100 • %d days reigning • %s",
                    world.getRuler().getCitizen().getName(),
                    world.getRuler().getApproval(),
                    world.getRuler().getReignDays(world.getDay()),
                    game.isPlayerControlled() ? "under your direct control" : "acting independently"));
        }

        pauseButton.setText(game.isPaused() ? "Resume" : "Pause");
        speedButton.setText("Speed x" + game.getSpeed());
        controlButton.setText(game.isPlayerControlled() ? "Release control" : "Take control");

        eventAdapter.clear();
        for (GameEvent event : world.getRecentEvents(40)) {
            eventAdapter.add(event);
        }
        eventAdapter.notifyDataSetChanged();

        candidates = game.getRulerCandidates();
        if (candidates.size() != lastCandidateCount) {
            lastCandidateCount = candidates.size();
            List<String> names = new ArrayList<>();
            for (Citizen citizen : candidates) {
                names.add(citizen.getName() + " · loyalty " + (int) citizen.getLoyalty());
            }
            rulerAdapter.clear();
            rulerAdapter.addAll(names);
            rulerAdapter.notifyDataSetChanged();
        }
    }

    private static List<String> decreeNames() {
        List<String> names = new ArrayList<>();
        for (Decree decree : Decree.values()) {
            names.add(decree.getDisplayName());
        }
        return names;
    }
}
