# Omatydom

> A living island of kingdoms, clans, cultures and disasters: watch a society think, believe, work, build and change — and decide when (or whether) to intervene.

**Omatydom** is a life-simulation game set on an island, where you observe and shape a living society. The game runs semi-automatically, giving you the freedom to intervene — or to step back and let events unfold on their own.

The island's people are diverse: they belong to different races and hold their own ideas, each following a distinct religion and ideology. At the head of the nation stands a ruler. You can choose who leads, assassinate them, or take direct control over their decisions.

In Omatydom, you are not forced to micro-manage every detail. You can guide, manipulate, or simply watch as the people of the island live, believe, and evolve — shaped by your choices, or by their own.

---

## Download & play right now / التنزيل والتجربة

**رابط التحميل المباشر للتطبيق (APK):**  
https://github.com/dotee313/Omatydom/releases/download/v0.9.2/omatydom-0.9.2-debug.apk

افتح الرابط من جوالك،حمّل الملف، ثم اضغط عليه واسمح بالتثبيت من «مصادر غير معروفة». بعد التثبيت افتح التطبيق — الجزيرة وسكانها يعملون تلقائيًا. التصميم الآن أنظف: سبع صفحات (الجزيرة، السكان، الطبيعة، المخلوقات، الممالك، القدرات، الأحداث) تتنقل بينها بقائمة سفلية من الأيقونات، وكل ضغطة على أيقونة أو شخص أو أرض تعرض شرحًا بسيطًا لمدة ٣ ثوانٍ. اختر أرضًا بالضغط ثم افتح «القدرات» لتوجيه تأثيرك إلى المكان المختار. السنة الكاملة في اللعبة تساوي ساعة واحدة من الوقت الحقيقي عند السرعة x1. يعمل على Android 5.0 أو أحدث.

صفحة الإصدار الكاملة: https://github.com/dotee313/Omatydom/releases/tag/v0.9.2

> The APK is debug-signed and intended for play-testing. Your phone may ask you to allow installation from unknown sources.

---

## What's new in v0.9.2 — "Refined Dock, Clearer Icons"

This release is the supervised three-team polish pass over v0.9.1. Design, code and graphics reporters each audited the build; approved fixes were applied by a separate fixer in each team and cross-checked before merging.

- **Design refinements** — the dock uses adaptive heights (160dp on phones, 220dp on tablets) instead of a fixed 220dp, every dock/tool/brush button is now 48dp, Arabic text wraps instead of being clipped, margins are RTL-correct `Start/End`, the laws group owns the mode button, and bars use darker, higher-contrast colours. Scrollbars are visible on horizontal tool rows.
- **Code hardening** — `birthNote` and the desktop `statusOf` no longer claim birth spacing for someone who cannot conceive; `IslandView` skips null citizens and stops redrawing while paused; people/event lists refresh when every displayed field changes; dock icons are cached drawables instead of per-header bitmaps; `getIdentifier` is replaced with static `R.id` tables.
- **Graphics fixes** — the religion overlay no longer duplicates the blessing power, the white highlight is visible on light tiles, all dock badges are 12×12, traits and specialty are visually distinct, new culture/status/pregnancy card icons are wired into the dossier, zoom in/out share one lens colour, and the launcher icon is generated consistently at all five densities.
- **Unchanged** — seven tabs, five overlays, sixteen brushes, 3-second Arabic hints, island resources, citizens, jobs, races/religions/ideologies, classes, wildlife, politics, building, hunting/fishing/gathering/mining, and the clock (270-day pregnancy, 360-day spacing, 360 game days = 1 real hour at x1).

## What's new in v0.9.1 — "Organized Dock, Deeper People"

This release refines the latest play-test feedback: the single dock is now sorted into four titled groups, every group and card section gets fresh pixel art, and tapping a citizen opens a richer but strictly truthful dossier.

