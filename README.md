# Omatydom

> A tiny living island: watch a society think, believe, work, build and change — and decide when (or whether) to intervene.

**Omatydom** is a life-simulation game set on an island, where you observe and shape a living society. The game runs semi-automatically, giving you the freedom to intervene — or to step back and let events unfold on their own.

The island's people are diverse: they belong to different races and hold their own ideas, each following a distinct religion and ideology. At the head of the nation stands a ruler. You can choose who leads, assassinate them, or take direct control over their decisions.

In Omatydom, you are not forced to micro-manage every detail. You can guide, manipulate, or simply watch as the people of the island live, believe, and evolve — shaped by your choices, or by their own.

---

## Download & play right now / التنزيل والتجربة

**رابط التحميل المباشر للتطبيق (APK):**  
https://github.com/dotee313/Omatydom/releases/download/v0.3.0/omatydom-0.3.0-debug.apk

افتح الرابط من جوالك، حمّل الملف، ثم اضغط عليه واسمح بالتثبيت من «مصادر غير معروفة». بعد التثبيت افتح التطبيق — الجزيرة وسكانها يعملون تلقائيًا. الشاشة الآن كلها أيقونات: اضغط أي أيقونة لتنفيذ أمرها، واضغط أي شخص أو قطعة أرض في الجزيرة لتظهر لك معلومات بسيطة عنها لمدة ٣ ثوانٍ. يعمل على Android 5.0 أو أحدث.

صفحة الإصدار الكاملة: https://github.com/dotee313/Omatydom/releases/tag/v0.3.0

> The APK is debug-signed and intended for play-testing. Your phone may ask you to allow installation from unknown sources.

---

## What's new in v0.3.0 — "Pixel Island"

A full visual and interaction redesign, built for cheap phone rendering:

- **Real pixel people** — citizens are now tiny 6×7 pixel characters: hair, face, eyes, coloured clothes and legs. Rulers get a gold crown; race colour paints the skin and job colour paints the clothes. No sprites, no textures, no anti-aliasing.
- **Non-circular island** — the coastline is a rounded square warped by smooth noise, so it has coves, headlands and soft corners instead of being a disc.
- **Pixel terrain and buildings** — water ripples, grass tufts, trees, fruit, mountain peaks, cave mouths, huts, temples, towers, docks and mines are all drawn from a few flat rectangles.
- **Icons instead of text** — pause, speed, control, assassination, all eight decrees and ruler candidates are hand-made pixel icons generated in code (`PixelIcons.java`).
- **3-second explanations** — tap any icon, person or tile and a short Arabic explanation appears for exactly 3 seconds, then disappears by itself.
- **Candidate portraits** — the ruler candidates row shows small pixel faces you can tap to appoint, with an arrow to browse more candidates.
- **Same living simulation underneath** — jobs, classes, races, religions, ideologies, ideas, wildlife, politics, successions, coups and random events are unchanged.

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

- `OmatydomGame` — facade: create/tick, pause/speed, appoint or assassinate a ruler, take/release control, issue decrees, assign jobs, build structures.
- `SimulationEngine` — one day: economy, agent work, wildlife, idea spread, ageing, births, deaths, coups, succession, events.
- `World` — island state: map, citizens, agents, animals, stockpiles, ruler, chronicle.
- `IslandMap` / `Tile` / `TileType` / `Structure` — terrain and buildings.
- `Agent` — a citizen's position, job, target and work progress.
- `Citizen` / `Ruler` — the people and the throne.
- `Race`, `SocialClass`, `Idea`, `Job` — identity and daily life.
- `Decree`, `GameEvent`, `EventType` — player actions and the island chronicle.

---

