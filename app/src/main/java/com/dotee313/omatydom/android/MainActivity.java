package com.dotee313.omatydom.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.dotee313.omatydom.core.BuildingType;
import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.DisasterType;
import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.GameEvent;
import com.dotee313.omatydom.core.ModPack;
import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.Power;
import com.dotee313.omatydom.core.ResourceType;
import com.dotee313.omatydom.core.TechCategory;
import com.dotee313.omatydom.core.Technology;
import com.dotee313.omatydom.core.World;
import com.dotee313.omatydom.core.civ.Achievement;
import com.dotee313.omatydom.core.civ.Clan;
import com.dotee313.omatydom.core.civ.Culture;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.StatusEffect;
import com.dotee313.omatydom.core.civ.TradeRoute;
import com.dotee313.omatydom.core.civ.War;
import com.dotee313.omatydom.core.civ.WarType;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Ideology;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.model.Religion;
import com.dotee313.omatydom.core.model.SocialClass;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.MapTemplate;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.Weather;
import com.dotee313.omatydom.core.world.WorldLaw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Seven-tab Android front-end with code-drawn pixel icons and 3-second Arabic hints. */
public class MainActivity extends Activity {

    private static final int HINT_MILLIS = 3000;
    private static final String[] SEASONS = {"ربيع", "صيف", "خريف", "شتاء"};
    private static final String[] TAB_HINTS = {
            "الجزيرة: اختر طبقة الألوان، ثم اضغط أي شخص أو قطعة أرض لتعرف ما هي.",
            "السكان: اضغط أي وجه لتعيين صاحبه حاكمًا.",
            "الطبيعة: قوانين العالم، نمط اللعبة، الأحياء الحيوية والموارد.",
            "المخلوقات: عدد كل كائن، واضغط + لتطلق كائنًا جديدًا.",
            "الممالك: عشائر وممالك وحروب وتقنيات ومراسيم — كل أيقونة تشرح نفسها.",
            "القدرات: اضغط أيقونة لتغيير العالم، أو أرسل إحدى الكوارث السبع عشرة.",
            "الأحداث: إحصاءات عميقة، الإنجازات وسجل الأيام."
    };
    private static final String[] OVERLAY_HINTS = {
            "طبقة التضاريس: الألوان الطبيعية للبر والماء والغابات.",
            "طبقة الأحياء الحيوية: كل قطعة تُلوَّن بحسب حيويتها.",
            "طبقة الممالك: اللون لأقرب عاصمة ملكية إلى كل قطعة.",
            "طبقة المعتقدات: لون الدين والفكرة لأقرب شخص إلى المقطع.",
            "طبقة المهن: لون عمل الشخص الواقف على المقطع."
    };
    private static final String[] TAB_KEYS = {
            "world", "people", "nature", "creatures", "kingdoms", "powers", "events"
    };

    /** One short Arabic explanation per brush, index matches IslandView.Brush. */
    private static final String[] BRUSH_HINTS = {
            "فحص: اضغط شخصًا أو قطعة أرض لتقرأ شرحها وتختار هدف القدرات.",
            "ماء: تموجات زرقاء تحوّل المقطع إلى ماء عميق للصيادين.",
            "رمال: يصنع شاطئًا رمليًا يفصل البر عن البحر.",
            "عشب: يهيئ أرضًا مفتوحة للمشي والبناء.",
            "غابة: تنبت أشجارًا يقطع منها الحطّابون الخشب.",
            "بستان: تنبت فواكه يجمعها السكان طعامًا.",
            "جبل: يرفع صخرة يستخرج منها الحجر والخام.",
            "كهف: يفتح تجويفًا فيه معادن نادرة وجذور قديمة.",
            "كوخ: يبني بيتًا للسكان مقابل خشب وحجر.",
            "مزرعة: تنتج محاصيل يخزنها العمال مقابل خشب وحجر.",
            "طريق: يرصّ أحجارًا تسهّل تنقل السكان والتجار.",
            "إنسان: يضيف مستوطنًا جديدًا على أرض قابلة للمشي.",
            "ذئب: يطلق كائنًا مفترسًا يطوف حول المقطع.",
            "نار: رمح العاصفة يضرب الهدف ويترك أثر لهب.",
            "مطر: يعيد ملء الغابات والبساتين قرب الهدف.",
            "نيزك: حجر محرق يدمر الهدف ويترك معادن."
    };

    /** Short notes shown on the first tile painted with a brush. */
    private static final String[] BRUSH_PAINT_HINTS = {
            "تم الفحص — اضغط أيقونة أخرى لتغيير الأداة.",
            "تلوين الماء جارٍ — اسحب لرسم الشاطئ.",
            "الرمال تنتشر — اسحب لتوسيع الشاطئ.",
            "العشب ينتشر — اسحب لتهيئة الأرض.",
            "الأشجار تنبت — اسحب لتكثيف الغابة.",
            "البساتين تنبت — اسحب لتوسيع مصدر الطعام.",
            "الجبال ترتفع — اسحب لتشكيل السلسلة.",
            "الكهوف تتفتح — اسحب لإضافة مغارات.",
            "يُبنى كوخ جديد حيث تلمس الفرشاة.",
            "تُبنى مزرعة جديدة حيث تلمس الفرشاة.",
            "يمتد الطريق حجرًا بعد حجر.",
            "ظهر مستوطن جديد في المقطع.",
            "ظهر ذئب يترصد المكان.",
            "ضربة لهب نزلت على الهدف.",
            "مطر الحياة يهطل على الهدف.",
            "نيزك سقط على الهدف."
    };

    private OmatydomGame game;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> signatures = new HashMap<>();
    private boolean ticking;

    private TextView statusView;
    private TextView rulerView;
    private TextView seasonView;
    private TextView hintView;
    private TextView statText;
    private TextView templateText;
    private TextView modPackText;
    private TextView nextTechText;
    private ProgressBar dayProgress;
    private ListView eventsView;
    private IslandView islandView;
    private EventAdapter eventAdapter;
    private TextView brushHintView;
    private TextView tradeRouteText;

    private final ImageButton[] brushButtons = new ImageButton[IslandView.Brush.values().length];
    private final TextView[] resourceStripCounts = new TextView[6];
    private IslandView.Brush currentBrush = IslandView.Brush.INSPECT;
    private IslandView.Brush lastPaintHintBrush = IslandView.Brush.INSPECT;
    private long lastPaintHintAt;
    private boolean gridVisible;

    private static final ResourceType[] STRIP_RESOURCES = {
            ResourceType.GOLD, ResourceType.BREAD, ResourceType.WOOD,
            ResourceType.STONE, ResourceType.ORE, ResourceType.CRYSTAL
    };
    private static final String[] STRIP_LABELS = {
            "ذهب", "طعام", "خشب", "حجر", "خام", "بلور"
    };

    /** Curated, actually-buildable catalogue shown in the in-game build menu. */
    private static final BuildingType[] BUILD_MENU_TYPES = {
            BuildingType.REED_SHACK, BuildingType.TIMBER_HOME, BuildingType.CLAY_COTTAGE,
            BuildingType.FARM, BuildingType.GRANARY, BuildingType.FISHING_HUT,
            BuildingType.MINE, BuildingType.QUARRY, BuildingType.SMITHY,
            BuildingType.WEAVING_SHED, BuildingType.POTTERY, BuildingType.APIARY,
            BuildingType.WINDMILL, BuildingType.BAKERY, BuildingType.SCHOOL
    };

    private LinearLayout peopleList;
    private LinearLayout natureList;
    private LinearLayout creatureList;
    private LinearLayout kingdomList;
    private LinearLayout clanList;
    private LinearLayout techList;
    private LinearLayout achievementList;
    private LinearLayout warTypeRow;
    private GridLayout decreeGrid;
    private GridLayout powerGrid;
    private GridLayout disasterGrid;

    private final ImageButton[] navButtons = new ImageButton[7];
    private final View[] pages = new View[7];
    private final ImageButton[] overlayButtons = new ImageButton[5];
    private final ImageButton[] lawButtons = new ImageButton[WorldLaw.values().length];
    private final ImageButton[] modPackButtons = new ImageButton[ModPack.values().length];
    private final ImageButton[] achievementButtons = new ImageButton[Achievement.values().length];
    private final TextView[] creatureCounts = new TextView[AnimalType.values().length];
    private final TextView[] resourceAmounts = new TextView[ResourceType.values().length];

    private final List<Citizen> candidates = new ArrayList<>();
    private int currentTab;
    private int selectedTileX = -1;
    private int selectedTileY = -1;
    private int selectedCitizenId = -1;

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
        brushHintView = findViewById(R.id.brushHintText);
        tradeRouteText = findViewById(R.id.tradeRouteText);
        statText = findViewById(R.id.statText);
        dayProgress = findViewById(R.id.dayProgress);
        eventsView = findViewById(R.id.eventsList);
        peopleList = findViewById(R.id.peopleList);
        natureList = findViewById(R.id.natureList);
        creatureList = findViewById(R.id.creatureList);
        kingdomList = findViewById(R.id.kingdomList);
        clanList = findViewById(R.id.clanList);
        techList = findViewById(R.id.techList);
        achievementList = findViewById(R.id.achievementList);
        warTypeRow = findViewById(R.id.warTypeRow);
        decreeGrid = findViewById(R.id.decreeGrid);
        powerGrid = findViewById(R.id.powerGrid);
        disasterGrid = findViewById(R.id.disasterGrid);