- **Four sorted dock groups** — **البشر** for people and ruler actions, **البهائم** for wildlife and semi-sapient creatures, **القوانين** for island laws and decrees, and **الإعدادات والخيارات** for speed, zoom/camera, overlays, brushes, build and game options. The seven tabs and all legacy IDs are preserved; map tools still appear only on the Island tab.
- **New pixel icons** — a dedicated pixel artist produced distinct 9×9–12×12 badges for the four groups and eight citizen-card sections using the approved burgundy/orange/brown palette. No WorldBox assets were copied.
- **Deeper citizen cards** — five divided Arabic sections with the portrait and new section icons: kingdom/clan/chief/members and people; faith, ideology, race and island culture; family and conditional pregnancy; skills, traits, idea and occupation; location and live status. Fields the model does not store (language, spouse, kinship, separate skills/specialty) are declared unrecorded instead of invented.
- **Truthful fertility text** — the pregnancy day uses the real 270-day constant, and birth spacing counts down only from a real previous birth for eligible adult females. Males, children and never-pregnant citizens get "not applicable".
- **More desktop detail** — `citizens [n]` / `people` prints the same real fields, and `qa/verify_v091.sh` runs all four verifiers plus literal building-cost and 360-days-per-hour clock checks.

## What's new in v0.9.0 — "Build Your Island"

This release answers the latest play-test feedback: the logo is now a real launcher icon, every bar lives in one unified bottom dock, citizens open a portrait card, animals and people walk smoothly, the sea is wider, and you can build or reclaim land yourself.

- **Logo fixed** — your supplied logo is installed as the Android launcher icon (PNG densities 48–192px) and stays visible in the bottom dock.
- **One bottom dock** — the former top title/status/resource/time/tool bars and the bottom tabs are merged into a single scrollable bottom dock. The island takes the remaining screen space; map brushes and camera tools show only on the Island tab.
- **Citizen cards** — tapping any islander opens an Arabic dialog with the existing pixel portrait, identity, job, class, race, faith, ideology, health, happiness, wealth, kingdom, location, pregnancy day, temporary statuses and trait, plus an "Appoint ruler" button. Simulation keeps running.
- **Build your island** — a new dock button opens 15 real buildings with their wood/stone/gold costs and a land-reclaim option. Pick one, tap a tile, and the game charges the exact resources. A dashed orange/burgundy preview shows whether the tile is valid before you tap.
- **Natural walking** — citizens and animals ease between their old and new tiles during each 10-second game day instead of snapping. Facing follows movement; pause and speed changes still come from the core clock.
- **Wider sea** — the default island's water share rose from ~26.8% to ~42.7% across five seeds, with a more indented coastline and all 16 templates kept intact.
- **Verification** — `qa/verify_v081.sh` runs `VerifyV060`, `VerifyV070`, `VerifyV080` and the new `VerifyV081` (sea share, exact build costs, land reclaim, calendar); all pass. Pregnancy remains 270 days + 360 days rest, and one game year is still one real hour at x1.

## What's new in v0.8.0 — "Your Art, Richer World"

This release integrates the original artwork and interface design you supplied, deepens the pixel rendering, and adds a living trade layer between kingdoms.

- **Your artwork is now in the game** — the supplied logo, info/mods/settings icons, palette and menu background are shipped as original Android resources. The title bar, utility row, selected tabs and brushes use your burgundy/orange/charcoal palette; the info dialog shows your menu art.
- **Richer pixel rendering** — neighbour-aware shoreline and biome seams, wet sand lips, two-tone contact shadows, water sparkles, striped farm crops, cobbled roads, layered mountains and caves, facing cues and zoom-gated health/material bars for citizens, plus smoke, glints and longer rain/meteor trails for effects.
- **Trade caravans** — once two kingdoms exist, caravans open every 30–45 game days, travel for 10–20 days, deliver gold, food and knowledge, and appear in the Android Kingdoms page and the desktop `trade` command.
- **Cleaner utility row** — compact info / mods / settings buttons next to the title, with 3-second Arabic hints and readable 40dp touch targets. All seven tabs, five overlays, 16 brushes and camera controls are preserved.
- **Verification** — `qa/verify_v080.sh` runs `VerifyV060`, `VerifyV070` and the new `VerifyV080`; all pass. The 270-day pregnancy, one-year rest and one-real-hour game year are unchanged.

