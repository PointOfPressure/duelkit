package io.github.pointofpressure.duelkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/** config/duelkit.json. Missing fields fall back to the defaults below. */
public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("duelkit.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Config get = new Config();

    public Swaps swaps = new Swaps();
    public AutoAttack autoAttack = new AutoAttack();

    public static final class AutoAttack {
        public boolean enabled = true;
        public boolean players = true;
        /** Monsters: zombies, skeletons, creepers, endermen, spiders, slimes... */
        public boolean hostiles = true;
        /** Everything else alive: animals, villagers, golems. Never armour stands. */
        public boolean others = false;
        /** Only while holding a sword, axe, spear, mace or trident. */
        public boolean weaponsOnly = false;
        /** Attack at this charge (0-1)... */
        public double minCharge = 1.0;
        /** ...counted this many ticks ahead, so the hit lands on the server's first fully charged tick. */
        public double leadTicks = 0.5;
    }

    public static final class Swaps {
        public boolean enabled = true;
        /** Held items that swaps start from: sword, axe, spear, mace, trident, empty. */
        public List<String> from = new ArrayList<>(List.of("sword", "axe"));
        /** Natural: the swapped weapon stays in hand for a few ticks instead of going back the same tick. */
        public boolean natural = false;
        public int naturalMinTicks = 2;
        public int naturalMaxTicks = 4;

        /** Spear reach: target from the range of a hotbar spear, passing through the spear slot before the hit. */
        public Reach reach = Reach.WHEN_NEEDED;

        /** Falling at least maceMinFall blocks: hit with the mace (Density preferred). */
        public boolean smash = true;
        public double maceMinFall = 1.5;
        public boolean smashMobs = true;
        /** On the ground: hit players with a Breach mace instead of your weapon. */
        public boolean breach = false;
        public boolean breachMobs = false;

        /** Raised shield facing you: hit with an axe. */
        public boolean shieldBreak = true;
        /** ...then, if falling, the mace in the same tick (stun slam). */
        public boolean stunSlam = true;
    }

    public enum Reach {
        /** Normal reach. */
        OFF,
        /** Spear range for targeting; the spear swap only happens when the target is past normal reach. */
        WHEN_NEEDED,
        /** Every hit goes through the spear slot. */
        ALWAYS
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) get = GSON.fromJson(Files.readString(FILE), Config.class);
        } catch (Exception e) {
            Duelkit.LOG.warn("config/duelkit.json unreadable, using defaults: {}", e.toString());
            get = new Config();
            return;
        }
        if (get == null) get = new Config();
        save();
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(get));
        } catch (Exception e) {
            Duelkit.LOG.warn("could not save config/duelkit.json: {}", e.toString());
        }
    }
}
