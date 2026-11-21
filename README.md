# Omatydom

> A tiny living island: watch a society think, believe, work, build and change — and decide when (or whether) to intervene.

**Omatydom** is a life-simulation game set on an island, where you observe and shape a living society. The game runs semi-automatically, giving you the freedom to intervene — or to step back and let events unfold on their own.

The island's people are diverse: they belong to different races and hold their own ideas, each following a distinct religion and ideology. At the head of the nation stands a ruler. You can choose who leads, assassinate them, or take direct control over their decisions.

In Omatydom, you are not forced to micro-manage every detail. You can guide, manipulate, or simply watch as the people of the island live, believe, and evolve — shaped by your choices, or by their own.

---

## What's new in v0.2.0 — "The Living Island"

This update turns the abstract population into an actual **place**:

- **A real island map** — 40×24 tiles of water, sand, grass, forest, orchards, mountains and caves. Resources regenerate slowly.
- **Six original races** — Vaeli, Korun, Sireni, Dravi, Lumin and Tiren. None of them exist in our world; each has a colour, a story and a natural trade.
- **Tiny simple bodies** — islanders are drawn as a coloured dot with a short body line, like compact stick figures. No sprites, no textures, no heavy rendering: cheap enough for a phone.
- **Working citizens** — each person walks the map and performs a real job:
  - Woodcutters harvest forests → **wood**
  - Gatherers pick fruit in orchards → **food**
  - Fishers work from the shore → **food**
  - Hunters track deer and boar → **food**
  - Miners dig stone, ore and rare crystal in mountains and caves
  - Builders spend wood and stone to raise **huts, docks, towers, temples and mines**
  - Priests calm and convert nearby people; rulers inspire loyalty; traders earn gold
- **Social classes** — Child, Laborer, Artisan, Clergy and Noble, coloured and driven by wealth and occupation.
- **Spreading ideas** — Survival, Progress, Mysticism, Rebellion, Peace, Trade and Curiosity jump between neighbours and nudge happiness and loyalty.
- **Wildlife** — deer, boar and fish wander the island; hunters deplete them and new animals arrive over time.
- **ASCII map on desktop** — the full simulation is playable and debuggable without Android.

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

## Project layout (v0.2.0)

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

في التحديث الجديد الجزيرة أصبحت خريطة حقيقية: ماء، رمال، غابات، أشجار فواكه، جبال وكهوف. سكانها من **ستة أعراق خيالية لا توجد في عالمنا**، لكل منهم لون ومهنة طبيعية. الناس أجسام صغيرة جدًا (نقطة بلون العرق + خط قصير بلون المهنة) حتى لا يثقلوا الهاتف. كل شخص يمشي ويعمل: يجمع الخشب، يبني البيوت والأبراج، يصيد السمك والحيوانات، يجمع الفواكه، ويستخرج الحجر والخام والكريستال. هناك أيضًا طبقات اجتماعية وأفكار تنتقل بين الجيران.

التحكم: افتح المشروع في Android وشغّل `app`، أو جرّب `gradle :desktop:run --args="42"` ثم اكتب `map` لرؤية الجزيرة و`agents` لرؤية العمال.

---

## Contributing

Ideas, balancing suggestions, event packs and pull requests are welcome. Keep all game rules inside `core` so both front-ends stay in sync.

## License

To be decided by the project owner.