## What's new in v0.7.0 — "Sandbox World"

A large visual and interaction overhaul. The simulation is unchanged, but the game now looks and plays much more like a compact pixel sandbox while keeping every sprite original and generated in code.

- **Zoomable, pannable world** — pinch to zoom, drag to move the camera, double-tap to zoom in, and centre/zoom buttons on the map toolbar. Tiles stay readable instead of squeezing the whole island into the screen.
- **Sandbox paint brushes** — water, sand, grass, forest, orchard, mountain, cave, hut, farm, road, settler, wolf, fire, rain and meteor. Drag a brush over the island to reshape it instantly; inspect mode keeps the old tap-to-explain behaviour.
- **Animated living map** — moving water glints and shoreline foam, drifting clouds, weather particles, day/night tint and seasonal colour shifts.
- **Detailed original sprites** — layered trees by biome, shaded mountains and caves, distinct buildings for all 20 structure types, readable citizens with job tools and animals drawn individually for all eight species.
- **Impact effects** — rain ripples, fire sparks, lightning flashes, meteor streaks and rings, war marks and gentle healing/blessing motes when powers or disasters are used.
- **Cleaner icon-first interface** — a compact resource strip, rounded dark panels, highlighted active tool tabs, 16 brush badges and the same seven bottom pages with 3-second Arabic explanations.

All simulation rules from v0.6.0 remain: 16 templates, 19 biomes, four peoples, kingdoms/clans/cultures/wars, 70 technologies, 23 laws, 17 disasters, 33 resources, 134 buildings, one child at a time, 270-day pregnancy, a full-year rest and one game year per real hour at x1.

## What's new in v0.6.0 — "Living Civilizations"

This is the large follow-up to v0.5.0, built from the systems discovered in the supplied `New.zip` data pack. Every icon, name and rule here is an original lightweight implementation; no proprietary WorldBox art or text was copied.

- **Sixteen map templates** — islands, continent, checkerboard, cheese, donut, anthill, volcano, empty, box world, pancake, toast, lasagna, cubicles, bad apple, chaos pearl and dormant volcano. Templates change real terrain generation, not just the label.
- **Nineteen biomes** — grass, savanna, enchanted, mushroom, corrupted, infernal, jungle, swamp, wasteland, desert, crystal, candy, lemon, tumor, biomass, pumpkin, cybertile, permafrost and sand. Biomes alter work, food, regrowth and tile colouring.
- **Four civilized peoples** — human, elf, orc and dwarf. Species affect names, colours and the kingdoms that settlers can found; children inherit their mother's people.
- **Kingdoms, clans, cultures and diplomacy** — capitals move, treasuries and armies grow, clans form, cultures accumulate knowledge, and relations shift between peace, alliance, rivalry and war.
- **Five war types** — conquest, inspire, rebellion, spite and whisper. Wars consume armies and treasure, lower stability and eventually end with a treaty.
- **Seventy technologies** — a real research tree with costs, categories and effects. Priests and traders generate knowledge, and research improves work, food, wood, ore, birth rate, learning, loyalty, defence, trade and construction costs.
- **Twenty-three toggleable world laws** — hunger, old age, animals, grass, minerals, erosion, disasters, diplomacy, borders, armies, population cap, forever peace, forever lava and more. Laws are simulated in both the engine and the Android Nature tab.
- **Seventeen disasters** — from earthquakes, meteorites, tornadoes and ash fall to necromancers, plague ships, bandits and alien vessels. Each one changes tiles, resources, creatures, health or stability.
- **Twelve status effects** — burning, poisoned, frozen, enchanted, caffeinated, shielded, enraged and others now tick down each day and modify health, mood and work. Powers and disasters apply them.
- **Thirty-three named resources** — the original stockpiles are still used by the economy, while fish, meat, herbs, crops, metals, gems and other named resources are tracked and displayed.
- **Six extra jobs** — blacksmith, settler, road builder, cleaner, fireman and attacker join the original ten, with real daily behaviour.
- **134 building types** — grouped into housing, food, craft, military, culture, trade and utility. Twenty hand-drawn structure glyphs are rendered on the map; the rest map to a matching visual family.
- **Three mod packs and twelve achievements** — Core, Test Mod and Crazy Traits tune birth rate, work and trait frequency; achievements are unlocked by real milestones and announced in the chronicle.
- **Seven-tab Android interface** — world, people, nature, creatures, kingdoms, powers and events. New overlay modes tint the island by terrain, biome, kingdom, religion or job. All icons remain code-generated and every tap still explains itself for three seconds.
- **Preserved rules** — 12 months, four seasons, one game year per real hour at x1, one child at a time, 270-day pregnancy and a full-year rest are unchanged.

