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
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Settings screen (Mod Menu → Duelkit). Only loaded when YACL is installed. */
final class ConfigScreen {
    private static final List<String> KINDS = List.of("sword", "axe", "spear", "mace", "trident", "empty");

    private ConfigScreen() { }

    static Screen create(Screen parent) {
        Config.Swaps s = Config.get.swaps, ds = new Config.Swaps();
        Config.AutoAttack a = Config.get.autoAttack, da = new Config.AutoAttack();

        OptionGroup.Builder from = group("Swap from", "Swaps only start while holding one of these.");
        for (String k : KINDS) {
            from.option(bool(Character.toUpperCase(k.charAt(0)) + k.substring(1), null, ds.from.contains(k),
                    () -> s.from.contains(k), v -> { s.from.remove(k); if (v) s.from.add(k); }));
        }

        ConfigCategory swaps = ConfigCategory.createBuilder()
                .name(text("Swaps"))
                .option(bool("Enabled", "Every swap on this page.", ds.enabled, () -> s.enabled, v -> s.enabled = v))
                .group(from.build())
                .group(group("Reach", null)
                        .option(Option.<Config.Reach>createBuilder()
                                .name(text("Spear reach"))
                                .description(OptionDescription.of(text("Needs a spear in the hotbar. When needed: up close it is a normal hit; past your weapon's reach the hit goes through the spear slot and lands from spear range. Always: every hit goes through the spear slot.")))
                                .binding(ds.reach, () -> s.reach, v -> s.reach = v)
                                .controller(o -> EnumControllerBuilder.create(o).enumClass(Config.Reach.class)
                                        .formatValue(v -> text(Character.toUpperCase(Duelkit.reachName(v).charAt(0)) + Duelkit.reachName(v).substring(1))))
                                .build())
                        .build())
                .group(group("Mace", null)
                        .option(bool("Smash", "Falling: hit with the Density mace, else any mace.", ds.smash, () -> s.smash, v -> s.smash = v))
                        .option(Option.<Double>createBuilder()
                                .name(text("Minimum fall"))
                                .description(OptionDescription.of(text("Blocks fallen before Smash kicks in.")))
                                .binding(ds.maceMinFall, () -> s.maceMinFall, v -> s.maceMinFall = v)
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(0.0, 10.0).step(0.5)
                                        .formatValue(v -> text(String.format("%.1f blocks", v))))
                                .build())
                        .option(bool("Smash mobs", null, ds.smashMobs, () -> s.smashMobs, v -> s.smashMobs = v))
                        .option(bool("Breach", "On the ground: hit players with the Breach mace instead of your weapon.", ds.breach, () -> s.breach, v -> s.breach = v))
                        .option(bool("Breach mobs", null, ds.breachMobs, () -> s.breachMobs, v -> s.breachMobs = v))
                        .build())
                .group(group("Shields", null)
                        .option(bool("Shield break", "Raised shield facing you: hit with an axe.", ds.shieldBreak, () -> s.shieldBreak, v -> s.shieldBreak = v))
                        .option(bool("Stun slam", "Shield break while falling: the mace follows in the same tick.", ds.stunSlam, () -> s.stunSlam, v -> s.stunSlam = v))
                        .build())
                .group(group("Return", null)
                        .option(bool("Natural", "Keep the swapped weapon for a few ticks instead of returning the same tick.", ds.natural, () -> s.natural, v -> s.natural = v))
                        .option(ticks("Natural min", ds.naturalMinTicks, () -> s.naturalMinTicks, v -> s.naturalMinTicks = v))
                        .option(ticks("Natural max", ds.naturalMaxTicks, () -> s.naturalMaxTicks, v -> s.naturalMaxTicks = v))
                        .build())
                .build();

        ConfigCategory auto = ConfigCategory.createBuilder()
                .name(text("Auto attack"))
                .option(bool("Enabled", "Attack whatever is under the crosshair once charged.", da.enabled, () -> a.enabled, v -> a.enabled = v))
                .group(group("Targets", null)
                        .option(bool("Players", null, da.players, () -> a.players, v -> a.players = v))
                        .option(bool("Hostile mobs", null, da.hostiles, () -> a.hostiles, v -> a.hostiles = v))
                        .option(bool("Other mobs", "Animals, villagers, golems. Never armour stands.", da.others, () -> a.others, v -> a.others = v))
                        .build())
                .group(group("Timing", null)
                        .option(bool("Weapons only", "Only while holding a sword, axe, spear, mace or trident.", da.weaponsOnly, () -> a.weaponsOnly, v -> a.weaponsOnly = v))
                        .option(Option.<Double>createBuilder()
                                .name(text("Charge"))
                                .description(OptionDescription.of(text("Attack charge to wait for.")))
                                .binding(da.minCharge, () -> a.minCharge, v -> a.minCharge = v)
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(0.0, 1.0).step(0.05)
                                        .formatValue(v -> text(Math.round(v * 100) + "%")))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(text("Lead"))
                                .description(OptionDescription.of(text("Ticks ahead the charge is counted, so the hit lands on the server's first charged tick.")))
                                .binding(da.leadTicks, () -> a.leadTicks, v -> a.leadTicks = v)
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(0.0, 2.0).step(0.25)
                                        .formatValue(v -> text(String.format("%.2f ticks", v))))
                                .build())
                        .build())
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(text("Duelkit"))
                .category(swaps)
                .category(auto)
                .save(Config::save)
                .build()
                .generateScreen(parent);
    }

    private static Component text(String s) {
        return Component.literal(s);
    }

    private static OptionGroup.Builder group(String name, String description) {
        OptionGroup.Builder g = OptionGroup.createBuilder().name(text(name));
        return description == null ? g : g.description(OptionDescription.of(text(description)));
    }

    private static Option<Boolean> bool(String name, String description, boolean def, Supplier<Boolean> get, Consumer<Boolean> set) {
        Option.Builder<Boolean> o = Option.<Boolean>createBuilder().name(text(name));
        if (description != null) o.description(OptionDescription.of(text(description)));
        return o.binding(def, get, set).controller(TickBoxControllerBuilder::create).build();
    }

    private static Option<Integer> ticks(String name, int def, Supplier<Integer> get, Consumer<Integer> set) {
        return Option.<Integer>createBuilder()
                .name(text(name))
                .binding(def, get, set)
                .controller(o -> IntegerSliderControllerBuilder.create(o).range(1, 20).step(1)
                        .formatValue(v -> text(v + (v == 1 ? " tick" : " ticks"))))
                .build();
    }
}
