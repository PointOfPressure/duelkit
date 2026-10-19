# Duelkit

Client-side PvP helpers for Fabric 1.21.11 and 26.3. Nothing that can't already be done by hand with a fast hotbar; it just does it in the same tick, every time.

## Attribute swapping

The server applies the **attributes** (attack damage, attack speed) of the item you held last tick, but the **enchantments, mace smash bonus and shield disabling** of the item in your hand when the hit arrives. Duelkit selects the right weapon just before the attack packet and puts your original slot back before the next tick, so you keep your weapon's damage, cooldown and charge.

| Swap | When | Hits with |
|---|---|---|
| Smash | falling at least `maceMinFall` blocks | Density mace, else any mace |
| Breach | not falling, target is a player | Breach mace |
| Shield break | target's shield is raised and facing you | an axe (anything with shield disabling) |
| Stun slam | shield break while falling | axe, then the mace in the same tick |
| Spear reach | always, if a spear is in your hotbar | your held weapon targets at the spear's range |

Spear reach and the mace swaps stack: hold a sword, target from spear range, land the hit with the mace.

Swaps only start while holding an item listed in `from` (default: sword, axe). Mobs only get the smash swap unless `breachMobs` is on.

## Auto attack

Attacks whatever is under your crosshair as soon as your weapon is charged, through the normal attack path, so every swap above still applies. Counts the charge half a tick ahead (`leadTicks`) so the hit arrives on the server's first fully charged tick.

Targets players and hostile mobs by default. Never armour stands, item frames or other non-living entities.

## Keys

All rebindable under Controls → Duelkit.

| Default | Toggle |
|---|---|
| `T` | swaps on / off |
| `]` | Breach swap |
| `0` | perfect / natural return |
| `J` | auto attack |
| unbound | spear reach |

## Config

`config/duelkit.json`, written on first launch.

### `swaps`

| Key | Default | |
|---|---|---|
| `enabled` | `true` | |
| `from` | `["sword", "axe"]` | held items swaps start from: `sword`, `axe`, `spear`, `mace`, `trident`, `empty` |
| `natural` | `false` | keep the swapped weapon for `naturalMinTicks`–`naturalMaxTicks` ticks instead of returning the same tick |
| `naturalMinTicks` / `naturalMaxTicks` | `2` / `4` | |
| `reach` | `true` | spear reach |
| `smash` | `true` | |
| `maceMinFall` | `1.5` | blocks |
| `smashMobs` | `true` | |
| `breach` | `true` | |
| `breachMobs` | `false` | |
| `shieldBreak` | `true` | |
| `stunSlam` | `true` | |

### `autoAttack`

| Key | Default | |
|---|---|---|
| `enabled` | `true` | |
| `players` | `true` | |
| `hostiles` | `true` | monsters: zombies, skeletons, creepers, endermen, spiders, slimes… |
| `others` | `false` | animals, villagers, golems |
| `weaponsOnly` | `false` | only while holding a sword, axe, spear, mace or trident |
| `minCharge` | `1.0` | 0–1 |
| `leadTicks` | `0.5` | |

## Servers

Same-tick swaps depend on the server applying attributes once per tick. Vanilla 1.21.11 and 26.3 do. Paper only does with `update-equipment-on-player-actions: false`. Anticheats and server rules vary; check before using it somewhere that isn't yours.

## Build

```
./gradlew build
```

One source tree in `src/`, one Gradle project per Minecraft version in `versions/<version>/`, each with a small `Compat` class for the calls that differ. Jars land in `versions/<version>/build/libs/`. Requires Fabric API.

## License

MIT
