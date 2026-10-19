# Duelkit

Client-side PvP helpers for Fabric 1.21.11. Nothing that can't already be done by hand with a fast hotbar; it just does it in the same tick, every time.

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

## Keys

All rebindable under Controls → Duelkit.

| Default | Toggle |
|---|---|
| `T` | swaps on / off |
| `]` | Breach swap |
| `0` | perfect / natural return |
| unbound | spear reach |

## Config

`config/duelkit.json`, written on first launch. Swap settings live under `"swaps"`.

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

## Servers

Same-tick swaps depend on the server applying attributes once per tick. Vanilla 1.21.11 does. Paper only does with `update-equipment-on-player-actions: false`. Anticheats and server rules vary; check before using it somewhere that isn't yours.

## Build

```
./gradlew build
```

Jar in `build/libs/`. Requires Fabric API.

## License

MIT
