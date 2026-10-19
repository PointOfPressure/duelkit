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

    public static final class Swaps {
        public boolean enabled = true;
        /** Held items that swaps start from: sword, axe, spear, mace, trident, empty. */
        public List<String> from = new ArrayList<>(List.of("sword", "axe"));
        /** Natural: the swapped weapon stays in hand for a few ticks instead of going back the same tick. */
        public boolean natural = false;
        public int naturalMinTicks = 2;
        public int naturalMaxTicks = 4;

        /** Your other weapons target at the range of the best spear in your hotbar. */
        public boolean reach = true;

        /** Falling at least maceMinFall blocks: hit with the mace (Density preferred). */
        public boolean smash = true;
        public double maceMinFall = 1.5;
        public boolean smashMobs = true;
        /** Not falling: hit players with a Breach mace. */
        public boolean breach = true;
        public boolean breachMobs = false;

        /** Raised shield facing you: hit with an axe. */
        public boolean shieldBreak = true;
        /** ...then, if falling, the mace in the same tick (stun slam). */
        public boolean stunSlam = true;
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