## Earlier: v0.5.0 — "Living World"

A major update inspired by the structure and design ideas found in the supplied `New.zip` archive. All new icons, names and behaviour are original and generated in code; no proprietary art or text was copied.

- **Ten world powers with a dedicated tab** — life rain, green tide, healing, blessing, storm spear, gray sickness, earthquake, sky fire, calm and golden rain. Each has a code-drawn icon, gold cost, cooldown in game days and a chronicle entry. Tap a tile first to aim a power; otherwise it picks a random land tile.
- **Changing weather** — clear skies, rain, storms, drought, snow and volcanic ash. Weather lasts several days and really changes work output, resource regrowth, mood and the chance of storm damage to buildings.
- **Eight long ages** — Dawn, High Sun, Harvest, Storm, Quiet Moon, Ash, Long Ice and Renewal. An age lasts two game years, shifts work and birth rates, changes mood and is announced in the chronicle.
- **Personalities** — every islander gets one of eight traits: brave, lazy, generous, zealous, curious, resilient, greedy or content. Traits modify work, happiness, loyalty, learning and survival, and appear in the people tab and tap hints.
- **Dangerous creatures** — wolves, sand worms, crystal golems, restless ones and young dragons now roam beside deer, boars and fish. They are drawn larger with red eyes; they wound or kill nearby islanders, and hunters can be hurt when attacking them.
- **World statistics** — births, deaths, structures built and powers used are shown in the top status line and tracked in `World`.
- **Cleaner five-tab layout** — the new Powers tab joins island, people, decrees and chronicle in the bottom navigation bar. Arabic hints still disappear after 3 seconds.
- **Same core simulation rules** — 12 months, four seasons, one game year per real hour at x1, one child at a time, 270-day pregnancy and a full-year rest are unchanged.

## Earlier: v0.4.0 — "Realistic Island"

A cleaner, deeper update built on top of v0.3.0:

- **Cleaner layout with a bottom navigation bar** — four icon tabs: island, people, decrees and chronicle. Top status, season bar and 3-second hint stay visible while the pages swap underneath.
- **Icons from the supplied art pack** — the bottom bar and all eight decree icons are real PNGs taken from `worldbox-diplomacy-overhaul-0.14.0` and shipped as tiny `drawable-nodpi` assets; no runtime downloads.
- **More believable pixel people** — characters are now 10×14 shaded sprites with hair, face highlights, eyes, nose, mouth, boots, arms, belts and a different tool for every job (axe, pick, rod, bow, staff, basket, coins). Rulers wear a gold crown; pregnant women have a rounded belly.
- **Portraits in the people tab** — every candidate gets a 12×12 shaded face generated in `PixelIcons.java`, tappable to appoint as ruler.
- **Real-time calendar** — 12 months of 30 days and four seasons of 90 days. One full game year = exactly one real hour at speed x1 (10 seconds per day); `x2`/`x3` are faster.
- **Nine-month pregnancy** — a woman can carry only one child at a time. Conception takes 270 game days (three seasons / nine months), then she rests a full year before conceiving again. The people tab and tap hints show the exact day of pregnancy.
- **Same living simulation underneath** — jobs, classes, races, religions, ideologies, ideas, wildlife, politics, successions, coups and random events are unchanged.

