# Whack Me

[![CI](https://github.com/Despical/WhackMe/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/WhackMe/actions/workflows/build.yml)
![Java 25](https://img.shields.io/badge/Java-25-007396.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.6.1-079ec0?logo=gradle&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62b47a)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Whack Me is a fast-paced, single-player whack-a-block minigame for Minecraft
servers.

The player enters a configured arena and tries to hit as many green point
blocks as possible before the timer expires while avoiding red blocks. Correct
hits increase the score and streak; incorrect hits reduce the score and reset
the current streak. At the end of a run, Whack Me reports the final score,
personal best, longest streak, success rate, and correct and incorrect hits.

---

## Features

- Single-player arenas with setup menus, editing tools, join signs, and runtime
  state handling.
- Configurable point-block locations, appearance, timing, scoring, and arena
  records.
- Customizable messages, scoreboards, boss bars, sounds, menus, and result
  summaries.
- Player statistics for games played, perfect runs, personal records, hit
  streaks, and positive or negative block hits.
- Flat-file storage by default, with optional MySQL persistence.
- Global leaderboard support and PlaceholderAPI placeholders.
- Optional NoteBlockAPI music with bundled songs and per-arena selection.
- Public Bukkit API for game lifecycle, player flow, and statistic changes.

---

## Requirements

- Java 25
- A Paper-compatible Minecraft server

---

## Resources

- [Documentation](https://docs.despical.dev/whack-me/)
- [Javadocs](https://javadoc.despical.dev/WhackMe/)
- [SpigotMC](https://www.spigotmc.org/resources/whack-me.104912/)
- [BuiltByBit](https://builtbybit.com/resources/whack-me.50294/)

---

## Building

Clone the repository:

```bash
git clone https://github.com/Despical/WhackMe.git
cd WhackMe
```

Build the packaged plugin jar:

```bash
./gradlew shadowJar
```

On Windows:

```cmd
gradlew.bat shadowJar
```

The packaged jar is normally created under `build/libs/`.

Run the full verification used during development:

```bash
./gradlew clean build javadoc
```

On Windows:

```cmd
gradlew.bat clean build javadoc
```

---

## Configuration

Configuration files are bundled in `src/main/resources` and copied to the
plugin data folder on first startup. Most server-facing behavior can be changed
without rebuilding the plugin.

Common files and directories:

- `config.yml` controls gameplay, storage, chat, command, and player behavior.
- `arenas.yml` stores arena locations, point-block settings, signs, songs, and
  records.
- `messages.yml`, `scoreboard.yml`, `bossbar.yml`, and `sounds.yml`
  control presentation.
- `mysql.yml` configures MySQL when database storage is enabled.
- `signs.yml` controls join-sign formatting.
- `menu/` contains the setup and statistics menu layouts.
- `musics/` contains bundled NoteBlockAPI songs.

---

## API

Whack Me exposes Bukkit events under
`dev.despical.whackme.api.event` for game lifecycle changes, player joins and
leaves, and statistic updates.

Maven:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.Despical</groupId>
    <artifactId>WhackMe</artifactId>
    <version>main-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Gradle:

```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly group: 'com.github.Despical', name: 'WhackMe', version: 'main-SNAPSHOT'
}
```

---

## Integrations

Optional integrations declared by the plugin include:

- PlaceholderAPI
- NoteBlockAPI
- Multiverse-Core
- SlimeWorldManager
- SlimeWorldPlugin
- MultiWorld
- My_Worlds

---

## Security

Do not open a public issue for a discovered vulnerability. Read
[SECURITY.md](SECURITY.md) for responsible disclosure instructions.

---

## Contributing

Community pull requests are welcome. Keep changes focused, preserve the
established code style, and run the full Gradle verification before submitting.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for the complete contribution process
and follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) when participating in the
project.

---

## License

Whack Me is free software licensed under the
[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).

See [LICENSE](LICENSE) for the full license text.