        eventAdapter = new EventAdapter(this);
        eventsView.setAdapter(eventAdapter);

        islandView = findViewById(R.id.islandView);
        islandView.setGame(game);
        islandView.setOnThingTappedListener(new IslandView.OnThingTappedListener() {
            @Override
            public void onAgentTapped(Citizen citizen) {
                selectedCitizenId = citizen.getId();
                islandView.selectAgent(citizen.getId());
                openCitizenInfo(citizen);
                showHint("بطاقة «" + citizen.getName() + "» — اختر من النافذة.");
            }

            @Override
            public void onTileTapped(int x, int y) {
                selectedTileX = x;
                selectedTileY = y;
                showHint(tileExplanation(x, y) + " — صار هذا هدف القدرات.");
            }

            @Override
            public void onTilePainted(int x, int y, IslandView.Brush brush) {
                boolean hintShown = paintAt(x, y, brush);
                if (!hintShown) {
                    showPaintHintOnce(brush);
                }
                islandView.invalidate();
            }
        });
        islandView.setOnBuildRequestedListener(new IslandView.OnBuildRequestedListener() {
            @Override
            public void onBuildRequested(int x, int y, BuildingType type) {
                handleBuildRequested(x, y, type);
            }

            @Override
            public void onTerrainRequested(int x, int y, TileType type) {
                handleTerrainRequested(x, y, type);
            }
        });

        setupTopControls();
        setupUtilityBar();
        setupResourceStrip();
        setupOverlayBar();
        setupBrushBar();
        setupBuildButton();
        setupMapControls();
        setupBottomNav();
        setupDecreeGrid();
        setupPowersGrid();
        setupDisasterGrid();
        setupNatureStatic();
        setupCreaturesStatic();
        setupKingdomActions();
        setupAchievements();

        selectTab(0);
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

    // ------------------------------------------------------------------
    // Static controls
    // ------------------------------------------------------------------

    private void setupTopControls() {
        ImageButton pause = findViewById(R.id.pauseButton);
        ImageButton speed = findViewById(R.id.speedButton);
        ImageButton control = findViewById(R.id.controlButton);
        ImageButton assassinate = findViewById(R.id.assassinateButton);

        pause.setContentDescription("إيقاف الزمن أو متابعته");
        speed.setContentDescription("تغيير سرعة الزمن");
        control.setContentDescription("تولي قرارات الحاكم أو تركها له");
        assassinate.setContentDescription("إزالة الحاكم الحالي");

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
            game.togglePlayerControl();
            showHint(game.isPlayerControlled()
                    ? "قرارات الحاكم أصبحت بيدك — اختر من صف السكان من تريد تعيينه."
                    : "تركت الحاكم يتخذ قراراته وحده مرة أخرى.");
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

    /** Three compact utilities above the game controls. All hints stay short and Arabic. */
    private void setupUtilityBar() {
        ImageButton info = findViewById(R.id.utilityInfoButton);
        ImageButton mods = findViewById(R.id.utilityModsButton);
        ImageButton settings = findViewById(R.id.utilitySettingsButton);

        info.setImageDrawable(UserArt.info(this));
        mods.setImageDrawable(UserArt.mods(this));
        settings.setImageDrawable(UserArt.settings(this));

        info.setOnClickListener(v -> {
            String about = UserArt.aboutText();
            showHint(about);
            ImageView header = new ImageView(this);
            header.setImageResource(R.drawable.user_menu_background);
            header.setScaleType(ImageView.ScaleType.CENTER_CROP);
            header.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(72)));
            new AlertDialog.Builder(this)
                    .setCustomTitle(header)
                    .setMessage(about)
                    .setPositiveButton("إغلاق", null)
                    .show();
        });

        mods.setOnClickListener(v -> {
            ModPack[] packs = ModPack.values();
            ModPack next = packs[(game.getModPack().ordinal() + 1) % packs.length];
            game.setModPack(next);
            showHint("نمط اللعبة الآن: «" + next.getDisplayNameAr() + "» — " + next.getDescription());
            refreshUi();
        });