## Earlier: v0.3.0 — "Pixel Island"

- First pixel-art redesign: shaded 6×7 citizens, rounded-square noise-warped island, pixel terrain and code-generated icons.
- All choices became icons and any tap showed a 3-second Arabic explanation.

## Earlier: v0.2.0 — "The Living Island"

- 40×24 tile island with water, sand, grass, forest, orchards, mountains and caves; resources regenerate slowly.
- Six original races — Vaeli, Korun, Sireni, Dravi, Lumin and Tiren — each with a colour, a story and a natural trade.
- Working citizens: woodcutters, gatherers, fishers, hunters, miners, builders, priests, rulers and traders.
- Social classes and seven spreading ideas that change happiness and loyalty.
- Wildlife and an ASCII map on desktop for play-testing.

## Earlier foundation (v0.1.0)

- Pure-Java simulation engine with no Android dependencies (`core`).
- Text-based desktop runner (`desktop`) and native Android UI (`app`).
- One intervention API (`OmatydomGame`) shared by both front-ends.

---

## Architecture

```
┌──────────────┐        ┌──────────────────────┐
│   desktop    │        │         app          │
│  (CLI + map) │        │ (Android + IslandView)│
└──────┬───────┘        └──────────┬───────────┘
       │                           │
       └──────────┬────────────────┘
                  ▼
        ┌────────────────────┐
        │        core        │
        │  pure Java engine  │
        │  model + world     │
        └────────────────────┘
```

| Module | Type | Purpose |
| --- | --- | --- |
| `core` | `java-library` | Game rules: terrain, agents, animals, jobs, resources, events, politics. No framework dependencies. |
| `desktop` | Java application | Command-line front-end with ASCII map, used for play-testing. |
| `app` | Android application | Native Java UI that draws the island and forwards player actions. |

The front-ends never contain game rules. They only call `OmatydomGame` and render the resulting state.

### Key classes

- `OmatydomGame` — facade: create/tick, pause/speed, rulers, decrees, powers, laws, mod packs, research, wars, disasters, creatures, jobs and construction.
- `SimulationEngine` — one day: weather, ages, economy, biomes, agent work, wildlife, monsters, ideas, ageing, births, deaths, politics, kingdoms, clans, cultures, technology, laws, disasters, achievements and events.
- `World` — island state: map, citizens, agents, animals, named resources, laws, achievements, civs, wars, relations, tech bonuses, ruler, weather and chronicle statistics.
- `IslandMap` / `Tile` / `TileType` / `Biome` / `MapTemplate` / `Structure` / `BuildingType` — generated terrain, biomes and buildings.
- `Kingdom` / `Clan` / `Culture` / `War` / `Species` / `DiplomacyRelation` / `StatusEffect` / `Achievement` — the deep civilization layer.
- `Technology` / `TechEffect` / `TechCategory` / `ResourceType` / `ModPack` / `DisasterType` — data catalogues with real gameplay effects.
- `Agent` / `Animal` — positions, jobs, targets, work and wildlife.
- `Citizen` / `Ruler` / `Trait` — the people, the throne, personalities and temporary conditions.
- `Race`, `SocialClass`, `Idea`, `Job` — identity and daily life.
- `Decree`, `Power`, `Age`, `Weather`, `GameEvent`, `EventType` — actions, world states and the chronicle.

---

## Project layout (v0.6.0)