## Project layout (v0.3.0)

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
│       ├── Decree.java
│       ├── GameEvent.java
│       ├── EventType.java
│       ├── NameGenerator.java
│       ├── model/
│       │   ├── Citizen.java, Ruler.java
│       │   ├── Race.java, Gender.java
│       │   ├── Religion.java, Ideology.java
│       │   ├── Job.java, SocialClass.java, Idea.java
│       └── world/
│           ├── IslandMap.java, Tile.java
│           ├── TileType.java, Structure.java
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
        └── res/layout/activity_main.xml, event_item.xml
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
gradle :desktop:run                 # random island
gradle :desktop:run --args="42"     # reproducible island
```

### Desktop commands

| Command | Description |
| --- | --- |
| `help` | Show all commands. |
| `status` | Day, population, stability, stockpiles, ruler, mood. |
| `map` | ASCII island: terrain, buildings, animals and working islanders. |
| `agents [n]` | List living islanders with job, class, idea and position. |
| `resources` | Wood, stone, ore, crystal, food, gold, buildings, wildlife. |
| `animals` | Deer / boar / fish counts. |
| `jobs` | List assignable job names. |
| `job <id> <JOB>` | Reassign an islander (e.g. `job 3 BUILDER`). |
| `build <x> <y> <STRUCTURE>` | Build from your own stockpile (e.g. `build 12 8 HUT`). |
| `events [n]` | Show the last `n` chronicle entries. |
| `step [n]` / `run [n]` | Advance `n` days. |
| `pause` / `resume` | Stop or resume the clock. |
| `speed n` | Days per tick (1–10). |
| `rulers` / `choose <id>` | List candidates / appoint a ruler. |
| `assassinate` | Remove the ruler and trigger a succession. |
| `control` / `auto` | Take direct control / step back. |
| `decree [NAME]` | Issue a decree (`FESTIVAL`, `CRACKDOWN`, …). |
| `quit` | Exit. |

---

## Roadmap

- [ ] Save/load (serialize `World` to JSON).
- [ ] Zoom/pan and touch interaction on the Android island view.
- [ ] Factions with relations, rivalries and wars.
- [ ] Data-driven events and jobs instead of hard-coded ones.
- [ ] Character screens with portraits and needs.
- [ ] Android UI with fragments and Material 3.
- [ ] Scenario/challenge modes ("keep the island stable for 10 years").

---

## ملخص بالعربية

**أوماتيدوم** لعبة محاكاة حياة على جزيرة، حيث تراقب مجتمعًا حيًا وتشكّله. الزمن يمضي تلقائيًا، ولك أن تتدخل أو تترك الأحداث تجري من نفسها.

في التحديث الجديد الجزيرة ليست دائرية بل شكلها مربع مدوّر فيه خلجان ورؤوس عشوائية: ماء، رمال، غابات، أشجار فواكه، جبال وكهوف. سكانها من **ستة أعراق خيالية لا توجد في عالمنا**، وكل واحد منهم الآن **شخصية بكسلية صغيرة** لها شعر وعينان وملابس بلون المهنة، والحاكم يرتدي تاجًا ذهبيًا. الرسم كله مستطيلات مسطّحة بلا صور ولا تأثيرات ثقيلة حتى لا يثقل الهاتف. كل شخص يمشي ويعمل: يجمع الخشب، يبني البيوت والأبراج، يصيد السمك والحيوانات، يجمع الفواكه، ويستخرج الحجر والخام والكريستال. هناك أيضًا طبقات اجتماعية وأفكار تنتقل بين الجيران.

التحكم صار بالأيقونات فقط: اضغط أيقونة الإيقاف أو السرعة أو التولّي أو الاغتيال، واضغط أي مرسوم من الصف الأول، واضغط وجهًا من صف المرشحين لتعيينه حاكمًا. وأي ضغطة على أيقونة أو شخص أو قطعة أرض تعرض شرحًا بسيطًا لمدة ٣ ثوانٍ ثم يختفي تلقائيًا.

التحكم التجريبي: افتح المشروع في Android وشغّل `app`، أو جرّب `gradle :desktop:run --args="42"` ثم اكتب `map` لرؤية الجزيرة و`agents` لرؤية العمال.

---

## Contributing

Ideas, balancing suggestions, event packs and pull requests are welcome. Keep all game rules inside `core` so both front-ends stay in sync.

## License

To be decided by the project owner.
