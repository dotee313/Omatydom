package com.dotee313.omatydom.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native Android front-end: pixel icons, pixel island, 3-second tap explanations. */
public class MainActivity extends Activity {

    private static final int CANDIDATES_PER_PAGE = 5;
    private static final int HINT_MILLIS = 3000;

    private OmatydomGame game;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean ticking;

    private TextView statusView;
    private TextView rulerView;
    private TextView hintView;
    private ListView eventsView;
    private IslandView islandView;
    private EventAdapter eventAdapter;

    private ImageButton pauseButton;
    private ImageButton speedButton;
    private ImageButton controlButton;
    private ImageButton assassinateButton;
    private ImageButton nextCandidateButton;
    private LinearLayout decreeRow;
    private LinearLayout candidateRow;

    private final List<ImageButton> decreeButtons = new ArrayList<>();
    private final List<ImageButton> candidateButtons = new ArrayList<>();
    private final Decree[] decrees = Decree.values();
    private List<Citizen> candidates = new ArrayList<>();
    private int candidatePage;
    private String candidateSignature;

    private final Runnable hideHint = () -> hintView.setText("");

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
        hintView = findViewById(R.id.hintText);
        eventsView = findViewById(R.id.eventsList);
        pauseButton = findViewById(R.id.pauseButton);
        speedButton = findViewById(R.id.speedButton);
        controlButton = findViewById(R.id.controlButton);
        assassinateButton = findViewById(R.id.assassinateButton);
        nextCandidateButton = findViewById(R.id.nextCandidateButton);
        decreeRow = findViewById(R.id.decreeRow);
        candidateRow = findViewById(R.id.candidateRow);

        eventAdapter = new EventAdapter(this);
        eventsView.setAdapter(eventAdapter);

        islandView = findViewById(R.id.islandView);
        islandView.setGame(game);
        islandView.setOnThingTappedListener(new IslandView.OnThingTappedListener() {
            @Override
            public void onAgentTapped(Citizen citizen) {
                islandView.selectAgent(citizen.getId());
                showHint(citizenExplanation(citizen));
            }

            @Override
            public void onTileTapped(int x, int y) {
                showHint(tileExplanation(x, y));
            }
        });