```
Omatydom/
├── README.md
├── settings.gradle
├── build.gradle
├── gradle.properties
├── .gitignore
├── core/
│   ├── build.gradle
│   └── src/main/java/com/dotee313/omatydom/core/
│       ├── GameConfig.java
│       ├── OmatydomGame.java
│       ├── SimulationEngine.java
│       ├── SimulationListener.java
│       ├── World.java
│       ├── Age.java, Power.java, Decree.java
│       ├── GameEvent.java, EventType.java, NameGenerator.java
│       ├── Technology.java, TechEffect.java, TechCategory.java
│       ├── ResourceType.java, BuildingType.java, ModPack.java, DisasterType.java
│       ├── civ/
│       │   ├── Species.java, Kingdom.java, Clan.java, Culture.java
│       │   └── War.java, WarType.java, DiplomacyRelation.java
│       │   └── StatusEffect.java, Achievement.java
│       ├── model/
│       │   ├── Citizen.java, Ruler.java, Trait.java
│       │   ├── Race.java, Gender.java
│       │   ├── Religion.java, Ideology.java
│       │   └── Job.java, SocialClass.java, Idea.java
│       └── world/
│           ├── IslandMap.java, Tile.java, TileType.java
│           ├── Biome.java, MapTemplate.java, WorldLaw.java
│           ├── Structure.java, Weather.java
│           └── Agent.java, Animal.java, AnimalType.java
├── desktop/
│   └── src/main/java/com/dotee313/omatydom/desktop/DesktopLauncher.java
└── app/
    ├── src/main/AndroidManifest.xml
    └── src/main/
        ├── java/com/dotee313/omatydom/android/
        │   ├── MainActivity.java
        │   ├── IslandView.java
        │   ├── PixelIcons.java
        │   └── EventAdapter.java
        ├── res/layout/activity_main.xml, event_item.xml
        └── res/drawable-nodpi/*.png   (icons from the supplied art pack)
```

---

## Build and run

Requirements: **JDK 17**, **Gradle 8.x**, and for Android **SDK 34** (or Android Studio).

### Android

1. Open Android Studio → **Open** → select the `Omatydom` folder.
2. Let Gradle sync.
3. Run `app` on an emulator or device (API 21+).

```bash
gradle :app:assembleDebug
```

### Desktop demo

```bash
gradle :desktop:run                                      # random island
gradle :desktop:run --args="42"                          # reproducible island
gradle :desktop:run --args="42 CONTINENT TEST_MOD"       # template + ruleset
```

### Regression checks

```bash
qa/verify_v091.sh          # VerifyV060 + VerifyV070 + VerifyV080 + VerifyV081 (sea, building, calendar)
qa/verify_v081.sh          # Previous combined runner, still valid
```

### Desktop commands

The CLI includes the full v0.6.0 command set plus `trade`, `sea`, `citizens` and `build <BUILDING_TYPE>`:

| Command | Description |
| --- | --- |
| `help` | Show all commands. |
| `status` / `stats` | Current world / deep simulation counters. |
| `map` | ASCII island: terrain, buildings, animals and working islanders. |
| `agents` | List living islanders with job, class, idea and position. |
| `citizens [n]` / `people` | Print richer truthful citizen data: kingdom/clan, faith, ideology, race/species, fertility, status and culture. |
| `resources [TYPE]` / `animals` / `biome <x> <y>` | Inspect stockpiles, wildlife and tiles. |
| `jobs` / `job <id> <JOB>` | List or assign jobs, including the six new ones. |
| `build <BUILDING_TYPE>` / `build <x> <y> <STRUCTURE>` / `building <x> <y> <BUILDING_TYPE>` | Build a catalogued type on free land, a simple structure at a tile, or any of the 134 types. |
| `sea` | Print the current map's water ratio. |
| `events [n]` / `achievements` | Show the chronicle / unlocked milestones. |
| `step [n]` / `run [n]` / `pause` / `resume` / `speed n` | Time control. |
| `rulers` / `choose <id>` / `assassinate` / `control` / `auto` | Politics and direct control. |
| `decree [NAME]` / `power [NAME]` | Issue a decree / cast a world power at a random land tile. |
| `templates` / `mods` / `new [seed] [TEMPLATE] [MOD]` | Browse and start worlds with different generation and rules. |
| `laws [KEY [on|off]]` | List or toggle any of the 23 world laws. |
| `civs` / `trade` / `tech [research]` / `war [TYPE] [A_ID] [B_ID]` | Inspect civilizations, caravans, research and wars. |
| `disaster [TYPE]` / `spawn [TYPE]` | Trigger any disaster or spawn any creature. |
| `quit` | Exit. |

---

## Roadmap

