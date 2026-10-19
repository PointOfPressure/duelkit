# Duelkit

Client-side PvP helpers for Fabric 1.21.11 and 26.3. Nothing a fast hand can't already do; it just does it on the right tick, every time. Each feature has its own toggle key, and everything is configurable with the recommended setup as the default.

| Feature | What it does | Key |
|---|---|---|
| Swaps | Lands each hit with the right weapon, then puts yours back before the next server tick | `T` |
| Spear reach | Past your weapon's reach, the hit goes through a hotbar spear and lands from spear range | unbound (cycles off → when out of reach → every hit) |
| Auto attack | Attacks what's under your crosshair as soon as your weapon is charged | `J` |
| Aim assist | Console-style: adds to your own aim, never moves you on its own; stronger during mace dives | `[` |
| Fast throw | Hold right click: pearls, wind charges and bottles throw the tick they come off cooldown | unbound |
| Wind jump | Throw a wind charge at your feet: you jump as the blast arrives, so the knockback stacks | `-` |
| Water bucket clutch | Falling with a water bucket, looking at a block top: places it as soon as the block is in reach | unbound |

`]` toggles Breach on the ground, `0` toggles swap-back timing. All keys are under Controls → Duelkit.

## Swaps

The server applies the **attributes** (damage, attack speed) of the item you held last tick, but the **enchantments, mace smash bonus and shield disabling** of the item in hand when the hit arrives.

| Swap | When | Hits with | Default |
|---|---|---|---|
| Spear reach | target past your weapon's reach, spear in hotbar | your weapon, from spear range, via the spear slot | when out of reach |
| Mace when falling | fallen at least 1.5 blocks | Density mace, else any mace | on |
| Axe on shields | raised shield facing you | an axe (anything that disables shields) | on |
| Stun slam | axe on a shield while falling | axe, then the mace in the same tick | on |
| Breach on the ground | target is a player | Breach mace | off |

On the ground it is a plain hit with your weapon unless the target is out of its reach. Everything stacks: sword attributes, spear range, mace hit.

## Aim assist

Only adds to what your hand does. **Pull**: moving your mouse toward the target turns you a bit further. **Stickiness**: slows your aim while it's on the target. **Tracking**: follows part of the target's movement while you're on it. Keeps one target instead of flicking between people. Never attacks.

**Mace dive**: once you've fallen 3 blocks, the cone and range widen, tracking is full, and it pulls toward the target even with your hand still.

## Settings

Mod Menu → Duelkit, one tab per feature, with an **Advanced** section on each for fine tuning. Needs [YetAnotherConfigLib](https://modrinth.com/mod/yacl) and [Mod Menu](https://modrinth.com/mod/modmenu); both optional. Everything is also in `config/duelkit.json`.

Swaps → Advanced → **Log each hit** writes what every hit did to `logs/latest.log`.

## Servers

Same-tick swaps depend on the server applying attributes once per tick. Vanilla 1.21.11 and 26.3 do; Paper only with `update-equipment-on-player-actions: false`. Anticheats and server rules vary; check before using it somewhere that isn't yours.

## Build

```
./gradlew build
```

One source tree in `src/`, one Gradle project per Minecraft version in `versions/<version>/`, each with a small `Compat` class for the calls that differ. Jars land in `versions/<version>/build/libs/`. Requires Fabric API.

## License

MIT