        setupControlButtons();
        setupDecreeButtons();
        setupCandidateButtons();

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
        uiHandler.removeCallbacks(hideHint);
        super.onPause();
    }

    private void setupControlButtons() {
        pauseButton.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.PAUSE));
        pauseButton.setOnClickListener(v -> {
            game.togglePause();
            showHint(game.isPaused()
                    ? "الزمن متوقف مؤقتًا — اضغط مرة أخرى للمتابعة."
                    : "الزمن يعمل الآن — الأيام تمر تلقائيًا.");
            refreshUi();
        });

        speedButton.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.SPEED));
        speedButton.setOnClickListener(v -> {
            game.cycleSpeed();
            showHint("سرعة المحاكاة الآن x" + game.getSpeed()
                    + " — كل ضغطة تنتقل بين السرعات المتاحة.");
            refreshUi();
        });

        controlButton.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.CONTROL));
        controlButton.setOnClickListener(v -> {
            if (game.isPlayerControlled()) {
                game.releaseControl();
                showHint("تركت الحاكم يتخذ قراراته وحده مرة أخرى.");
            } else {
                game.takeControl();
                showHint("أصبحت قرارات الحاكم بيدك — اختياراتك ومراسيمك تؤثر على الجزيرة.");
            }
            refreshUi();
        });

        assassinateButton.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.ASSASSINATE));
        assassinateButton.setOnClickListener(v -> {
            if (game.assassinateRuler()) {
                showHint("سقط الحاكم — استقرار أقل وخلافة تنتظر الاختيار.");
            } else {
                showHint("لا يوجد حاكم حي يمكن إزالته الآن.");
            }
            refreshUi();
        });

        nextCandidateButton.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.NEXT));
        nextCandidateButton.setOnClickListener(v -> {
            int pages = Math.max(1, (candidates.size() + CANDIDATES_PER_PAGE - 1)
                    / CANDIDATES_PER_PAGE);
            candidatePage = (candidatePage + 1) % pages;
            showHint("صفحة أخرى من المرشحين — اضغط على الوجه لتعيينه حاكمًا.");
            refreshUi();
        });
    }

    private void setupDecreeButtons() {
        int size = Math.round(44 * getResources().getDisplayMetrics().density);
        for (Decree decree : decrees) {
            ImageButton button = new ImageButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(2, 0, 2, 0);
            button.setLayoutParams(params);
            button.setBackground(null);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(2, 2, 2, 2);
            button.setContentDescription(decree.getDisplayName());
            button.setImageDrawable(PixelIcons.decree(this, decree));
            button.setOnClickListener(v -> {
                game.issueDecree(decree);
                showHint(decreeExplanation(decree));
                refreshUi();
            });
            decreeRow.addView(button);
            decreeButtons.add(button);
        }
    }

    private void setupCandidateButtons() {
        int size = Math.round(44 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < CANDIDATES_PER_PAGE; i++) {
            ImageButton button = new ImageButton(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(2, 0, 2, 0);
            button.setLayoutParams(params);
            button.setBackground(null);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(2, 2, 2, 2);
            button.setVisibility(View.INVISIBLE);
            candidateRow.addView(button);
            candidateButtons.add(button);
        }
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

    private void showHint(String message) {
        hintView.setText(message);
        uiHandler.removeCallbacks(hideHint);
        uiHandler.postDelayed(hideHint, HINT_MILLIS);
    }

    private void refreshUi() {
        World world = game.getWorld();

        statusView.setText(String.format(Locale.getDefault(),
                "اليوم %d  •  السكان %d  •  الاستقرار %.0f/100%nذهب %.0f  •  طعام %.0f  •  خشب %.0f%nحجر %.0f  •  خام %.0f  •  مبانٍ %d  •  سرعة x%d  •  %s",
                world.getDay(), world.getPopulation(), world.getStability(),
                world.getGold(), world.getFood(), world.getWood(),
                world.getStone(), world.getOre(), world.getMap().countStructures(),
                game.getSpeed(), game.isPaused() ? "متوقف" : "يعمل"));

        if (world.getRuler() == null) {
            rulerView.setText("لا يوجد حاكم — الجزيرة تنتظر قائدًا.");
        } else {
            rulerView.setText(String.format(Locale.getDefault(),
                    "الحاكم: %s • تأييد %.0f/100 • %d يومًا • %s",
                    world.getRuler().getCitizen().getName(),
                    world.getRuler().getApproval(),
                    world.getRuler().getReignDays(world.getDay()),
                    game.isPlayerControlled() ? "تحت إدارتك" : "يتصرف وحده"));
        }

        eventAdapter.clear();
        for (GameEvent event : world.getRecentEvents(40)) {
            eventAdapter.add(event);
        }
        eventAdapter.notifyDataSetChanged();
        islandView.invalidate();

        candidates = game.getRulerCandidates();
        int pages = Math.max(1, (candidates.size() + CANDIDATES_PER_PAGE - 1)
                / CANDIDATES_PER_PAGE);
        if (candidatePage >= pages) {
            candidatePage = 0;
        }
        refreshCandidateButtons();
    }

    private void refreshCandidateButtons() {
        StringBuilder signature = new StringBuilder();
        int start = candidatePage * CANDIDATES_PER_PAGE;
        for (int i = 0; i < CANDIDATES_PER_PAGE; i++) {
            int index = start + i;
            if (index < candidates.size()) {
                Citizen citizen = candidates.get(index);
                signature.append(citizen.getId()).append(':')
                        .append(citizen.getJob()).append(':')
                        .append(citizen.getGender()).append(';');
            } else {
                signature.append("-;");
            }
        }
        String value = signature.toString();
        if (value.equals(candidateSignature)) {
            return;
        }
        candidateSignature = value;

        for (int i = 0; i < CANDIDATES_PER_PAGE; i++) {
            ImageButton button = candidateButtons.get(i);
            int index = start + i;
            if (index >= candidates.size()) {
                button.setVisibility(View.INVISIBLE);
                continue;
            }
            final Citizen citizen = candidates.get(index);
            button.setVisibility(View.VISIBLE);
            button.setContentDescription(citizen.getName());
            button.setImageDrawable(PixelIcons.portrait(this, citizen));
            button.setOnClickListener(v -> {
                if (game.chooseRuler(citizen.getId())) {
                    showHint("تم تعيين " + citizen.getName() + " حاكمًا للجزيرة.");
                } else {
                    showHint("لا يمكن تعيين هذا الشخص — يجب أن يكون بالغًا وحيًا.");
                }
                refreshUi();
            });
        }
    }

    private String citizenExplanation(Citizen citizen) {
        return String.format(Locale.getDefault(),
                "%s — %s • %s • %d سنة • صحة %.0f • سعادة %.0f • فكرة: %s.%nاضغط على وجهه في الأعلى لتعيينه حاكمًا.",
                citizen.getName(),
                citizen.getRace().getDescription(),
                arabicJob(citizen.getJob()),
                citizen.getAgeYears(),
                citizen.getHealth(),
                citizen.getHappiness(),
                citizen.getIdea().getDisplayName());
    }

    private String tileExplanation(int x, int y) {
        Tile tile = game.getMap().get(x, y);
        if (tile == null) {
            return "خارج الجزيرة.";
        }
        String name = arabicTile(tile.getType());
        String detail;
        if (tile.getStructure() != Structure.NONE) {
            detail = "هنا " + arabicStructure(tile.getStructure())
                    + " — مبنى يؤثر على العمل والاستقرار.";
        } else {
            switch (tile.getType()) {
                case WATER:
                    detail = "ماء عميق — الصيادون يعملون من الشاطئ القريب.";
                    break;
                case SAND:
                    detail = "شاطئ رملي يفصل الجزيرة عن البحر.";
                    break;
                case GRASS:
                    detail = "أرض عشبية صالحة للمشي والبناء.";
                    break;
                case FOREST:
                    detail = "غابة — الحطّابون يقطعون هنا الخشب. المخزون: "
                            + tile.getResource() + ".";
                    break;
                case ORCHARD:
                    detail = "بستان — جامعو الفواكه يأخذون هنا الطعام. المخزون: "
                            + tile.getResource() + ".";
                    break;
                case MOUNTAIN:
                    detail = "جبل — المنقّبون يستخرجون الحجر والخام. المخزون: "
                            + tile.getResource() + ".";
                    break;
                case CAVE:
                    detail = "كهف — هنا تُوجد معادن نادرة وجذور قديمة. المخزون: "
                            + tile.getResource() + ".";
                    break;
                default:
                    detail = "أرض عادية.";
                    break;
            }
        }
        return name + " (" + x + "," + y + "): " + detail;
    }

    private static String decreeExplanation(Decree decree) {
        switch (decree) {
            case FESTIVAL:
                return "مهرجان عام: موسيقى وطعام يرفعان السعادة والولاء مقابل قليل من الذهب.";
            case TAX_CUT:
                return "تخفيض الضرائب: الخزانة تقل، لكن الناس يتنفسون ويحبون الحاكم.";
            case TAX_RAISE:
                return "زيادة الضرائب: ذهب أكثر في الخزانة، وغضب أكثر في الشارع.";
            case BUILD_TEMPLE:
                return "بناء معبد: حجر مرتفع ومكان عبادة يزيد الاستقرار والولاء.";
            case AMNESTY:
                return "عفو عام: تُفتح الأبواب وتخفف الضغائن القديمة.";
            case CONSCRIPTION:
                return "تجنيد: كل بيت يرسل حارسًا — نظام أكثر وسعادة أقل.";
            case FREE_TRADE:
                return "تجارة حرة: سفن جديدة تحمل ذهبًا وأفكارًا إلى الجزيرة.";
            case CRACKDOWN:
                return "حملة صارمة: الحرس يكثر في الشوارع — استقرار أعلى وصمت أعلى.";
            default:
                return decree.getDescription();
        }
    }

    private static String arabicJob(Job job) {
        switch (job) {
            case RULER: return "حاكم";
            case PRIEST: return "كاهن";
            case BUILDER: return "بانٍ";
            case WOODCUTTER: return "حطّاب";
            case GATHERER: return "جامع فواكه";
            case MINER: return "منقّب";
            case HUNTER: return "صيّاد بري";
            case FISHER: return "صيّاد سمك";
            case TRADER: return "تاجر";
            case LABORER:
            default: return "عامل";
        }
    }

    private static String arabicTile(TileType type) {
        switch (type) {
            case WATER: return "ماء";
            case SAND: return "رمال";
            case GRASS: return "عشب";
            case FOREST: return "غابة";
            case ORCHARD: return "بستان";
            case MOUNTAIN: return "جبل";
            case CAVE: return "كهف";
            default: return "أرض";
        }
    }

    private static String arabicStructure(Structure structure) {
        switch (structure) {
            case HUT: return "كوخ";
            case TEMPLE: return "معبد";
            case TOWER: return "برج مراقبة";
            case DOCK: return "رصيف";
            case MINE: return "منجم";
            default: return "مبنى";
        }
    }
}