- [x] Factions with relations, rivalries and wars.
- [x] Deep Android interface with map overlays and code-generated icons.
- [ ] Save/load (serialize `World` to JSON).
- [x] Zoom/pan on the Android island view (v0.7.0, enhanced in v0.8.0).
- [ ] Fully data-driven events and jobs instead of hard-coded ones.
- [x] Character screens with portraits and needs (v0.9.0: Arabic citizen card; v0.9.1: grouped sections and truthful fields; v0.9.2: refined dock, adaptive heights, clearer icons).
- [ ] Android UI with fragments and Material 3.
- [ ] Scenario/challenge modes ("keep the island stable for 10 years").

---

## ملخص بالعربية

**أوماتيدوم** لعبة محاكاة حياة على جزيرة، حيث تراقب مجتمعًا حيًا وتشكّله. الزمن يمضي تلقائيًا، ولك أن تتدخل أو تترك الأحداث تجري من نفسها.

الجزيرة ليست دائرية بل مربع مدوّر فيه خلجان ورؤوس عشوائية: ماء، رمال، غابات، أشجار فواكه، جبال وكهوف. سكانها من **ستة أعراق خيالية لا توجد في عالمنا**، وكل واحد منهم الآن **شخصية بكسلية أوضح وأكثر واقعية** (١٠×١٤ بكسل): شعر، وجه مضلّل، عينان، أنف، فم، ذراعان، حزام، حذاء، وأداة تختلف حسب المهنة (فأس، معول، صنارة، قوس، عصا، سلة، عملة). الحاكم يرتدي تاجًا ذهبيًا، والمرأة الحامل يظهر بطنها. أكثر أيقونات اللعبة مرسومة داخل التطبيق بالكود، ومعها الآن شعارك وأيقونات المعلومات والأنماط والإعدادات وخلفية قائمتك الأصلية من الملف الذي أرسلته، مُبسّطة لتناسب واجهة الجوال. الرسم مستطيلات مسطّحة خفيفة على الهاتف مع حواف وتظليل ووميض أقوى. كل شخص يمشي ويعمل: يجمع الخشب، يبني البيوت والأبراج، يصيد السمك والحيوانات، يجمع الفواكه، ويستخرج الحجر والخام والكريستال.

في الإصدار ٠٫٩٫٢ راجعت ثلاث فرق متخصصة الواجهة والكود والرسومات، فتحسّن ارتفاع الشريط بحسب حجم الشاشة، وصارت أهداف اللمس ٤٨dp، وتوقفت قصّ النصوص العربية، وصحّحت الهوامش اتجاه RTL، وأصلحت أيقونات متشابهة وأضيفت أيقونات للثقافة والحالة والحمل. كما لم يعد نص الحمل يذكر فاصل الولادة لمن لا يمكنها الحمل، وتوقفت إعادة رسم الخريطة عند إيقاف التطبيق. ثوابت الزمن والبناء والسكان والتبويبات السبع باقية كما هي.

في الإصدار ٠٫٩٫١ رُتّب الشريط السفلي في أربع مجموعات واضحة: **البشر** لكل ما يخص السكان والحاكم، **البهائم** للحيوانات والمخلوقات شبه العاقلة، **القوانين** لقوانين الجزيرة والمراسيم، و**الإعدادات والخيارات** للسرعة والعدسة والطبقات والفرش والبناء. ولكل مجموعة ولكل قسم في بطاقة الشخصية أيقونة بكسلية جديدة رسمها فنان مخصص بالألوان نفسها. بطاقة الشخصية صارت خمسة أقسام مرتبة تعرض المملكة والعشيرة والشعب والدين والأيديولوجيا والفكرة والمهنة والحالة والموقع، وتقول بصدق إن اللغة والزواج والقرابة والمهارات المنفصلة غير مسجلة في النموذج بدل اختراعها. نص الحمل صار محكومًا بالبيانات الحقيقية: يوم الحمل من ٢٧٠، والفاصل يُحسب فقط بعد ولادة حقيقية وللإناث البالغات القابلات للحمل. الاختبارات الأربعة تنجح، وأُضيف فحص تكاليف المباني وثوابت الساعة.

