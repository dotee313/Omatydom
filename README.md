# Omatydom

> A life-simulation game about watching a society think, believe, argue, and change — and deciding when (or whether) to intervene.

**Omatydom** is a life-simulation game set on an island, where you observe and shape a living society. The game runs semi-automatically, giving you the freedom to intervene — or to step back and let events unfold on their own.

The island's people are diverse: they belong to different races and hold their own ideas, each following a distinct religion and ideology. At the head of the nation stands a ruler. You can choose who leads, assassinate them, or take direct control over their decisions.

In Omatydom, you are not forced to micro-manage every detail. You can guide, manipulate, or simply watch as the people of the island live, believe, and evolve — shaped by your choices, or by their own.

---

## Core pillars

- **Emergent society** — every citizen has an age, race, gender, religion, ideology, wealth, health, happiness, education, and loyalty. Beliefs drift, people are born, people die, and moods ripple through the island.
- **Semi-automatic time** — days advance on their own. The economy, population, politics, and random events keep moving whether you act or not.
- **Real agency** — pause, change speed, issue decrees, appoint a ruler, assassinate a ruler, or seize direct control of the throne. You can also release control and become a pure observer again.
- **Consequence** — stability, food, gold, public mood, and ruler approval are coupled. Crackdowns calm the island but make people miserable; festivals make people happy but cost gold.
- **Deterministic simulation** — the engine runs on a seed, so a scenario can be reproduced, tested, and shared.

## What exists in v0.1.0 (the foundation)

This repository contains a **playable foundation**, not a finished game:

- A pure-Java simulation engine with no Android dependencies (`core`).
- A text-based desktop runner for testing and balancing (`desktop`).
- A native Android UI written in Java (`app`).
- A clean intervention API (`OmatydomGame`) used by both front-ends.

Planned next: map/tile rendering, character portraits, save/load, diplomacy between factions, a schema-based event system, modding support, and a richer Android UI with fragments and Material components.

---

## Architecture

```
┌──────────────┐        ┌──────────────────┐
│   desktop    │        │       app        │
│  (CLI demo)  │        │ (Android UI)    │
└──────┬───────┘        └────────┬─────────┘
       │                         │
       └──────────┬──────────────┘
                  ▼
        ┌───────────────────┐
        │       core        │
        │  pure Java engine │
        │  model + systems  │
        └───────────────────┘
```

| Module | Type | Purpose |
| --- | --- | --- |
| `core` | `java-library` | All game rules: citizens, ruler, world, events, decrees, simulation loop. No framework dependencies. |
| `desktop` | Java application | Command-line front-end used to play-test the simulation quickly. |
| `app` | Android application | Native Android UI that drives the same `core` engine. |

The frontend never contains game rules. It only calls `OmatydomGame` and renders the resulting `World` state. This keeps the simulation testable on a desktop JVM and reusable on Android.

### Key classes

- `OmatydomGame` — public facade: create a game, tick time, pause/speed, choose/assassinate a ruler, take/release control, issue decrees.
- `SimulationEngine` — advances one day: economy, ageing, health, happiness, belief drift, births, deaths, coups, succession, random events.
- `World` — the island state: citizens, ruler, gold, food, stability, day counter, event history.
- `Citizen` / `Ruler` — the people and the throne.
- `Decree`, `GameEvent`, `EventType` — player actions and the chronicle of what happened.
- `Race`, `Religion`, `Ideology`, `Gender` — the diversity of the island.

---

## Project layout

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
│       └── model/
│           ├── Citizen.java
│           ├── Ruler.java
│           ├── Race.java
│           ├── Religion.java
│           ├── Ideology.java
│           └── Gender.java
├── desktop/
│   ├── build.gradle
│   └── src/main/java/com/dotee313/omatydom/desktop/DesktopLauncher.java
└── app/
    ├── build.gradle
    ├── src/main/AndroidManifest.xml
    └── src/main/
        ├── java/com/dotee313/omatydom/android/
        │   ├── MainActivity.java
        │   └── EventAdapter.java
        └── res/
            ├── layout/activity_main.xml
            ├── layout/event_item.xml
            └── values/strings.xml, themes.xml
```

---

## Build and run

Requirements: **JDK 17**, **Gradle 8.x**, and for the Android app **Android SDK 34** (or simply Android Studio).

### Android

1. Open Android Studio → **Open** → select the `Omatydom` folder.
2. Let Gradle sync (it will download the Android Gradle Plugin automatically).
3. Run the `app` configuration on an emulator or device (API 21+).

Or from a terminal with Gradle installed:

```bash
gradle :app:assembleDebug
```

### Desktop demo

```bash
gradle :desktop:run                 # random seed
gradle :desktop:run --args="12345"  # fixed seed (reproducible island)
```

### Desktop commands

| Command | Description |
| --- | --- |
| `help` | Show all commands. |
| `status` | Day, population, stability, gold, food, ruler, mood. |
| `events [n]` | Show the last `n` chronicle entries (default 10). |
| `step [n]` / `run [n]` | Advance `n` days (default 1). |
| `pause` / `resume` | Stop or resume the automatic clock. |
| `speed n` | Set how many days pass per tick (1–10). |
| `rulers` | List eligible adult candidates. |
| `choose <id>` | Appoint a candidate as ruler. |
| `assassinate` | Remove the current ruler and trigger a succession. |
| `control` / `auto` | Take direct control of the throne / give it back. |
| `decree <NAME>` | Issue a decree (`FESTIVAL`, `TAX_CUT`, `CRACKDOWN`, …). |
| `quit` | Exit. |

Decree names are listed by typing `decree` with no argument.

---

## Roadmap

- [ ] Save/load (serialize `World` to JSON).
- [ ] Tile map and visual simulation of citizens moving on the island.
- [ ] Factions with relations, rivalries, and wars.
- [ ] Data-driven event definitions instead of hard-coded events.
- [ ] Portrait generator and character screens.
- [ ] Android UI rewritten with fragments + Material 3.
- [ ] Tutorial scenario and challenge modes ("keep the island stable for 10 years").

---

## ملخص بالعربية

**أوماتيدوم** لعبة محاكاة حياة تدور في جزيرة، حيث تراقب مجتمعًا حيًا وتشكّله. الزمن يمضي تلقائيًا، ولك أن تتدخل أو أن تترك الأحداث تجري من نفسها.

سكان الجزيرة مختلفون: أعراق وديانات وأيديولوجيات متعددة، وعلى رأس الأمة حاكم. تستطيع اختيار من يحكم، أو اغتياله، أو تولّي زمام قراراته مباشرة. لا تُجبر على إدارة كل تفصيلة: يمكنك التوجيه، والتلاعب، أو مجرد المشاهدة بينما يعيش الناس ويعتقدون ويتطورون.

المشروع مقسّم إلى ثلاثة أجزاء: `core` (محرك المحاكاة بلغة جافا خالصة)، و`desktop` (واجهة سطر أوامر للتجربة)، و`app` (واجهة أندرويد أصلية). افتح المجلد في Android Studio وشغّل تطبيق `app`، أو شغّل النسخة المكتبية باستخدام `gradle :desktop:run`.

---

## Contributing

Ideas, balancing suggestions, event packs, and pull requests are welcome. Please keep all game rules inside `core` so both front-ends stay in sync.

## License

To be decided by the project owner.
