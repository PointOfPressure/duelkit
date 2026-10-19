package io.github.pointofpressure.duelkit;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Settings screen (Mod Menu → Duelkit). One tab per feature. Only loaded when YACL is installed. */
final class ConfigScreen {
    private static final List<String> KINDS = List.of("sword", "axe", "spear", "mace", "trident", "empty");
    private static final List<String> KIND_NAMES = List.of("Sword", "Axe", "Spear", "Mace", "Trident", "Empty hand");

    private ConfigScreen() { }

    static Screen create(Screen parent) {
        Config c = Config.get, d = new Config();
        return YetAnotherConfigLib.createBuilder()
                .title(text("Duelkit"))
                .category(swaps(c.swaps, d.swaps))
                .category(autoAttack(c.autoAttack, d.autoAttack))
                .category(aimAssist(c.aimAssist, d.aimAssist))
                .category(utility(c, d))
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory swaps(Config.Swaps s, Config.Swaps d) {
        OptionGroup.Builder holding = group("Only while holding", "Swaps only happen while one of these is in your hand.");
        for (int i = 0; i < KINDS.size(); i++) {
            String k = KINDS.get(i);
            holding.option(bool(KIND_NAMES.get(i), null, d.holding.contains(k), () -> s.holding.contains(k),
                    v -> { s.holding.remove(k); if (v) s.holding.add(k); }));
        }
        return ConfigCategory.createBuilder()
                .name(text("Swaps"))
                .tooltip(text("Lands each hit with the right weapon, then puts yours back before the server notices the change."))
                .option(bool("Enabled", null, d.enabled, () -> s.enabled, v -> s.enabled = v))
                .group(holding.build())
                .group(group("Spear reach", "Needs a spear in the hotbar.")
                        .option(choice("Spear reach", "When out of reach: up close it is a normal hit; past your weapon's reach the hit goes through the spear and lands from spear range. Every hit: always goes through the spear.",
                                Config.SpearReach.class, d.spearReach, () -> s.spearReach, v -> s.spearReach = v, r -> r.label))
                        .build())
                .group(group("Mace", null)
                        .option(bool("Mace when falling", "Hit with the Density mace, or any mace.", d.maceWhenFalling, () -> s.maceWhenFalling, v -> s.maceWhenFalling = v))
                        .option(decimal("Minimum fall", "How far you must have fallen.", d.minFall, () -> s.minFall, v -> s.minFall = v, 0, 10, 0.5, v -> String.format("%.1f blocks", v)))
                        .option(bool("…on mobs too", null, d.maceOnMobs, () -> s.maceOnMobs, v -> s.maceOnMobs = v))
                        .option(bool("Breach mace on the ground", "Hit players with the Breach mace instead of your weapon.", d.breachOnGround, () -> s.breachOnGround, v -> s.breachOnGround = v))
                        .option(bool("…on mobs too", null, d.breachOnMobs, () -> s.breachOnMobs, v -> s.breachOnMobs = v))
                        .build())
                .group(group("Shields", null)
                        .option(bool("Axe on raised shields", "Only when the shield faces you.", d.axeOnShields, () -> s.axeOnShields, v -> s.axeOnShields = v))
                        .option(bool("Mace after the axe when falling", "Stun slam: axe drops the shield, mace lands in the same tick.", d.stunSlam, () -> s.stunSlam, v -> s.stunSlam = v))
                        .build())
                .group(group("Swap back", null)
                        .option(choice("Swap back", "Same tick: your weapon is back before the next server tick. After a delay: the swapped weapon stays in hand for a moment.",
                                Config.SwapBack.class, d.swapBack, () -> s.swapBack, v -> s.swapBack = v, b -> b.label))
                        .option(ticks("Shortest delay", d.delayMin, () -> s.delayMin, v -> s.delayMin = v))
                        .option(ticks("Longest delay", d.delayMax, () -> s.delayMax, v -> s.delayMax = v))
                        .build())
                .group(advanced()
                        .option(bool("Shield must face you", "Only swap to the axe when the raised shield covers you. Off: any raised shield.", d.shieldFacingCheck, () -> s.shieldFacingCheck, v -> s.shieldFacingCheck = v))
                        .option(bool("Log each hit", "Writes what every hit did to logs/latest.log. For troubleshooting.", d.debugLog, () -> s.debugLog, v -> s.debugLog = v))
                        .build())
                .build();
    }

    private static ConfigCategory autoAttack(Config.AutoAttack a, Config.AutoAttack d) {
        return ConfigCategory.createBuilder()
                .name(text("Auto attack"))
                .tooltip(text("Attacks whatever is under your crosshair as soon as your weapon is charged."))
                .option(bool("Enabled", null, d.enabled, () -> a.enabled, v -> a.enabled = v))
                .group(group("Targets", null)
                        .option(bool("Players", null, d.players, () -> a.players, v -> a.players = v))
                        .option(bool("Hostile mobs", null, d.hostiles, () -> a.hostiles, v -> a.hostiles = v))
                        .option(bool("Other mobs", "Animals, villagers, golems. Never armour stands.", d.others, () -> a.others, v -> a.others = v))
                        .build())
                .group(advanced()
                        .option(decimal("Charge", "How charged your weapon must be.", d.charge, () -> a.charge, v -> a.charge = v, 0, 1, 0.05, v -> Math.round(v * 100) + "%"))
                        .option(decimal("Head start", "Counts the charge this far ahead so the hit lands on the server's first charged tick.", d.leadTicks, () -> a.leadTicks, v -> a.leadTicks = v, 0, 2, 0.25, v -> String.format("%.2f ticks", v)))
                        .option(bool("Only holding a weapon", "Sword, axe, spear, mace or trident.", d.weaponsOnly, () -> a.weaponsOnly, v -> a.weaponsOnly = v))
                        .option(bool("Only while attack is held", null, d.onlyWhileAttackHeld, () -> a.onlyWhileAttackHeld, v -> a.onlyWhileAttackHeld = v))
                        .option(bool("While eating or blocking", null, d.whileUsingItem, () -> a.whileUsingItem, v -> a.whileUsingItem = v))
                        .build())
                .build();
    }

    private static ConfigCategory aimAssist(Config.AimAssist a, Config.AimAssist d) {
        return ConfigCategory.createBuilder()
                .name(text("Aim assist"))
                .tooltip(text("While holding a weapon. Only adds to what your hand is doing: never moves you on its own, never pulls against you. Never attacks."))
                .option(bool("Enabled", null, d.enabled, () -> a.enabled, v -> a.enabled = v))
                .group(group("Targets", null)
                        .option(bool("Players", null, d.players, () -> a.players, v -> a.players = v))
                        .option(bool("Hostile mobs", null, d.hostiles, () -> a.hostiles, v -> a.hostiles = v))
                        .build())
                .group(group("Feel", null)
                        .option(decimal("Pull", "Moving your mouse toward the target turns you this much further toward it.", d.pull, () -> a.pull, v -> a.pull = v, 0, 2, 0.05, v -> Math.round(v * 100) + "%"))
                        .option(decimal("Stickiness", "Slows your aim by this much while it is on the target.", d.stickiness, () -> a.stickiness, v -> a.stickiness = v, 0, 0.9, 0.05, v -> Math.round(v * 100) + "%"))
                        .option(decimal("Tracking", "While on the target, follows this much of its movement across your screen.", d.tracking, () -> a.tracking, v -> a.tracking = v, 0, 1, 0.05, v -> Math.round(v * 100) + "%"))
                        .option(decimal("Cone", "Only targets this close to your crosshair.", d.cone, () -> a.cone, v -> a.cone = v, 10, 120, 5, v -> Math.round(v) + "°"))
                        .option(decimal("Range", null, d.range, () -> a.range, v -> a.range = v, 2, 8, 0.5, v -> String.format("%.1f blocks", v)))
                        .build())
                .group(group("Mace dive", "Falling onto someone: wider and longer reach, full tracking, and it pulls even with your hand still.")
                        .option(bool("Enabled", null, d.dive, () -> a.dive, v -> a.dive = v))
                        .option(decimal("Magnet", "How hard it pulls on its own while you fall.", d.diveMagnet, () -> a.diveMagnet, v -> a.diveMagnet = v, 0, 30, 0.5, v -> String.format("%.1f", v)))
                        .option(decimal("From a fall of", null, d.diveMinFall, () -> a.diveMinFall, v -> a.diveMinFall = v, 1, 20, 0.5, v -> String.format("%.1f blocks", v)))
                        .option(decimal("Cone", null, d.diveCone, () -> a.diveCone, v -> a.diveCone = v, 10, 180, 5, v -> Math.round(v) + "°"))
                        .option(decimal("Range", null, d.diveRange, () -> a.diveRange, v -> a.diveRange = v, 3, 32, 1, v -> String.format("%.0f blocks", v)))
                        .option(decimal("Tracking", null, d.diveTracking, () -> a.diveTracking, v -> a.diveTracking = v, 0, 1, 0.05, v -> Math.round(v * 100) + "%"))
                        .build())
                .group(advanced()
                        .option(decimal("Aim inset", "How far inside the hitbox edge it aims.", d.inset, () -> a.inset, v -> a.inset = v, 0, 0.3, 0.01, v -> String.format("%.2f blocks", v)))
                        .option(decimal("Track within", "Tracking only kicks in this close to the hitbox.", d.trackWithin, () -> a.trackWithin, v -> a.trackWithin = v, 0, 15, 0.5, v -> String.format("%.1f°", v)))
                        .option(decimal("Keep target", "Holds the current target until it is this many times further off than a new one may be.", d.keepTarget, () -> a.keepTarget, v -> a.keepTarget = v, 1, 3, 0.1, v -> String.format("%.1f×", v)))
                        .build())
                .build();
    }

    private static ConfigCategory utility(Config c, Config d) {
        Config.FastThrow f = c.fastThrow, df = d.fastThrow;
        Config.WindJump w = c.windJump, dw = d.windJump;
        Config.BucketClutch b = c.bucketClutch, db = d.bucketClutch;
        return ConfigCategory.createBuilder()
                .name(text("Utility"))
                .tooltip(text("Fast throw, wind charge jumps and water bucket clutches."))
                .group(group("Fast throw", "Hold right click: throws the moment the item is off cooldown.")
                        .option(bool("Enabled", null, df.enabled, () -> f.enabled, v -> f.enabled = v))
                        .option(bool("Ender pearls", null, df.enderPearls, () -> f.enderPearls, v -> f.enderPearls = v))
                        .option(bool("Wind charges", null, df.windCharges, () -> f.windCharges, v -> f.windCharges = v))
                        .option(bool("Bottles o' enchanting", null, df.xpBottles, () -> f.xpBottles, v -> f.xpBottles = v))
                        .option(bool("Snowballs and eggs", null, df.snowballsAndEggs, () -> f.snowballsAndEggs, v -> f.snowballsAndEggs = v))
                        .option(bool("Splash and lingering potions", null, df.splashPotions, () -> f.splashPotions, v -> f.splashPotions = v))
                        .option(bool("Off hand too", "Throws from the off hand when the main hand is empty or a weapon.", df.offHand, () -> f.offHand, v -> f.offHand = v))
                        .build())
                .group(group("Wind jump", "Throw a wind charge at your feet: you jump as the blast arrives, so its knockback stacks on the jump.")
                        .option(bool("Enabled", null, dw.enabled, () -> w.enabled, v -> w.enabled = v))
                        .option(decimal("Look down at least", "How far below the horizon you must be looking for a throw to count.", dw.minPitch, () -> w.minPitch, v -> w.minPitch = v, 30, 90, 5, v -> Math.round(v) + "°"))
                        .option(Option.<Integer>createBuilder().name(text("Blast window"))
                                .description(OptionDescription.of(text("How long after the throw a blast still counts.")))
                                .binding(dw.windowMs, () -> w.windowMs, v -> w.windowMs = v)
                                .controller(o -> IntegerSliderControllerBuilder.create(o).range(250, 3000).step(50).formatValue(v -> text(v + " ms")))
                                .build())
                        .build())
                .group(group("Water bucket clutch", "Falling with a water bucket in hand, looking at the top of a block: places the water as soon as the block is in reach. You aim.")
                        .option(bool("Enabled", null, db.enabled, () -> b.enabled, v -> b.enabled = v))
                        .option(decimal("Minimum fall", "Ignores shorter falls.", db.minFall, () -> b.minFall, v -> b.minFall = v, 1, 20, 0.5, v -> String.format("%.1f blocks", v)))
                        .build())
                .build();
    }

    private static OptionGroup.Builder advanced() {
        return group("Advanced", "Fine tuning. The defaults are the recommended setup.").collapsed(true);
    }

    private static Component text(String s) {
        return Component.literal(s);
    }

    private static OptionGroup.Builder group(String name, String description) {
        OptionGroup.Builder g = OptionGroup.createBuilder().name(text(name));
        return description == null ? g : g.description(OptionDescription.of(text(description)));
    }

    private static <T> Option.Builder<T> option(String name, String description) {
        Option.Builder<T> o = Option.<T>createBuilder().name(text(name));
        return description == null ? o : o.description(OptionDescription.of(text(description)));
    }

    private static Option<Boolean> bool(String name, String description, boolean def, Supplier<Boolean> get, Consumer<Boolean> set) {
        return ConfigScreen.<Boolean>option(name, description).binding(def, get, set).controller(TickBoxControllerBuilder::create).build();
    }

    private static Option<Double> decimal(String name, String description, double def, Supplier<Double> get, Consumer<Double> set,
                                          double min, double max, double step, Function<Double, String> format) {
        return ConfigScreen.<Double>option(name, description).binding(def, get, set)
                .controller(o -> DoubleSliderControllerBuilder.create(o).range(min, max).step(step).formatValue(v -> text(format.apply(v))))
                .build();
    }

    private static Option<Integer> ticks(String name, int def, Supplier<Integer> get, Consumer<Integer> set) {
        return ConfigScreen.<Integer>option(name, null).binding(def, get, set)
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 20).step(1).formatValue(v -> text(v + (v == 1 ? " tick" : " ticks"))))
                .build();
    }

    private static <E extends Enum<E>> Option<E> choice(String name, String description, Class<E> type, E def, Supplier<E> get, Consumer<E> set,
                                                       Function<E, String> label) {
        return ConfigScreen.<E>option(name, description).binding(def, get, set)
                .controller(o -> EnumControllerBuilder.create(o).enumClass(type).formatValue(v -> text(label.apply(v))))
                .build();
    }
}
