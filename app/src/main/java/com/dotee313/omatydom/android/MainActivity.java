package com.dotee313.omatydom.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dotee313.omatydom.core.Age;
import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.Power;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native Android front-end with four tabs, a bottom bar and 3-second hints. */
public class MainActivity extends Activity {

    private static final int HINT_MILLIS = 3000;
    private static final String[] SEASONS = {"ربيع", "صيف", "خريف", "شتاء"};

    private OmatydomGame game;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean ticking;

    private TextView statusView;
    private TextView rulerView;
    private TextView seasonView;
    private TextView hintView;
    private ProgressBar dayProgress;
    private ListView eventsView;
    private IslandView islandView;
    private EventAdapter eventAdapter;

    private LinearLayout peopleList;
    private GridLayout decreeGrid;
    private GridLayout powerGrid;

    private final ImageButton[] navButtons = new ImageButton[5];
    private final View[] pages = new View[5];
    private int selectedTileX = -1;
    private int selectedTileY = -1;
    private int currentTab;

    private final List<Citizen> candidates = new ArrayList<>();
    private String peopleSignature;

    private final Runnable hideHint = () -> hintView.setText("");

    private final Runnable tickLoop = new Runnable() {
        @Override
        public void run() {
            if (!ticking) {
                return;
            }
            game.tick();
            refreshUi();
            uiHandler.postDelayed(this, GameConfig.UI_TICK_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = OmatydomGame.create();

        statusView = findViewById(R.id.statusText);
        rulerView = findViewById(R.id.rulerText);
        seasonView = findViewById(R.id.seasonText);
        hintView = findViewById(R.id.hintText);
        dayProgress = findViewById(R.id.dayProgress);
        eventsView = findViewById(R.id.eventsList);
        peopleList = findViewById(R.id.peopleList);
        decreeGrid = findViewById(R.id.decreeGrid);
        powerGrid = findViewById(R.id.powerGrid);

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
                selectedTileX = x;
                selectedTileY = y;
                showHint(tileExplanation(x, y) + " — صار هذا هدف القدرات.");
            }
        });

        setupTopControls();
        setupBottomNav();
        setupDecreeGrid();
        setupPowersGrid();

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