        // Reuse the existing pause/speed listeners instead of duplicating their logic.
        settings.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("إعدادات الزمن")
                .setMessage("غيّر الزمن هنا أو من أعلى الشاشة — اللعبة مستمرة أثناء القراءة.")
                .setPositiveButton(game.isPaused() ? "متابعة الزمن" : "إيقاف الزمن",
                        (dialog, which) -> findViewById(R.id.pauseButton).performClick())
                .setNegativeButton("تغيير السرعة",
                        (dialog, which) -> findViewById(R.id.speedButton).performClick())
                .setNeutralButton("إغلاق", null)
                .show());
    }

    // ------------------------------------------------------------------
    // Map toolbar: resources, brushes, camera and sandbox painting
    // ------------------------------------------------------------------

    private void setupResourceStrip() {
        LinearLayout strip = findViewById(R.id.resourceStrip);
        for (int i = 0; i < STRIP_RESOURCES.length; i++) {
            final int index = i;
            ResourceType type = STRIP_RESOURCES[i];
            LinearLayout badge = new LinearLayout(this);
            badge.setOrientation(LinearLayout.HORIZONTAL);
            badge.setGravity(android.view.Gravity.CENTER_VERTICAL);
            badge.setBackgroundResource(R.drawable.bg_button);
            badge.setPadding(2, 1, 2, 1);
            badge.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            ImageView icon = new ImageView(this);
            icon.setImageDrawable(PixelIcons.resourceSwatch(this, type.getColor()));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setContentDescription(STRIP_LABELS[i]);
            badge.addView(icon, new LinearLayout.LayoutParams(dp(14), dp(14)));

            TextView count = new TextView(this);
            count.setTextSize(9f);
            count.setTextColor(0xFFFFFFFF);
            count.setGravity(android.view.Gravity.CENTER);
            count.setBackground(PixelIcons.smallBadge(this, type.getColor()));
            badge.addView(count, new LinearLayout.LayoutParams(0, dp(18), 1f));
            resourceStripCounts[i] = count;

            badge.setOnClickListener(v -> showHint(STRIP_LABELS[index] + " — المخزون "
                    + (int) world().getResource(STRIP_RESOURCES[index]) + "."));
            strip.addView(badge);
        }
    }

    private void setupBrushBar() {
        int[] ids = {
                R.id.brushInspectButton, R.id.brushWaterButton,
                R.id.brushSandButton, R.id.brushGrassButton,
                R.id.brushForestButton, R.id.brushOrchardButton,
                R.id.brushMountainButton, R.id.brushCaveButton,
                R.id.brushHouseButton, R.id.brushFarmButton,
                R.id.brushRoadButton, R.id.brushHumanButton,
                R.id.brushWolfButton, R.id.brushFireButton,
                R.id.brushRainButton, R.id.brushMeteorButton
        };
        IslandView.Brush[] brushes = IslandView.Brush.values();
        for (int i = 0; i < ids.length && i < brushes.length; i++) {
            final IslandView.Brush brush = brushes[i];
            ImageButton button = findViewById(ids[i]);
            brushButtons[i] = button;
            button.setImageDrawable(PixelIcons.brushIcon(this, brush));
            button.setContentDescription(BRUSH_HINTS[i]);
            button.setBackgroundResource(R.drawable.bg_button);
            button.setOnClickListener(v -> selectBrush(brush, true));
        }
        selectBrush(IslandView.Brush.INSPECT, false);
    }

    private void selectBrush(IslandView.Brush brush, boolean announce) {
        if (brush == null) {
            return;
        }
        currentBrush = brush;
        islandView.setBrush(brush);
        applyBrushSelection(brush, announce);
    }

    private void applyBrushSelection(IslandView.Brush brush, boolean announce) {
        IslandView.Brush[] brushes = IslandView.Brush.values();
        for (int i = 0; i < brushButtons.length; i++) {
            boolean active = i < brushes.length && brushes[i] == brush;
            brushButtons[i].setBackgroundResource(
                    active ? R.drawable.brush_selected : R.drawable.bg_button);
            brushButtons[i].setAlpha(active ? 1f : 0.68f);
        }
        int index = brush.ordinal();
        if (index >= 0 && index < BRUSH_HINTS.length) {
            brushHintView.setText(BRUSH_HINTS[index]);
            if (announce) {
                showHint(BRUSH_HINTS[index]);
            }
        }
        lastPaintHintBrush = brush;
        lastPaintHintAt = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------
    // In-game construction: planned building / sand expansion
    // ------------------------------------------------------------------

    /** The build tool lives in Worker 1's unified dock; no XML is touched here. */
    private void setupBuildButton() {
        View build = findViewById(R.id.buildButton);
        if (build == null) {
            return;
        }
        build.setContentDescription("أدوات البناء — اختر مبنى أو توسيع اليابسة");
        if (build instanceof ImageButton) {
            ((ImageButton) build).setImageDrawable(
                    PixelIcons.brushIcon(this, IslandView.Brush.HOUSE));
        }
        build.setOnClickListener(v -> showBuildDialog());
    }

    private void showBuildDialog() {
        CharSequence[] labels = new CharSequence[BUILD_MENU_TYPES.length + 2];
        labels[0] = "توسيع اليابسة (رمال) — ماء يلامس شاطئًا فقط";
        for (int i = 0; i < BUILD_MENU_TYPES.length; i++) {
            BuildingType type = BUILD_MENU_TYPES[i];
            labels[i + 1] = type.getDisplayNameAr() + " — خشب " + type.getWoodCost()
                    + " • حجر " + type.getStoneCost() + " • ذهب " + type.getGoldCost();
        }
        labels[labels.length - 1] = "إلغاء البناء — العودة لفرش الطبيعة";

        new AlertDialog.Builder(this)
                .setTitle("ماذا تريد أن تبني؟")
                .setItems(labels, (dialog, which) -> {
                    if (which == 0) {
                        islandView.setPlannedBuilding(null);
                        islandView.setPlannedTerrain(TileType.SAND);
                        markBuildMode(true);
                        showHint("اختر مقطع ماء يلامس اليابسة — سيتحول إلى رمال ويتوسع الشاطئ.");
                    } else if (which == labels.length - 1) {
                        islandView.setPlannedBuilding(null);
                        islandView.setPlannedTerrain(null);
                        markBuildMode(false);
                        showHint("أُلغي التخطيط — فرش الطبيعة يعمل كما كان.");
                    } else {
                        BuildingType type = BUILD_MENU_TYPES[which - 1];
                        islandView.setPlannedTerrain(null);
                        islandView.setPlannedBuilding(type);
                        markBuildMode(true);
                        showHint("اختر أرضًا فارغة لبناء «" + type.getDisplayNameAr() + "» — خشب "
                                + type.getWoodCost() + " • حجر " + type.getStoneCost()
                                + " • ذهب " + type.getGoldCost() + ".");
                    }
                })
                .show();
    }

    /** Highlights the dock button only while a build or sand plan is active. */
    private void markBuildMode(boolean active) {
        View build = findViewById(R.id.buildButton);
        if (build != null) {
            build.setBackgroundResource(
                    active ? R.drawable.brush_selected : R.drawable.bg_button);
        }
    }

    private void handleBuildRequested(int x, int y, BuildingType type) {
        if (type == null) {
            return;
        }
        if (game.buildBuilding(x, y, type)) {
            showHint("تم بناء «" + type.getDisplayNameAr() + "» عند (" + x + "," + y + ").");
            refreshUi();
        } else {
            Tile tile = world().getMap().get(x, y);
            if (tile == null) {
                showHint("خارج الخريطة — لا يمكن البناء هنا.");
            } else if (!tile.isFreeLand()) {
                showHint(tile.getType() == TileType.WATER
                        ? "لا يُبنى فوق الماء — توسّع اليابسة أولًا من أدوات البناء."
                        : "الأرض غير فارغة — اختر مقطعًا بلا مبنى.");
            } else {
                World world = world();
                showHint("الموارد غير كافية: خشب " + type.getWoodCost() + " (لديك "
                        + (int) world.getWood() + ") • حجر " + type.getStoneCost() + " (لديك "
                        + (int) world.getStone() + ") • ذهب " + type.getGoldCost()
                        + " (لديك " + (int) world.getGold() + ").");
            }
        }
        islandView.invalidate();
    }

    /** Sand expansion is allowed only on a water tile touching dry land. */
    private void handleTerrainRequested(int x, int y, TileType type) {
        if (type != TileType.SAND) {
            showHint("أداة توسيع اليابسة تضع الرمال فقط.");
            return;
        }
        World world = world();
        if (!world.getMap().inBounds(x, y)) {
            showHint("خارج الخريطة — لا يمكن توسيع اليابسة هنا.");
            return;
        }
        TileType here = world.getMap().typeAt(x, y);
        if (here != TileType.WATER) {
            showHint("التوسيع يبدأ من الماء — هذا المقطع " + arabicTile(here) + ".");
            return;
        }
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        boolean touchesLand = false;
        for (int[] side : sides) {
            if (world.getMap().typeAt(x + side[0], y + side[1]) != TileType.WATER) {
                touchesLand = true;
                break;
            }
        }
        if (!touchesLand) {
            showHint("هذا الماء بعيد عن الشاطئ — اختر مقطعًا يلامس أرضًا يابسة.");
            return;
        }
        if (game.paintTile(x, y, TileType.SAND)) {
            showHint("توسّعت اليابسة عند (" + x + "," + y + ") — صار المقطع رمالًا صالحة للبناء.");
            refreshUi();
        } else {
            showHint("تعذّر توسيع اليابسة عند (" + x + "," + y + ").");
        }
        islandView.invalidate();
    }

    private void setupMapControls() {
        ImageButton zoomIn = findViewById(R.id.zoomInButton);
        ImageButton zoomOut = findViewById(R.id.zoomOutButton);
        ImageButton center = findViewById(R.id.centerButton);
        ImageButton grid = findViewById(R.id.gridButton);

        zoomIn.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.ZOOM_IN));
        zoomIn.setContentDescription("تكبير الخريطة");
        zoomIn.setOnClickListener(v -> {
            islandView.zoomIn();
            showHint("تكبير الخريطة — المربعات أكبر ويمكن رسم التفاصيل بدقة.");
        });

        zoomOut.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.ZOOM_OUT));
        zoomOut.setContentDescription("تصغير الخريطة");
        zoomOut.setOnClickListener(v -> {
            islandView.zoomOut();
            showHint("تصغير الخريطة — ترى جزيرة أوسع دفعة واحدة.");
        });

        center.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.CENTER));
        center.setContentDescription("توسيط الخريطة");
        center.setOnClickListener(v -> {
            islandView.centerOnMap();
            showHint("عادت الخريطة إلى مركز الجزيرة.");
        });

        grid.setImageDrawable(PixelIcons.control(this, PixelIcons.Control.GRID));
        grid.setContentDescription("إظهار أو إخفاء شبكة المربعات");
        grid.setOnClickListener(v -> {
            gridVisible = !gridVisible;
            islandView.setGrid(gridVisible);
            grid.setBackgroundResource(
                    gridVisible ? R.drawable.brush_selected : R.drawable.bg_button);
            showHint(gridVisible
                    ? "شبكة المربعات ظاهرة — كل مربع قطعة أرض يمكن رسمها."
                    : "أُخفيت الشبكة — الخريطة الآن بدون خطوط.");
        });
    }

    /** Applies the selected brush at one tile. Returns true when a hint was shown. */
    private boolean paintAt(int x, int y, IslandView.Brush brush) {
        if (brush == null || x < 0 || y < 0) {
            return false;
        }
        switch (brush) {
            case WATER:
                game.paintTile(x, y, TileType.WATER);
                break;
            case SAND:
                game.paintTile(x, y, TileType.SAND);
                break;
            case GRASS:
                game.paintTile(x, y, TileType.GRASS);
                break;
            case FOREST:
                game.paintTile(x, y, TileType.FOREST);
                break;
            case ORCHARD:
                game.paintTile(x, y, TileType.ORCHARD);
                break;
            case MOUNTAIN:
                game.paintTile(x, y, TileType.MOUNTAIN);
                break;
            case CAVE:
                game.paintTile(x, y, TileType.CAVE);
                break;
            case HOUSE:
                if (!game.build(x, y, Structure.HUT)) {
                    showHint("لا يُبنى كوخ هنا — تحتاج أرضًا فارغة و10 خشب و4 حجر.");
                    return true;
                }
                break;
            case FARM:
                if (!game.build(x, y, Structure.FARM)) {
                    showHint("لا تُبنى مزرعة هنا — تحتاج أرضًا فارغة و12 خشب و3 حجر.");
                    return true;
                }
                break;
            case ROAD:
                if (!game.build(x, y, Structure.ROAD)) {
                    showHint("لا يُرصّ طريق هنا — تحتاج أرضًا فارغة وخشبًا وحجرًا.");
                    return true;
                }
                break;
            case HUMAN:
                if (!game.spawnSettlerAt(x, y)) {
                    showHint("لا يستقر مستوطن هنا — اختر أرضًا قابلة للمشي.");
                    return true;
                }
                break;
            case WOLF:
                if (!game.spawnCreatureAt(AnimalType.WOLF, x, y)) {
                    showHint("خارج الخريطة — لا يمكن إطلاق ذئب هنا.");
                    return true;
                }
                break;
            case FIRE:
                game.usePower(Power.LIGHTNING, x, y);
                islandView.playEffect(IslandView.Effect.FIRE, x, y);
                break;
            case RAIN:
                game.usePower(Power.RAIN, x, y);
                islandView.playEffect(IslandView.Effect.RAIN, x, y);
                break;
            case METEOR:
                game.usePower(Power.METEOR, x, y);
                islandView.playEffect(IslandView.Effect.METEOR, x, y);
                break;
            case INSPECT:
            default:
                break;
        }
        return false;
    }

    /** One hint per stroke, not one per dragged tile. */
    private void showPaintHintOnce(IslandView.Brush brush) {
        if (brush == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (brush == lastPaintHintBrush && now - lastPaintHintAt < HINT_MILLIS) {
            return;
        }
        lastPaintHintBrush = brush;
        lastPaintHintAt = now;
        int index = brush.ordinal();
        if (index >= 0 && index < BRUSH_PAINT_HINTS.length) {
            showHint(BRUSH_PAINT_HINTS[index]);
        }
    }

    private void setupOverlayBar() {
        int[] ids = {
                R.id.overlayTerrainButton, R.id.overlayBiomeButton,
                R.id.overlayKingdomButton, R.id.overlayReligionButton,
                R.id.overlayJobButton
        };
        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            ImageButton button = findViewById(ids[i]);
            overlayButtons[i] = button;
            button.setImageDrawable(PixelIcons.overlay(this, i));
            button.setContentDescription(OVERLAY_HINTS[i]);
            button.setBackgroundResource(R.drawable.bg_button);
            button.setOnClickListener(v -> selectOverlay(index, true));
        }
        selectOverlay(0, false);
    }

    private void selectOverlay(int index, boolean announce) {
        if (index < 0 || index >= overlayButtons.length) {
            return;
        }
        islandView.setOverlay(IslandView.Overlay.values()[index]);
        for (int j = 0; j < overlayButtons.length; j++) {
            boolean active = j == index;
            overlayButtons[j].setBackgroundResource(
                    active ? R.drawable.brush_selected : R.drawable.bg_button);
            overlayButtons[j].setAlpha(active ? 1f : 0.6f);
        }
        if (announce) {
            showHint(OVERLAY_HINTS[index]);
        }
    }

    private void setupBottomNav() {
        int[] ids = {
                R.id.navWorldButton, R.id.navPeopleButton, R.id.navNatureButton,
                R.id.navCreaturesButton, R.id.navKingdomsButton,
                R.id.navPowersButton, R.id.navEventsButton
        };
        int[] pageIds = {
                R.id.worldPage, R.id.peoplePage, R.id.naturePage,
                R.id.creaturesPage, R.id.kingdomsPage,
                R.id.powersPage, R.id.eventsPage
        };
        String[] descriptions = {
                "الجزيرة", "السكان", "الطبيعة", "المخلوقات",
                "الممالك", "القدرات", "الأحداث"
        };
        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            navButtons[i] = findViewById(ids[i]);
            pages[i] = findViewById(pageIds[i]);
            navButtons[i].setImageDrawable(PixelIcons.nav(this, i));
            navButtons[i].setContentDescription(descriptions[i]);
            navButtons[i].setOnClickListener(v -> {
                selectTab(index);
                showHint(TAB_HINTS[index]);
            });
        }
    }

    private void selectTab(int index) {
        currentTab = index;
        for (int i = 0; i < pages.length; i++) {
            boolean active = i == index;
            pages[i].setVisibility(active ? View.VISIBLE : View.GONE);
            navButtons[i].setBackgroundResource(
                    active ? R.drawable.nav_selected : R.drawable.bg_button);
            navButtons[i].setAlpha(active ? 1f : 0.62f);
            // The active pill is burgundy, so whiten its silhouette for contrast.
            if (active) {
                navButtons[i].setColorFilter(0xFFFFFFFF,
                        android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                navButtons[i].clearColorFilter();
            }
        }
        View dockTools = findViewById(R.id.dockMapToolsRow);
        if (dockTools != null) {
            dockTools.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        }
        // Force a rebuild on tab open; later refreshes go through signature checks.
        signatures.remove(TAB_KEYS[index]);
        if (index == 4) {
            signatures.remove("techs");
        }
        refreshCurrentTab();
    }

    private void setupDecreeGrid() {
        Decree[] decrees = Decree.values();
        for (int i = 0; i < decrees.length; i++) {
            final Decree decree = decrees[i];
            ImageButton button = new ImageButton(this);
            button.setLayoutParams(gridCellParams(i, 4, dp(46), dp(46)));
            button.setBackgroundResource(R.drawable.bg_button);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(3, 3, 3, 3);
            button.setContentDescription(decree.getDisplayName() + " — " + decreeExplanation(decree));
            button.setImageDrawable(PixelIcons.decreeImage(this, decree));
            button.setOnClickListener(v -> {
                game.issueDecree(decree);
                showHint(decreeExplanation(decree));
                refreshUi();
            });
            decreeGrid.addView(button);
        }
    }

    private void setupPowersGrid() {
        Power[] powers = Power.values();
        for (int i = 0; i < powers.length; i++) {
            final Power power = powers[i];
            ImageButton button = new ImageButton(this);
            button.setLayoutParams(gridCellParams(i, 5, dp(42), dp(42)));
            button.setBackgroundResource(R.drawable.bg_button);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(2, 2, 2, 2);
            button.setContentDescription(arabicPower(power));
            button.setImageDrawable(PixelIcons.power(this, power));
            button.setOnClickListener(v -> castPower(power));
            powerGrid.addView(button);
        }
    }

    private void setupDisasterGrid() {
        DisasterType[] disasters = DisasterType.values();
        for (int i = 0; i < disasters.length; i++) {
            final DisasterType type = disasters[i];
            ImageButton button = new ImageButton(this);
            button.setLayoutParams(gridCellParams(i, 6, dp(40), dp(40)));
            button.setBackgroundResource(R.drawable.bg_button);
            button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
            button.setPadding(2, 2, 2, 2);
            button.setContentDescription(type.getArabicName());
            button.setImageDrawable(PixelIcons.disasterIcon(this, type.getColor()));
            button.setOnClickListener(v -> {
                int tx = selectedTileX >= 0 ? selectedTileX : game.getMap().getWidth() / 2;
                int ty = selectedTileY >= 0 ? selectedTileY : game.getMap().getHeight() / 2;
                IslandView.Effect effect = effectForDisaster(type);
                game.castDisaster(type);
                islandView.playEffect(effect, tx, ty);
                showHint("كارثة «" + type.getArabicName() + "» تضرب الجزيرة عند ("
                        + tx + "," + ty + ")، راقب الأحداث.");
                refreshUi();
            });
            disasterGrid.addView(button);
        }
    }

    private void castPower(Power power) {
        boolean targeted = selectedTileX >= 0 && selectedTileY >= 0;
        int cooldown = game.getPowerCooldownDaysLeft(power);
        if (cooldown > 0) {
            showHint("هذه القدرة تحتاج " + cooldown + " يومًا قبل أن تعود.");
            return;
        }
        int tx = targeted ? selectedTileX : game.getMap().getWidth() / 2;
        int ty = targeted ? selectedTileY : game.getMap().getHeight() / 2;
        boolean done = targeted
                ? game.usePower(power, tx, ty)
                : game.usePower(power);
        if (!done) {
            showHint("تحتاج " + power.getGoldCost() + " ذهبًا لتفعيل «"
                    + arabicPower(power) + "».");
            return;
        }
        islandView.playEffect(effectForPower(power), tx, ty);
        showHint(arabicPower(power) + " — " + arabicPowerDescription(power)
                + "\nالتبريد: " + power.getCooldownDays() + " يومًا.");
        refreshUi();
    }

    /** Visual effect paired with each power; global powers still get a centre flash. */
    private static IslandView.Effect effectForPower(Power power) {
        if (power == null) {
            return IslandView.Effect.FIRE;
        }
        switch (power) {
            case RAIN:
                return IslandView.Effect.RAIN;
            case BLOOM:
                return IslandView.Effect.BLOOM;
            case HEAL:
                return IslandView.Effect.HEAL;
            case BLESS:
            case PROSPERITY:
                return IslandView.Effect.BLESS;
            case PEACE:
                return IslandView.Effect.PEACE;
            case METEOR:
                return IslandView.Effect.METEOR;
            case LIGHTNING:
            case PLAGUE:
            case EARTHQUAKE:
            default:
                return IslandView.Effect.FIRE;
        }
    }

    /** Disasters have no coordinates in core, so the visual lands on the chosen tile. */
    private static IslandView.Effect effectForDisaster(DisasterType type) {
        if (type == null) {
            return IslandView.Effect.FIRE;
        }
        switch (type.name()) {
            case "METEORITE":
            case "ALIEN_VESSLE":
            case "LAVA_FLOW":
                return IslandView.Effect.METEOR;
            case "ACID_RAIN":
                return IslandView.Effect.RAIN;
            case "BANDIT_RAID":
            case "HELLSPAWN":
            case "NECROMANCER_RISE":
            case "WILD_STAMPEDE":
                return IslandView.Effect.WAR;
            default:
                return IslandView.Effect.FIRE;
        }
    }

    // ------------------------------------------------------------------
    // Nature page: template, mods, 23 laws, 19 biomes, 33 resources
    // ------------------------------------------------------------------

    private void setupNatureStatic() {
        natureList.removeAllViews();
        templateText = addLabel(natureList, "", 12f, 0xFFFCA311);
        StringBuilder templates = new StringBuilder("كل القوالب: ");
        for (MapTemplate template : MapTemplate.values()) {
            if (templates.length() > 12) {
                templates.append(" • ");
            }
            templates.append(template.getArabicDisplayName());
        }
        addLabel(natureList, templates.toString(), 10f, 0xFFB0BEC5);

        addSection(natureList, "نمط اللعبة — اختر مجموعة القواعد");
        modPackText = addLabel(natureList, "", 11f, 0xFFFFF3E0);
        GridLayout modGrid = grid(natureList, 3);
        int[] modColors = {0xFF66BB6A, 0xFF42A5F5, 0xFFAB47BC};
        ModPack[] packs = ModPack.values();
        for (int i = 0; i < packs.length; i++) {
            final ModPack pack = packs[i];
            ImageButton button = smallButton(PixelIcons.techIcon(this, modColors[i % modColors.length]),
                    pack.getDisplayNameAr(), v -> {
                        game.setModPack(pack);
                        showHint("نمط «" + pack.getDisplayNameAr() + "»: " + pack.getDescription());
                        refreshUi();
                    });
            button.setLayoutParams(gridParams(i, 3));
            modGrid.addView(button);
            modPackButtons[i] = button;
        }

        addSection(natureList, "قوانين العالم — مفعّل / معطّل (" + WorldLaw.values().length + ")");
        GridLayout lawGrid = grid(natureList, 2);
        WorldLaw[] laws = WorldLaw.values();
        for (int i = 0; i < laws.length; i++) {
            final WorldLaw law = laws[i];
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.HORIZONTAL);
            cell.setLayoutParams(gridParams(i, 2));
            ImageButton toggle = smallButton(PixelIcons.lawIcon(this, world().isLawEnabled(law)),
                    law.getArabicLabel(), v -> {
                        game.toggleLaw(law);
                        boolean on = world().isLawEnabled(law);
                        showHint("قانون «" + law.getArabicLabel() + "» "
                                + (on ? "مفعّل الآن — سيؤثر على حياة الجزيرة."
                                      : "معطّل الآن — توقفت آثاره."));
                        refreshUi();
                    });
            cell.addView(toggle);
            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, dp(30), 1f));
            name.setText(law.getArabicLabel());
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(11f);
            cell.addView(name);
            lawGrid.addView(cell);
            lawButtons[i] = toggle;
        }

        addSection(natureList, "الأحياء الحيوية — " + Biome.values().length + " نوعًا");
        GridLayout biomeGrid = grid(natureList, 4);
        Biome[] biomes = Biome.values();
        for (int i = 0; i < biomes.length; i++) {
            final Biome biome = biomes[i];
            LinearLayout cell = swatchCell(PixelIcons.biomeSwatch(this, biome.getColor()),
                    biome.getArabicDisplayName(), null);
            cell.setLayoutParams(gridParams(i, 4));
            cell.setOnClickListener(v -> showHint("حيوية «" + biome.getArabicDisplayName()
                    + "» — لونها على الخريطة، وتغيّر الغذاء والنمو."));
            biomeGrid.addView(cell);
        }

        addSection(natureList, "الموارد — " + ResourceType.values().length + " مادة");
        GridLayout resourceGrid = grid(natureList, 3);
        ResourceType[] resources = ResourceType.values();
        for (int i = 0; i < resources.length; i++) {
            final ResourceType resource = resources[i];
            TextView amount = new TextView(this);
            amount.setTextColor(0xFFFFF3E0);
            amount.setTextSize(9f);
            LinearLayout cell = swatchCell(PixelIcons.resourceSwatch(this, resource.getColor()),
                    resource.getArabicDisplayName(), amount);
            cell.setLayoutParams(gridParams(i, 3));
            cell.setOnClickListener(v -> showHint("مادة «" + resource.getArabicDisplayName()
                    + "» — المخزون " + (int) world().getResource(resource) + "."));
            resourceGrid.addView(cell);
            resourceAmounts[i] = amount;
        }
    }

    private void refreshNature() {
        World world = world();
        StringBuilder sig = new StringBuilder();
        sig.append(world.getDay()).append('|').append(game.getModPack().name()).append('|');
        for (WorldLaw law : WorldLaw.values()) {
            sig.append(world.isLawEnabled(law) ? '1' : '0');
        }
        for (ResourceType type : ResourceType.values()) {
            sig.append((int) world.getResource(type)).append(',');
        }
        if (!changed("nature", sig.toString())) {
            return;
        }
        MapTemplate template = world.getMapTemplate();
        templateText.setText("الخريطة الحالية: " + template.getArabicDisplayName()
                + " — " + template.getDescription());
        modPackText.setText("النمط الحالي: " + game.getModPack().getDisplayNameAr());
        ModPack[] packs = ModPack.values();
        for (int i = 0; i < packs.length; i++) {
            modPackButtons[i].setAlpha(game.getModPack() == packs[i] ? 1f : 0.5f);
        }
        WorldLaw[] laws = WorldLaw.values();
        for (int i = 0; i < laws.length; i++) {
            boolean on = world.isLawEnabled(laws[i]);
            lawButtons[i].setImageDrawable(PixelIcons.lawIcon(this, on));
            lawButtons[i].setAlpha(on ? 1f : 0.42f);
        }
        ResourceType[] resources = ResourceType.values();
        for (int i = 0; i < resources.length; i++) {
            resourceAmounts[i].setText("×" + (int) world.getResource(resources[i]));
        }
    }

    // ------------------------------------------------------------------
    // Creatures page
    // ------------------------------------------------------------------

    private void setupCreaturesStatic() {
        creatureList.removeAllViews();
        AnimalType[] types = AnimalType.values();
        for (int i = 0; i < types.length; i++) {
            final AnimalType type = types[i];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(5, 5, 5, 5);
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(PixelIcons.biomeSwatch(this, type.getColor()));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setContentDescription(arabicAnimal(type));
            row.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, dp(30), 1f));
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(12f);
            row.addView(name);

            TextView count = new TextView(this);
            count.setTextColor(0xFFFFF3E0);
            count.setTextSize(12f);
            row.addView(count);
            creatureCounts[i] = count;

            ImageButton spawn = smallButton(PixelIcons.control(this, PixelIcons.Control.NEXT),
                    "إطلاق " + arabicAnimal(type), v -> {
                        game.spawnCreature(type);
                        showHint("ظهر «" + arabicAnimal(type) + "» في الجزيرة — "
                                + (type.isDangerous() ? "احذر من هجومه على السكان."
                                   : type.isWater() ? "سيعيش في الماء القريب."
                                   : "سيجوب الغابات والسهول."));
                        refreshUi();
                    });
            row.addView(spawn);
            name.setText(arabicAnimal(type));
            creatureList.addView(row);
        }
    }

    private void refreshCreatures() {
        int[] counts = new int[AnimalType.values().length];
        for (com.dotee313.omatydom.core.world.Animal animal : game.getAnimals()) {
            if (animal.isAlive()) {
                counts[animal.getType().ordinal()]++;
            }
        }
        StringBuilder sig = new StringBuilder();
        for (int count : counts) {
            sig.append(count).append(',');
        }
        if (!changed("creatures", sig.toString())) {
            return;
        }
        for (int i = 0; i < counts.length; i++) {
            creatureCounts[i].setText("×" + counts[i]);
        }
    }

    // ------------------------------------------------------------------
    // Kingdoms page
    // ------------------------------------------------------------------

    private void setupKingdomActions() {
        ImageButton clan = findViewById(R.id.foundClanButton);
        clan.setImageDrawable(PixelIcons.clanIcon(this));
        clan.setContentDescription("تأسيس عشيرة");
        clan.setOnClickListener(v -> {
            int id = actorCitizenId();
            if (id < 0) {
                showHint("لا يوجد سكان بالغون لتأسيس عشيرة منهم.");
                return;
            }
            game.foundClan(id);
            showHint("تأسست عشيرة جديدة بقيادة " + citizenName(id)
                    + " — تظهر الآن في قائمة العشائر.");
            refreshUi();
        });

        ImageButton kingdom = findViewById(R.id.foundKingdomButton);
        kingdom.setImageDrawable(PixelIcons.kingdomIcon(this));
        kingdom.setContentDescription("تأسيس مملكة");
        kingdom.setOnClickListener(v -> {
            int id = actorCitizenId();
            if (id < 0) {
                showHint("لا يوجد سكان بالغون لتأسيس مملكة منهم.");
                return;
            }
            game.foundKingdom(id);
            showHint("قامت مملكة جديدة بقيادة " + citizenName(id)
                    + " — لونها سيظهر في طبقة الممالك.");
            refreshUi();
        });

        ImageButton research = findViewById(R.id.researchButton);
        research.setImageDrawable(PixelIcons.techIcon(this, 0xFF26C6DA));
        research.setContentDescription("بحث تقني");
        research.setOnClickListener(v -> {
            Technology next = game.getNextTechnology();
            if (next == null) {
                showHint("اكتشفت الجزيرة كل التقنيات السبعين.");
                return;
            }
            boolean started = game.researchNextTechnology();
            showHint(started
                    ? "بدأ البحث في «" + next.getArabicName() + "» — التكلفة "
                            + next.getCost() + " معرفة."
                    : "لا ثقافة تقود البحث في الجزيرة بعد.");
            refreshUi();
        });

        ImageButton war = findViewById(R.id.warButton);
        war.setImageDrawable(PixelIcons.disasterIcon(this, 0xFFE53935));
        war.setContentDescription("إعلان حرب غزو");
        war.setOnClickListener(v -> startWar(WarType.CONQUEST));

        WarType[] types = WarType.values();
        for (int i = 0; i < types.length; i++) {
            final WarType type = types[i];
            ImageButton button = smallButton(PixelIcons.disasterIcon(this, warColor(i)),
                    type.getArabicDisplayName(), v -> startWar(type));
            button.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f));
            warTypeRow.addView(button);
        }
    }

    private void startWar(WarType type) {
        List<Kingdom> kingdoms = game.getKingdoms();
        if (kingdoms.size() < 2) {
            showHint("تحتاج مملكتين على الأقل لإعلان حرب «"
                    + type.getArabicDisplayName() + "».");
            return;
        }
        Kingdom attacker = kingdoms.get(0);
        Kingdom defender = kingdoms.get(kingdoms.size() - 1);
        game.startWar(type, attacker.getId(), defender.getId());
        showHint("بدأت حرب «" + type.getArabicDisplayName() + "» بين "
                + attacker.getName() + " و" + defender.getName() + ".");
        refreshUi();
    }

    private void refreshKingdoms() {
        // Trade progress can change without changing the kingdom signature below.
        refreshTradeRoutes();
        List<Kingdom> kingdoms = game.getKingdoms();
        List<Clan> clans = game.getClans();
        List<Culture> cultures = game.getCultures();
        List<War> wars = game.getWars();
        World world = world();
        StringBuilder sig = new StringBuilder();
        sig.append(kingdoms.size()).append('/').append(clans.size()).append('/')
                .append(cultures.size()).append('/').append(wars.size()).append('|');
        for (Kingdom k : kingdoms) {
            sig.append(k.getId()).append(':').append((int) k.getStability()).append(':')
                    .append(k.getArmy()).append(':').append(k.getTreasury()).append(';');
        }
        for (Clan c : clans) {
            sig.append(c.getId()).append(':').append(c.getInfluence()).append(';');
        }
        for (War w : wars) {
            sig.append(w.getId()).append(':').append((int) (w.getIntensity() * 10)).append(';');
        }
        if (!cultures.isEmpty()) {
            sig.append((int) cultures.get(0).getKnowledge());
        }
        if (!changed("kingdoms", sig.toString())) {
            return;
        }

        kingdomList.removeAllViews();
        for (Culture culture : cultures) {
            addTextRow(kingdomList, PixelIcons.techIcon(this, 0xFFCE93D8),
                    "ثقافة «" + culture.getName() + "» • معرفة " + (int) culture.getKnowledge()
                            + " • تقنيات " + culture.getTechIds().size(),
                    "ثقافة " + culture.getName() + " تحفظ المعرفة وتكتشف التقنيات.", 1f);
        }
        for (Kingdom kingdom : kingdoms) {
            addTextRow(kingdomList, PixelIcons.kingdomIcon(this),
                    "مملكة «" + kingdom.getName() + "» • " + kingdom.getSpecies().getArabicDisplayName()
                            + " • استقرار " + (int) kingdom.getStability()
                            + " • جيش " + kingdom.getArmy()
                            + " • خزانة " + kingdom.getTreasury(),
                    "مملكة " + kingdom.getName() + " — عاصمتها عند (" + kingdom.getCapitalX()
                            + "," + kingdom.getCapitalY() + ") وتضم " + kingdom.getClanIds().size()
                            + " عشيرة.", 1f);
        }
        for (War war : wars) {
            addTextRow(kingdomList, PixelIcons.disasterIcon(this, 0xFFE53935),
                    "حرب «" + war.getType().getArabicDisplayName() + "» • شدّة "
                            + String.format(Locale.getDefault(), "%.1f", war.getIntensity())
                            + " • منذ اليوم " + war.getStartDay(),
                    war.getType().getDescription(), 1f);
        }
        if (kingdoms.isEmpty() && cultures.isEmpty() && wars.isEmpty()) {
            addLabel(kingdomList, "لا ممالك بعد — اضغط أيقونة التأسيس في الأعلى.", 11f, 0xFFB0BEC5);
        }

        clanList.removeAllViews();
        for (Clan clan : clans) {
            addTextRow(clanList, PixelIcons.clanIcon(this),
                    "عشيرة «" + clan.getName() + "» • نفوذ " + clan.getInfluence()
                            + " • أعضاء " + clan.getMemberIds().size(),
                    "عشيرة " + clan.getName() + " تضم " + clan.getMemberIds().size()
                            + " عضوًا ونفوذها " + clan.getInfluence() + ".", 1f);
        }
        addLabel(clanList, "إحصاء: عشائر " + world.getClansFounded()
                + " • ممالك " + world.getKingdomsFounded()
                + " • حروب " + world.getWarsStarted(), 10f, 0xFFFFF3E0);

        nextTechText = addLabel(kingdomList, "", 11f, 0xFFFCA311);
        Technology next = game.getNextTechnology();
        nextTechText.setText(next == null
                ? "التقنيات: اكتملت كل الاكتشافات."
                : "التقنية التالية: " + next.getArabicName() + " — "
                        + next.getCategory().getArabicName() + " • تكلفة " + next.getCost());
    }

    /** One Arabic line on the kingdoms page: route count, active count and progress. */
    private void refreshTradeRoutes() {
        List<TradeRoute> routes = game.getTradeRoutes();
        if (routes == null) {
            return;
        }
        StringBuilder sig = new StringBuilder().append(routes.size()).append('|');
        int active = 0;
        String progress = "—";
        for (TradeRoute route : routes) {
            if (route == null) {
                sig.append("null;");
                continue;
            }
            if (route.isActive()) {
                active++;
            }
            progress = String.format(Locale.getDefault(), "%.0f%%", route.getProgress() * 100);
            sig.append(route.getId()).append(':')
                    .append(route.isActive() ? '1' : '0').append(':')
                    .append(progress).append(';');
        }
        sig.append('|').append(active);
        if (!changed("routes", sig.toString())) {
            return;
        }
        tradeRouteText.setText("طرق التجارة: " + routes.size()
                + " • النشطة " + active + " • آخر تقدّم " + progress);
    }

    private void refreshTechList() {
        Set<Technology> unlocked = new HashSet<>(game.getUnlockedTechnologies());
        Technology next = game.getNextTechnology();
        World world = world();
        StringBuilder sig = new StringBuilder();
        sig.append(world.getTechsUnlocked()).append('|');
        if (!world.getCultures().isEmpty()) {
            sig.append((int) world.getCultures().get(0).getKnowledge());
        }
        sig.append('|').append(next == null ? "none" : next.name()).append('|');
        for (Technology tech : unlocked) {
            sig.append(tech.name()).append(',');
        }
        if (!changed("techs", sig.toString())) {
            return;
        }
        techList.removeAllViews();
        for (Technology tech : Technology.values()) {
            boolean known = unlocked.contains(tech);
            boolean upcoming = tech == next;
            String status = known ? "مكتشفة" : upcoming ? "التالية" : "مغلقة";
            addTextRow(techList, PixelIcons.techIcon(this, categoryColor(tech.getCategory())),
                    tech.getArabicName() + " • " + tech.getCategory().getArabicName()
                            + " • " + tech.getCost() + " • " + status,
                    "تقنية «" + tech.getArabicName() + "» من فرع "
                            + tech.getCategory().getArabicName() + " — " + status + ".",
                    known ? 1f : upcoming ? 0.85f : 0.42f);
        }
    }

    // ------------------------------------------------------------------
    // Powers page
    // ------------------------------------------------------------------

    private void refreshPowers() {
        if (!changed("powers", String.valueOf(world().getDay()))) {
            return;
        }
        Power[] powers = Power.values();
        for (int i = 0; i < powerGrid.getChildCount() && i < powers.length; i++) {
            int cooldown = game.getPowerCooldownDaysLeft(powers[i]);
            powerGrid.getChildAt(i).setAlpha(cooldown > 0 ? 0.45f : 1f);
        }
    }

    // ------------------------------------------------------------------
    // Events page
    // ------------------------------------------------------------------

    private void setupAchievements() {
        achievementList.removeAllViews();
        Achievement[] achievements = Achievement.values();
        for (int i = 0; i < achievements.length; i++) {
            final Achievement achievement = achievements[i];
            ImageButton button = smallButton(PixelIcons.techIcon(this, achievementColor(i)),
                    achievement.getDisplayNameAr(), v -> showHint("إنجاز «"
                            + achievement.getDisplayNameAr() + "»: "
                            + achievement.getDescription() + " (الهدف " + achievement.getTarget() + ")"));
            button.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
            achievementList.addView(button);
            achievementButtons[i] = button;
        }
    }

    private void refreshEvents() {
        World world = world();
        List<GameEvent> recent = world.getRecentEvents(60);
        StringBuilder sig = new StringBuilder();
        sig.append(world.getDay()).append('|').append(recent.size()).append('|')
                .append(world.getAchievements().size()).append('|');
        if (!recent.isEmpty()) {
            sig.append(recent.get(0).getTitle()).append(recent.get(0).getDay());
        }
        if (!changed("events", sig.toString())) {
            return;
        }

        int year = world.getDay() / GameConfig.DAYS_PER_YEAR + 1;
        int month = OmatydomGame.monthOf(world.getDay());
        String season = SEASONS[OmatydomGame.seasonOf(world.getDay())];
        int expecting = 0;
        for (Citizen citizen : world.getLivingCitizens()) {
            if (citizen.isPregnant()) {
                expecting++;
            }
        }
        statText.setText(String.format(Locale.getDefault(),
                "يوم %d • سنة %d • شهر %d • %s • %s%n"
                        + "سكان %d • حوامل %d • استقرار %.0f • ذهب %.0f • طعام %.0f%n"
                        + "خشب %.0f • حجر %.0f • خام %.0f • بلور %.0f%n"
                        + "مواليد %d • وفيات %d • مبانٍ %d • قدرات %d%n"
                        + "حروب %d • تقنيات %d • عشائر %d • ممالك %d%n"
                        + "قوانين %d/%d • إنجازات %d/%d • نمط %s",
                world.getDay(), year, month, season, arabicWeather(world.getWeather()),
                world.getPopulation(), expecting, world.getStability(), world.getGold(),
                world.getFood(), world.getWood(), world.getStone(), world.getOre(),
                world.getCrystal(), world.getBirths(), world.getDeaths(),
                world.getStructuresBuilt(), world.getPowersUsed(), world.getWarsStarted(),
                world.getTechsUnlocked(), world.getClansFounded(), world.getKingdomsFounded(),
                world.getLaws().size(), WorldLaw.values().length,
                world.getAchievements().size(), Achievement.values().length,
                game.getModPack().getDisplayNameAr()));

        Set<Achievement> unlocked = world.getAchievements();
        Achievement[] achievements = Achievement.values();
        for (int i = 0; i < achievements.length; i++) {
            achievementButtons[i].setAlpha(unlocked.contains(achievements[i]) ? 1f : 0.3f);
        }
        eventAdapter.clear();
        for (GameEvent event : recent) {
            eventAdapter.add(event);
        }
        eventAdapter.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------
    // Per-tick refresh, signatures and people
    // ------------------------------------------------------------------

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

    /** Rebuild a list only when its state signature changes, not every 500 ms. */
    private boolean changed(String key, String signature) {
        String old = signatures.get(key);
        if (signature.equals(old)) {
            return false;
        }
        signatures.put(key, signature);
        return true;
    }

    private void refreshCurrentTab() {
        switch (currentTab) {
            case 1: refreshPeopleList(); break;
            case 2: refreshNature(); break;
            case 3: refreshCreatures(); break;
            case 4: refreshKingdoms(); refreshTechList(); break;
            case 5: refreshPowers(); break;
            case 6: refreshEvents(); break;
            default: break;
        }
    }

    private World world() {
        return game.getWorld();
    }

    /** Six badges are created once; only their numbers change on each tick. */
    private void updateResourceStrip(World world) {
        for (int i = 0; i < STRIP_RESOURCES.length && i < resourceStripCounts.length; i++) {
            resourceStripCounts[i].setText("×" + (int) world.getResource(STRIP_RESOURCES[i]));
        }
    }

    private void refreshUi() {
        World world = world();
        int day = world.getDay();
        int year = day / GameConfig.DAYS_PER_YEAR + 1;
        int month = OmatydomGame.monthOf(day);
        String season = SEASONS[OmatydomGame.seasonOf(day)];

        statusView.setText(String.format(Locale.getDefault(),
                "اليوم %d • السنة %d • الشهر %d • %s%nالسكان %d • الاستقرار %.0f/100",
                day, year, month, season, world.getPopulation(), world.getStability()));
        updateResourceStrip(world);

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
                    world.getRuler().getCitizen().getName(), world.getRuler().getApproval(),
                    game.isPlayerControlled() ? "تحت إدارتك" : "يتصرف وحده", expecting));
        }

        Age age = game.getAge();
        seasonView.setText(String.format(Locale.getDefault(),
                "%s • %s • %s • الشهر %d/12 • يوم %d/%d%nمواليد %d • وفيات %d • مبانٍ %d • قدرات %d",
                arabicAge(age), arabicWeather(world.getWeather()), season, month,
                day % GameConfig.DAYS_PER_YEAR == 0
                        ? GameConfig.DAYS_PER_YEAR : day % GameConfig.DAYS_PER_YEAR,
                GameConfig.DAYS_PER_YEAR, world.getBirths(), world.getDeaths(),
                world.getStructuresBuilt(), world.getPowersUsed()));
        dayProgress.setProgress((int) (game.getDayProgress() * 100));

        islandView.invalidate();
        candidates.clear();
        candidates.addAll(game.getRulerCandidates());
        refreshCurrentTab();
    }

    private void refreshPeopleList() {
        StringBuilder sig = new StringBuilder();
        for (Citizen citizen : candidates) {
            sig.append(citizen.getId()).append(':').append(citizen.getJob()).append(':')
                    .append(citizen.isPregnant()).append(':').append((int) citizen.getHealth())
                    .append(';');
        }
        if (!changed("people", sig.toString())) {
            return;
        }
        peopleList.removeAllViews();

        int portraitSize = dp(42);
        for (final Citizen citizen : candidates) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(5, 5, 5, 5);
            row.setBackgroundResource(R.drawable.bg_panel);

            ImageView portrait = new ImageView(this);
            portrait.setLayoutParams(new LinearLayout.LayoutParams(portraitSize, portraitSize));
            portrait.setImageDrawable(PixelIcons.portrait(this, citizen));
            portrait.setAdjustViewBounds(false);
            portrait.setScaleType(ImageView.ScaleType.FIT_CENTER);
            portrait.setContentDescription("صورة " + citizen.getName());
            row.addView(portrait);

            TextView info = new TextView(this);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            info.setTextColor(0xFFFFFFFF);
            info.setTextSize(11f);
            String status = citizen.isPregnant()
                    ? "حامل • يوم " + citizen.getGestationDay(world().getDay())
                            + "/" + GameConfig.GESTATION_DAYS
                    : arabicJob(citizen.getJob()) + " • " + citizen.getAgeYears() + " سنة";
            info.setText(citizen.getName() + "\n" + status + " • " + arabicTrait(citizen.getTrait())
                    + " • " + citizen.getSpecies().getArabicDisplayName());
            row.addView(info);

            row.setOnClickListener(v -> appointRuler(citizen));
            peopleList.addView(row);
        }
    }

    /** Shared by the People row and the citizen card, so both behave identically. */
    private void appointRuler(Citizen citizen) {
        if (citizen == null) {
            return;
        }
        selectedCitizenId = citizen.getId();
        islandView.selectAgent(citizen.getId());
        if (game.chooseRuler(citizen.getId())) {
            showHint("تم تعيين " + citizen.getName() + " حاكمًا للجزيرة.");
        } else {
            showHint("لا يمكن تعيين هذا الشخص — يجب أن يكون بالغًا وحيًا.");
        }
        refreshUi();
    }

    private int actorCitizenId() {
        if (selectedCitizenId >= 0 && world().findCitizen(selectedCitizenId) != null) {
            return selectedCitizenId;
        }
        return candidates.isEmpty() ? -1 : candidates.get(0).getId();
    }

    private String citizenName(int id) {
        Citizen citizen = world().findCitizen(id);
        return citizen == null ? "أحد السكان" : citizen.getName();
    }

    // ------------------------------------------------------------------
    // Small view factories
    // ------------------------------------------------------------------

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView addLabel(LinearLayout parent, String text, float size, int color) {
        TextView view = new TextView(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        parent.addView(view);
        return view;
    }

    private void addSection(LinearLayout parent, String text) {
        TextView view = addLabel(parent, text, 12f, 0xFFFCA311);
        view.setPadding(0, dp(6), 0, dp(2));
    }

    private GridLayout grid(LinearLayout parent, int columns) {
        GridLayout grid = new GridLayout(this);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        grid.setColumnCount(columns);
        parent.addView(grid);
        return grid;
    }

    private GridLayout.LayoutParams gridParams(int index, int columns) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(index / columns), GridLayout.spec(index % columns));
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        return params;
    }

    private GridLayout.LayoutParams gridCellParams(int index, int columns, int width, int height) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(index / columns), GridLayout.spec(index % columns));
        params.width = width;
        params.height = height;
        return params;
    }

    private ImageButton smallButton(Drawable icon, String description,
                                    View.OnClickListener listener) {
        ImageButton button = new ImageButton(this);
        button.setBackgroundResource(R.drawable.bg_button);
        button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        button.setPadding(2, 2, 2, 2);
        button.setImageDrawable(icon);
        button.setContentDescription(description);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout swatchCell(Drawable icon, String label, TextView amount) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        cell.setPadding(2, 3, 2, 3);
        ImageView image = new ImageView(this);
        image.setImageDrawable(icon);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription(label + " — اضغط للشرح");
        cell.addView(image, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(0xFFFFFFFF);
        text.setTextSize(9f);
        cell.addView(text);
        if (amount != null) {
            cell.addView(amount);
        }
        return cell;
    }

    private void addTextRow(LinearLayout parent, Drawable icon, String text,
                            String hint, float alpha) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(4, 4, 4, 4);
        row.setBackgroundResource(R.drawable.bg_panel);
        row.setAlpha(alpha);
        ImageView image = new ImageView(this);
        image.setImageDrawable(icon);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription("أيقونة " + text);
        row.addView(image, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView label = new TextView(this);
        label.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        label.setText(text);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(10f);
        row.addView(label);
        row.setOnClickListener(v -> showHint(hint));
        parent.addView(row);
    }

    private static int achievementColor(int index) {
        return Color.rgb(100 + (index * 47) % 150,
                110 + (index * 71) % 140, 120 + (index * 97) % 130);
    }

    private static int warColor(int index) {
        return Color.rgb(180 + (index * 13) % 70,
                70 + (index * 37) % 130, 60 + (index * 53) % 110);
    }

    private static int categoryColor(TechCategory category) {
        switch (category) {
            case CRAFT: return 0xFFB0BEC5;
            case BUILD: return 0xFFFFB74D;
            case FARM: return 0xFF9CCC65;
            case MINING: return 0xFF90A4AE;
            case MILITARY: return 0xFFEF5350;
            case GOVERN: return 0xFFFCA311;
            case CULTURE: return 0xFFCE93D8;
            case TRADE: return 0xFF4FC3F7;
            case LORE:
            default: return 0xFF66BB6A;
        }
    }

    // ------------------------------------------------------------------
    // Explanations and Arabic labels
    // ------------------------------------------------------------------

    /** Programmatic Arabic card; the simulation keeps ticking while it is open. */
    private void openCitizenInfo(Citizen citizen) {
        if (citizen == null) {
            return;
        }
        String subtitle = citizen.getAgeYears() + " سنة • " + arabicGender(citizen.getGender())
                + " • " + arabicJob(citizen.getJob());
        CitizenInfoDialog.show(this, citizen, subtitle, citizenDetails(citizen),
                appointed -> appointRuler(appointed));
    }

    /** Real values from Citizen / Agent / civ models; no invented family data. */
    private String citizenDetails(Citizen citizen) {
        World world = world();
        StringBuilder details = new StringBuilder();
        details.append("المهنة: ").append(arabicJob(citizen.getJob()))
                .append(" • الطبقة: ").append(arabicSocialClass(citizen.getSocialClass())).append('\n');
        details.append("العرق: ").append(citizen.getRace().getDisplayName())
                .append(" — ").append(citizen.getRace().getDescription()).append('\n');
        details.append("النوع: ").append(citizen.getSpecies().getArabicDisplayName()).append('\n');
        details.append("الديانة: ").append(arabicReligion(citizen.getReligion())).append('\n');
        details.append("الأيديولوجيا: ").append(arabicIdeology(citizen.getIdeology())).append('\n');
        details.append("الصحة: ").append((int) citizen.getHealth())
                .append("/100 • السعادة: ").append((int) citizen.getHappiness())
                .append("/100 • الثروة: ").append((int) citizen.getWealth()).append("/100\n");
        details.append("ذهب الجزيرة: ").append((int) world.getGold()).append('\n');

        String kingdom = kingdomOf(citizen);
        details.append("المملكة: ").append(kingdom == null ? "لا مملكة بعد" : kingdom).append('\n');

        Agent agent = world.findAgent(citizen);
        if (agent == null) {
            details.append("الموقع: لا يظهر في الخريطة الآن.\n");
        } else {
            Tile tile = world.getMap().get(agent.getX(), agent.getY());
            details.append("الموقع: (").append(agent.getX()).append(',').append(agent.getY())
                    .append(") ").append(tile == null ? "خارج الخريطة" : arabicTile(tile.getType()));
            if (agent.hasTarget()) {
                details.append(" • يتجه إلى (").append(agent.getTargetX()).append(',')
                        .append(agent.getTargetY()).append(')');
            }
            details.append('\n');
        }

        details.append("الحالة الاجتماعية: ").append(arabicSocialClass(citizen.getSocialClass()));
        if (citizen.isPregnant()) {
            details.append(" • حامل في يوم ")
                    .append(citizen.getGestationDay(world.getDay())).append('/')
                    .append(GameConfig.GESTATION_DAYS)
                    .append(" • الراحة بعد الولادة ").append(GameConfig.MIN_BIRTH_SPACING_DAYS)
                    .append(" يومًا");
        } else {
            details.append(" • غير حامل • فاصل الولادة ")
                    .append(GameConfig.MIN_BIRTH_SPACING_DAYS)
                    .append(" يومًا • الزواج غير مسجّل في هذه النسخة");
        }
        details.append('\n');

        List<StatusEffect> effects = citizen.getActiveStatuses();
        if (effects.isEmpty()) {
            details.append("الأحوال المؤقتة: لا شيء.");
        } else {
            details.append("الأحوال المؤقتة: ");
            for (int i = 0; i < effects.size(); i++) {
                StatusEffect effect = effects.get(i);
                if (i > 0) {
                    details.append("، ");
                }
                details.append(effect.getDisplayNameAr()).append(' ')
                        .append(citizen.getStatusDays(effect)).append(" يوم");
            }
            details.append('.');
        }
        details.append("\nالسمة: ").append(arabicTrait(citizen.getTrait()));
        return details.toString();
    }

    /** Kingdom is derived through the citizen's clan; clans may exist before kingdoms. */
    private String kingdomOf(Citizen citizen) {
        List<Clan> clans = game.getClans();
        List<Kingdom> kingdoms = game.getKingdoms();
        for (Clan clan : clans) {
            if (clan.getChiefId() != citizen.getId()
                    && !clan.getMemberIds().contains(citizen.getId())) {
                continue;
            }
            for (Kingdom kingdom : kingdoms) {
                if (kingdom.getId() == clan.getKingdomId()
                        || kingdom.getClanIds().contains(clan.getId())) {
                    return "«" + kingdom.getName() + "» • عشيرة «" + clan.getName() + "»";
                }
            }
            return "عشيرة «" + clan.getName() + "» بلا مملكة";
        }
        return null;
    }

    private String citizenExplanation(Citizen citizen) {
        String state = citizen.isPregnant()
                ? "حامل في يوم " + citizen.getGestationDay(world().getDay())
                        + " من " + GameConfig.GESTATION_DAYS + " — ستلد بعد ثلاثة فصول."
                : arabicJob(citizen.getJob()) + " • " + citizen.getAgeYears() + " سنة • صحة "
                        + (int) citizen.getHealth() + " • سعادة " + (int) citizen.getHappiness();
        StringBuilder effects = new StringBuilder();
        for (StatusEffect effect : citizen.getActiveStatuses()) {
            if (effects.length() > 0) {
                effects.append("، ");
            }
            effects.append(effect.getDisplayNameAr()).append(' ')
                    .append(citizen.getStatusDays(effect)).append(" يوم");
        }
        return citizen.getName() + " — " + citizen.getSpecies().getArabicDisplayName()
                + " • " + citizen.getRace().getDescription() + "\n" + state + " • "
                + arabicTrait(citizen.getTrait())
                + (effects.length() > 0 ? "\nأحوال: " + effects : "")
                + ". اضغط «تعيين حاكم» في بطاقته أو في تبويب السكان.";
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
        Biome biome = world().biomeAt(x, y);
        return name + " (" + x + "," + y + "): " + detail
                + " الحيوية: " + (biome == null ? "غير معروفة" : biome.getArabicDisplayName()) + ".";
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
            case BLACKSMITH: return "حدّاد";
            case SETTLER: return "مستوطن";
            case ROAD_BUILDER: return "مهندس طرق";
            case CLEANER: return "عامل نظافة";
            case FIREMAN: return "رجال إطفاء";
            case ATTACKER: return "مقاتل";
            case LABORER:
            default: return "عامل";
        }
    }

    private static String arabicGender(Gender gender) {
        if (gender == Gender.FEMALE) {
            return "أنثى";
        }
        return "ذكر";
    }

    private static String arabicReligion(Religion religion) {
        if (religion == null) {
            return "لا ديانة";
        }
        switch (religion) {
            case SOLARISM: return "عبادة الشمس";
            case ANCESTOR_WORSHIP: return "عبادة الأجداد";
            case NATURE_CULT: return "عبادة الطبيعة";
            case THE_ONE: return "الإله الواحد";
            case NONE:
            default: return "لا ديانة";
        }
    }

    private static String arabicIdeology(Ideology ideology) {
        if (ideology == null) {
            return "لا رأي سياسي";
        }
        switch (ideology) {
            case MONARCHIST: return "مَلَكي";
            case DEMOCRAT: return "ديمقراطي";
            case THEOCRAT: return "ثيقراطي";
            case TRADITIONALIST: return "تقليدي";
            case EGALITARIAN: return "مساواتي";
            case NATIONALIST: return "قومي";
            case APATHETIC:
            default: return "لا مبالٍ";
        }
    }

    private static String arabicSocialClass(SocialClass socialClass) {
        if (socialClass == null) {
            return "عامل";
        }
        switch (socialClass) {
            case CHILD: return "طفل";
            case ARTISAN: return "حِرَفي";
            case CLERGY: return "رجال دين";
            case NOBLE: return "نبيل";
            case LABORER:
            default: return "عامل";
        }
    }

    private static String arabicAnimal(AnimalType type) {
        switch (type) {
            case DEER: return "أيل";
            case BOAR: return "خنزير بري";
            case FISH: return "سمك";
            case WOLF: return "ذئب";
            case WORM: return "دودة رملية";
            case GOLEM: return "غول بلوري";
            case ZOMBIE: return "هائم";
            case DRAGON:
            default: return "تنين صغير";
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

    private static String arabicWeather(Weather weather) {
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
            case HALL: return "قاعة العشيرة";
            case TEMPLE: return "معبد";
            case LIBRARY: return "مكتبة";
            case TOWER: return "برج مراقبة";
            case BARRACKS: return "ثكنات";
            case DOCK: return "رصيف";
            case BRIDGE: return "جسر";
            case MINE: return "منجم";
            case SMITHY: return "حدّاد";
            case FARM: return "مزرعة";
            case GRANARY: return "مخزن حبوب";
            case WINDMILL: return "طاحونة ريح";
            case WELL: return "بئر";
            case MARKET: return "سوق";
            case ROAD: return "طريق";
            case GARDEN: return "حديقة";
            case WAREHOUSE: return "مستودع";
            case PALACE: return "قصر";
            case NONE:
            default: return "أرض فارغة";
        }
    }
}