في الإصدار ٠٫٩٫٠ تحققت ملاحظاتك: الشعار صار أيقونة حقيقية للتطبيق، وكل الأشرطة صاروا شريطًا سفليًا موحدًا يترك مساحة أكبر للخريطة. الضغط على أي شخص يفتح بطاقة عربية بصورته ومعلوماته الحقيقية وزر تعيين الحاكم. الأحياء صارت تمشي بسلاسة بدل التنقل المفاجئ، والبحر كبر من نحو ٢٦٫٨٪ إلى ٤٢٫٧٪ بالمتوسط. وأضفت أداة بناء تختار ١٥ مبنى حقيقيًا أو توسّع اليابسة، مع معاينة قبل اللمس وخصم دقيق للخشب والحجر والذهب. الاختبارات الأربعة `VerifyV060/070/080/081` تنجح، والحمل ما زال ٢٧٠ يومًا ثم راحة سنة كاملة.

في الإصدار ٠٫٨٫٠ دخلت تصميماتك الأصلية إلى اللعبة: الشعار والأيقونات وخلفية القائمة وألوانك (الخمري والبرتقالي والفحمي) صارت جزءًا من شريط العنوان وأزرار المعلومات والأنماط والإعدادات والتبويبات والفرش. تحسّنت الرسومات كثيرًا: حواف ساحلية وبيئية متجاورة، ظلال تحت الأشجار والحيوانات والمباني، وميض ماء، صفوف محاصيل في المزارع، طرق مرصوفة، اتجاه نظر للسكان، ودخان ووميض وذيول أطول للتأثيرات. وأُضيفت **قوافل تجارية** بين الممالك: تُفتح كل ٣٠–٤٥ يومًا، وتستغرق ١٠–٢٠ يومًا، وتجلب ذهبًا وطعامًا ومعرفة، ويظهر تقدّمها في صفحة الممالك وأمر `trade` في نسخة الحاسوب.

في الإصدار ٠٫٦٫٠ صارت الجزيرة عالمًا أعمق: **١٦ شكلًا للخريطة**، **١٩ بيئة حيوية**، أربع شعوب (إنسان، إلف، أورك، قزم)، ممالك وعشائر وثقافات وعلاقات دبلوماسية وخمس أنواع حروب، **٧٠ تقنية** تُبحث وتغيّر الاقتصاد والولاء والدفاع، **٢٣ قانونًا عالميًا** يمكن تفعيله أو إيقافه، **١٧ كارثة** و**١٢ حالة مؤقتة** و**٣٣ موردًا** و**١٣٤ نوع مبنى**. تظهر كل هذه الأنظمة في سبع صفحات: الجزيرة، السكان، الطبيعة، المخلوقات، الممالك، القدرات، الأحداث. اضغط أي وجه في صفحة السكان لتعيينه حاكمًا. وأي ضغطة على أيقونة أو شخص أو قطعة أرض تعرض شرحًا بسيطًا لمدة ٣ ثوانٍ ثم يختفي تلقائيًا.

**الزمن والحمل:** السنة في اللعبة = ٤ فصول × ٩٠ يومًا = ٣٦٠ يومًا، وتستغرق **ساعة واحدة** من الوقت الحقيقي عند السرعة x1 (كل يوم = ١٠ ثوانٍ). المرأة تحمل مرة واحدة فقط، ويستغرق الحمل **٣ فصول = ٩ أشهر = ٢٧٠ يومًا**، ثم ترتاح سنة كاملة قبل حمل جديد. تجد اليوم الحالي للمرة الحامل في صفحة السكان وفي شرح الضغط عليها.

التحكم التجريبي: افتح المشروع في Android وشغّل `app`، أو جرّب `gradle :desktop:run --args="42"` ثم اكتب `map` لرؤية الجزيرة و`agents` لرؤية العمال.

---

## Contributing

Ideas, balancing suggestions, event packs and pull requests are welcome. Keep all game rules inside `core` so both front-ends stay in sync.

## License

To be decided by the project owner.