    private void setupTopControls() {
        ImageButton pause = findViewById(R.id.pauseButton);
        ImageButton speed = findViewById(R.id.speedButton);
        ImageButton control = findViewById(R.id.controlButton);
        ImageButton assassinate = findViewById(R.id.assassinateButton);

        pause.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.PAUSE));
        pause.setOnClickListener(v -> {
            game.togglePause();
            showHint(game.isPaused()
                    ? "الزمن متوقف — اضغط مرة أخرى للمتابعة."
                    : "الزمن يعمل — السنة الكاملة تساعة واحدة عند السرعة x1.");
            refreshUi();
        });

        speed.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.SPEED));
        speed.setOnClickListener(v -> {
            game.cycleSpeed();
            showHint("السرعة x" + game.getSpeed()
                    + " — عند x1 تساوي السنة ساعة واحدة، والضغطات التالية أسرع.");
            refreshUi();
        });

        control.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.CONTROL));
        control.setOnClickListener(v -> {
            if (game.isPlayerControlled()) {
                game.releaseControl();
                showHint("تركت الحاكم يتخذ قراراته وحده مرة أخرى.");
            } else {
                game.takeControl();
                showHint("قرارات الحاكم أصبحت بيدك — اختر من صف السكان من تريد تعيينه.");
            }
            refreshUi();
        });

        assassinate.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.ASSASSINATE));
        assassinate.setOnClickListener(v -> {
            if (game.assassinateRuler()) {
                showHint("سقط الحاكم — استقرار أقل وخلافة تنتظر الاختيار.");
            } else {
                showHint("لا يوجد حاكم حي يمكن إزالته الآن.");
            }
            refreshUi();
        });
    }

    private void setupBottomNav() {
        navButtons[0] = findViewById(R.id.navWorldButton);
        navButtons[1] = findViewById(R.id.navPeopleButton);
        navButtons[2] = findViewById(R.id.navDecreesButton);
        navButtons[3] = findViewById(R.id.navPowersButton);
        navButtons[4] = findViewById(R.id.navEventsButton);
        pages[0] = findViewById(R.id.worldPage);
        pages[1] = findViewById(R.id.peoplePage);
        pages[2] = findViewById(R.id.decreesPage);
        pages[3] = findViewById(R.id.powersPage);
        pages[4] = findViewById(R.id.eventsPage);
        navButtons[3].setImageDrawable(PixelIcons.control(this, PixelIcons.Control.CONTROL));

        final String[] hints = {
                "الجزيرة: اضغط أي شخص أو قطعة أرض لتعرف ما هي.",
                "السكان: اضغط أي وجه لتعيين صاحبه حاكمًا.",
                "المراسيم: كل أيقونة أمر يؤثر على كل الجزيرة.",
                "القدرات: اضغط أيقونة لتغيير العالم — اختر هدفها بالضغط على الأرض أولًا.",
                "الأحداث: سجل ما حدث يومًا بيوم."
        };
        for (int i = 0; i < navButtons.length; i++) {
            final int index = i;
            navButtons[i].setOnClickListener(v -> {
                selectTab(index);
                showHint(hints[index]);
            });
        }
        selectTab(0);
    }

    private void selectTab(int index) {
        currentTab = index;
        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisibility(i == index ? View.VISIBLE : View.GONE);
            navButtons[i].setAlpha(i == index ? 1f : 0.45f);
        }
        if (index == 1) {
            peopleSignature = null;
            refreshPeopleList();
        }
    }

    private void setupDecreeGrid() {
        int size = Math.round(62 * getResources().getDisplayMetrics().density);
        Decree[] decrees = Decree.values();
        for (int i = 0; i < decrees.length; i++) {
            final Decree decree = decrees[i];
            ImageButton button = new ImageButton(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 4), GridLayout.spec(i % 4));
            params.width = size;
            params.height = size;
            button.setLayoutParams(params);
            button.setBackground(null);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(4, 4, 4, 4);
            button.setContentDescription(decree.getDisplayName());
            button.setImageResource(PixelIcons.decreeImage(decree));
            button.setOnClickListener(v -> {
                game.issueDecree(decree);
                showHint(decreeExplanation(decree));
                refreshUi();
            });
            decreeGrid.addView(button);
        }
    }

    private void setupPowersGrid() {
        int size = Math.round(56 * getResources().getDisplayMetrics().density);
        Power[] powers = Power.values();
        for (int i = 0; i < powers.length; i++) {
            final Power power = powers[i];
            ImageButton button = new ImageButton(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 5), GridLayout.spec(i % 5));
            params.width = size;
            params.height = size;
            button.setLayoutParams(params);
            button.setBackground(null);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(3, 3, 3, 3);
            button.setContentDescription(arabicPower(power));
            button.setImageDrawable(PixelIcons.power(this, power));
            button.setOnClickListener(v -> castPower(power));
            powerGrid.addView(button);
        }
    }

    private void castPower(Power power) {
        boolean targeted = selectedTileX >= 0 && selectedTileY >= 0;
        int cooldown = game.getPowerCooldownDaysLeft(power);
        if (cooldown > 0) {
            showHint("هذه القدرة تحتاج " + cooldown + " يومًا قبل أن تعود.");
            return;
        }
        boolean done = targeted
                ? game.usePower(power, selectedTileX, selectedTileY)
                : game.usePower(power);
        if (!done) {
            showHint("تحتاج " + power.getGoldCost() + " ذهبًا لتفعيل «"
                    + arabicPower(power) + "».");
            return;
        }
        showHint(arabicPower(power) + " — " + arabicPowerDescription(power)
                + "\nالتبريد: " + power.getCooldownDays() + " يومًا.");
        refreshUi();
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
        int day = world.getDay();
        int year = day / GameConfig.DAYS_PER_YEAR + 1;
        int month = OmatydomGame.monthOf(day);
        String season = SEASONS[OmatydomGame.seasonOf(day)];

        statusView.setText(String.format(Locale.getDefault(),
                "اليوم %d • السنة %d • الشهر %d • %s%nالسكان %d • الاستقرار %.0f/100%nذهب %.0f • طعام %.0f • خشب %.0f • حجر %.0f",
                day, year, month, season, world.getPopulation(), world.getStability(),
                world.getGold(), world.getFood(), world.getWood(), world.getStone()));

        int expecting = 0;
        for (Citizen citizen : world.getLivingCitizens()) {
            if (citizen.isPregnant()) {
                expecting++;
            }
        }
        if (world.getRuler() == null) {
            rulerView.setText("لا يوجد حاكم — الجزيرة تنتظر قائدًا • حوامل: " + expecting);
        } else {
            rulerView.setText(String.format(Locale.getDefault(),
                    "الحاكم: %s • تأييد %.0f/100 • %s • حوامل: %d",
                    world.getRuler().getCitizen().getName(),
                    world.getRuler().getApproval(),
                    game.isPlayerControlled() ? "تحت إدارتك" : "يتصرف وحده",
                    expecting));
        }

        Age age = game.getAge();
        seasonView.setText(String.format(Locale.getDefault(),
                "%s • %s • %s • الشهر %d/12 • يوم %d/%d%nمواليد %d • وفيات %d • مبانٍ %d • قدرات %d",
                arabicAge(age),
                arabicWeather(world.getWeather()),
                season, month, day % GameConfig.DAYS_PER_YEAR == 0
                        ? GameConfig.DAYS_PER_YEAR : day % GameConfig.DAYS_PER_YEAR,
                GameConfig.DAYS_PER_YEAR,
                world.getBirths(), world.getDeaths(),
                world.getStructuresBuilt(), world.getPowersUsed()));
        dayProgress.setProgress((int) (game.getDayProgress() * 100));

        eventAdapter.clear();
        for (GameEvent event : world.getRecentEvents(40)) {
            eventAdapter.add(event);
        }
        eventAdapter.notifyDataSetChanged();
        islandView.invalidate();

        candidates.clear();
        candidates.addAll(game.getRulerCandidates());
        if (currentTab == 1) {
            refreshPeopleList();
        }
    }

    private void refreshPeopleList() {
        StringBuilder signature = new StringBuilder();
        for (Citizen citizen : candidates) {
            signature.append(citizen.getId()).append(':').append(citizen.getJob())
                    .append(':').append(citizen.isPregnant()).append(';');
        }
        String value = signature.toString();
        if (value.equals(peopleSignature)) {
            return;
        }
        peopleSignature = value;
        peopleList.removeAllViews();

        int portraitSize = Math.round(46 * getResources().getDisplayMetrics().density);
        for (final Citizen citizen : candidates) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(6, 6, 6, 6);
            row.setBackgroundResource(android.R.drawable.list_selector_background);

            ImageView portrait = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    portraitSize, portraitSize);
            portrait.setLayoutParams(imageParams);
            portrait.setImageDrawable(PixelIcons.portrait(this, citizen));
            portrait.setAdjustViewBounds(false);
            portrait.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(portrait);

            TextView info = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            info.setLayoutParams(textParams);
            info.setTextColor(0xFFFFFFFF);
            info.setTextSize(12f);
            String status = citizen.isPregnant()
                    ? "حامل • يوم " + citizen.getGestationDay(game.getWorld().getDay())
                            + "/" + GameConfig.GESTATION_DAYS
                    : arabicJob(citizen.getJob()) + " • " + citizen.getAgeYears() + " سنة";
            status += " • " + arabicTrait(citizen.getTrait());
            info.setText(citizen.getName() + "\n" + status);
            row.addView(info);

            row.setOnClickListener(v -> {
                if (game.chooseRuler(citizen.getId())) {
                    showHint("تم تعيين " + citizen.getName() + " حاكمًا للجزيرة.");
                } else {
                    showHint("لا يمكن تعيين هذا الشخص — يجب أن يكون بالغًا وحيًا.");
                }
                refreshUi();
            });
            peopleList.addView(row);
        }
    }

    private String citizenExplanation(Citizen citizen) {
        String state = citizen.isPregnant()
                ? "حامل في يوم " + citizen.getGestationDay(game.getWorld().getDay())
                        + " من " + GameConfig.GESTATION_DAYS
                        + " — ستلد بعد ثلاثة فصول."
                : arabicJob(citizen.getJob()) + " • " + citizen.getAgeYears() + " سنة • صحة "
                        + (int) citizen.getHealth() + " • سعادة " + (int) citizen.getHappiness();
        return citizen.getName() + " — " + citizen.getRace().getDescription()
                + "\n" + state + " • " + arabicTrait(citizen.getTrait())
                + ". اضغط وجهه في تبويب السكان لتعيينه حاكمًا.";
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
                    detail = "غابة — الحطّابون يقطعون هنا الخشب. المخزون: " + tile.getResource() + ".";
                    break;
                case ORCHARD:
                    detail = "بستان — جامعو الفواكه يأخذون هنا الطعام. المخزون: " + tile.getResource() + ".";
                    break;
                case MOUNTAIN:
                    detail = "جبل — المنقّبون يستخرجون الحجر والخام. المخزون: " + tile.getResource() + ".";
                    break;
                case CAVE:
                    detail = "كهف — هنا تُوجد معادن نادرة وجذور قديمة. المخزون: " + tile.getResource() + ".";
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

    private static String arabicPower(Power power) {
        switch (power) {
            case RAIN: return "مطر الحياة";
            case BLOOM: return "المدّ الأخضر";
            case HEAL: return "نور الشفاء";
            case BLESS: return "البركة";
            case LIGHTNING: return "رمح العاصفة";
            case PLAGUE: return "الحمّى الرمادية";
            case EARTHQUAKE: return "زلزال";
            case METEOR: return "نيزك";
            case PEACE: return "سكينة";
            case PROSPERITY:
            default: return "مطر الذهب";
        }
    }

    private static String arabicPowerDescription(Power power) {
        switch (power) {
            case RAIN: return "يعيد ملء الغابات والبساتين والجبال بالماء.";
            case BLOOM: return "ينبت مساحة واسعة ويضيف طعامًا للمخازن.";
            case HEAL: return "يعيد صحة كل من حول نقطة الهدف.";
            case BLESS: return "يرفع السعادة والولاء في المنطقة.";
            case LIGHTNING: return "يضرب الناس والحيوانات في مساحة صغيرة.";
            case PLAGUE: return "ينشر حمى خطيرة في مساحة صغيرة.";
            case EARTHQUAKE: return "يهدم المباني القريبة ويهز الاستقرار.";
            case METEOR: return "نيزك محرق يدمر الهدف ويترك معادن.";
            case PEACE: return "يهدئ كل الجزيرة ويرفع الاستقرار.";
            case PROSPERITY:
            default: return "يملأ الخزانة ويزيد ثروة كل بيت.";
        }
    }

    private static String arabicAge(Age age) {
        switch (age) {
            case DAWN: return "عصر الفجر";
            case SUN: return "عصر الشمس";
            case HARVEST: return "عصر الحصاد";
            case STORM: return "عصر العواصف";
            case MOON: return "عصر القمر";
            case ASH: return "عصر الرماد";
            case ICE: return "عصر الجليد";
            case RENEWAL:
            default: return "عصر التجدد";
        }
    }

    private static String arabicWeather(com.dotee313.omatydom.core.world.Weather weather) {
        switch (weather) {
            case RAIN: return "مطر";
            case STORM: return "عاصفة";
            case DROUGHT: return "جفاف";
            case SNOW: return "ثلوج";
            case ASH: return "رماد بركاني";
            case CLEAR:
            default: return "صحو";
        }
    }

    private static String arabicTrait(com.dotee313.omatydom.core.model.Trait trait) {
        switch (trait) {
            case BRAVE: return "شجاع";
            case LAZY: return "كسول";
            case GENEROUS: return "كريم";
            case ZEALOUS: return "متعصّب";
            case CURIOUS: return "فضولي";
            case RESILIENT: return "صامد";
            case GREEDY: return "طماع";
            case CONTENT:
            default: return "راضٍ";
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
