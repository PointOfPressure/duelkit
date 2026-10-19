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

    /** Small "D" button left of Options... in the pause menu. */
    public boolean pauseMenuButton = true;
    /** No pop-ups in the top right: chat verification warnings, recipe unlocks, advancements, tutorials. */
    public boolean hideToasts = true;
    /** Size of the totem pop animation, 1 = vanilla. */
    public double totemPopSize = 0.4;
    public Swaps swaps = new Swaps();
    public AutoAttack autoAttack = new AutoAttack();
    public AimAssist aimAssist = new AimAssist();
    public FastThrow fastThrow = new FastThrow();
    public WindJump windJump = new WindJump();
    public BucketClutch bucketClutch = new BucketClutch();
    public PearlCatch pearlCatch = new PearlCatch();
    public QuickUse quickUse = new QuickUse();
    public HoldToEat holdToEat = new HoldToEat();
    /** Hold the loadout key (K) for the loadout panel. */
    public boolean loadouts = true;

    public static final class Swaps {
        public boolean enabled = true;
        /** Held items swaps start from: sword, axe, spear, mace, trident, empty. */
        public List<String> holding = new ArrayList<>(List.of("sword", "axe"));
        public SpearReach spearReach = SpearReach.WHEN_NEEDED;
        /** Falling at least minFall blocks: hit with whichever hotbar mace deals the most damage. */
        public boolean maceWhenFalling = true;
        public double minFall = 1.5;
        public boolean maceOnMobs = true;
        /** On the ground: hit players with a Breach mace. */
        public boolean breachOnGround = false;
        public boolean breachOnMobs = false;
        /** Raised shield facing you: hit with an axe. */
        public boolean axeOnShields = true;
        /** ...then, if falling, the mace in the same tick. */
        public boolean stunSlam = true;
        /** Use the axe whenever they hold a shield, not only once it is raised: good players raise it as your hit arrives. */
        public boolean axeWhenShieldHeld = true;
        public SwapBack swapBack = SwapBack.SAME_TICK;
        public int delayMin = 2;
        public int delayMax = 4;
        // advanced
        /** Axe only when the raised shield actually faces you (shields block the front 180 degrees). */
        public boolean shieldFacingCheck = true;
        /** Write what each hit did to logs/latest.log. */
        public boolean debugLog = false;
    }

    public static final class AutoAttack {
        public boolean enabled = true;
        public boolean players = true;
        public boolean hostiles = true;
        /** Animals, villagers, golems. Never armour stands. */
        public boolean others = false;
        public boolean weaponsOnly = false;
        /** Attack charge to wait for, 0-1... */
        public double charge = 1.0;
        /** ...counted this many ticks ahead, so the hit lands on the server's first charged tick. */
        public double leadTicks = 0.5;
        /** In the air and still rising: hold the hit until you start falling, so it crits. */
        public boolean waitForCrits = true;
        // advanced
        /** Longest a hit is held back for a crit, in ticks. */
        public int critMaxWait = 8;
        /** Keep attacking while eating, drinking or blocking. */
        public boolean whileUsingItem = false;
        /** Only while the attack key is held. */
        public boolean onlyWhileAttackHeld = false;
    }

    public static final class AimAssist {
        public boolean enabled = true;
        public boolean players = true;
        public boolean hostiles = true;
        /** Cone width in degrees. */
        public double cone = 30;
        public double range = 6.0;
        /** Sphere: range is straight-line distance. Column: range is horizontal only, height is limited by verticalRange. */
        public RangeShape rangeShape = RangeShape.SPHERE;
        /** Column only: how far above or below a target may be. 0 = no limit. */
        public double verticalRange = 0;
        /** Extra turn toward the target, as a share of your own mouse movement toward it. */
        public double pull = 0.4;
        /** How much your own turning is slowed while the crosshair is on the target. */
        public double stickiness = 0.25;
        /** Share of the target's movement across your screen that is followed while you are on it. */
        public double tracking = 0.5;

        /** Mace dive: falling onto a target, the hardest aim in the game. Stronger, wider, and it pulls on its own. */
        public boolean dive = true;
        public double diveMinFall = 3.0;
        public double diveCone = 90;
        public double diveRange = 12.0;
        public double diveTracking = 1.0;
        /** Pulls toward the target even with your hand still. Fraction of the gap closed per second is 1 - e^-magnet. */
        public double diveMagnet = 6.0;

        // advanced
        /** Aim this far inside the hitbox edge, in blocks. */
        public double inset = 0.15;
        /** Tracking only applies while the crosshair is within this many degrees of the hitbox. */
        public double trackWithin = 3.0;
        /** A held target is kept until it is this many times further off-crosshair than a new one may be. */
        public double keepTarget = 1.5;
    }

    public static final class FastThrow {
        public boolean enabled = true;
        public boolean enderPearls = true;
        public boolean windCharges = true;
        public boolean xpBottles = true;
        public boolean snowballsAndEggs = false;
        public boolean splashPotions = false;
        // advanced
        /** Use the off hand when the main hand is empty or a weapon. */
        public boolean offHand = true;
    }

    public static final class WindJump {
        public boolean enabled = true;
        /** Degrees below the horizon you must be looking for a throw to count. */
        public double minPitch = 60;
        // advanced
        /** How long after the throw the blast still counts, in milliseconds. */
        public int windowMs = 1500;
    }

    public static final class BucketClutch {
        public boolean enabled = true;
        /** Only for falls longer than this many blocks. */
        public double minFall = 3.0;
        /** Skip falls that would not hurt: wind launches, slow falling, feather falling, soft landings. */
        public boolean onlyIfItHurts = true;
        /** Once landed, pick the water back up (look at it). */
        public boolean pickUp = true;
    }

    public static final class PearlCatch {
        public boolean enabled = true;
        /** Ticks between the pearl and the wind charge; 0 = same tick, straight after. */
        public int delayTicks = 0;
        /** Throw the wind charge straight down instead of after the pearl. */
        public boolean atFeet = false;
    }

    public static final class HoldToEat {
        public boolean enabled = true;
        public boolean goldenApples = true;
        public boolean anyFood = false;
    }

    public static final class QuickUse {
        public boolean enabled = true;
        public boolean windCharges = true;
        public boolean enderPearls = true;
    }

    public enum SpearReach {
        OFF("Off"), WHEN_NEEDED("When out of reach"), ALWAYS("Every hit");
        public final String label;
        SpearReach(String label) { this.label = label; }
    }

    public enum RangeShape {
        SPHERE("Sphere"), COLUMN("Column");
        public final String label;
        RangeShape(String label) { this.label = label; }
    }

    public enum SwapBack {
        SAME_TICK("Same tick"), DELAYED("After a delay");
        public final String label;
        SwapBack(String label) { this.label = label; }
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) get = GSON.fromJson(Files.readString(FILE), Config.class);
        } catch (Exception e) {
            Duelkit.LOG.warn("config/duelkit.json unreadable, using defaults: {}", e.toString());
            get = new Config();
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
