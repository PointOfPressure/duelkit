package io.github.pointofpressure.duelkit;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Duelkit implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("duelkit");
    private static final KeyMapping.Category KEYS = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("duelkit", "keys"));
    private static final int UNBOUND = InputConstants.UNKNOWN.getValue();

    @Override
    public void onInitializeClient() {
        Config.load();
        Config c = Config.get;
        toggle("swaps", InputConstants.KEY_T, () -> c.swaps.enabled = !c.swaps.enabled, () -> "Swaps: " + onOff(c.swaps.enabled));
        toggle("spear_reach", UNBOUND, () -> c.swaps.spearReach = next(c.swaps.spearReach), () -> "Spear reach: " + c.swaps.spearReach.label);
        toggle("breach", InputConstants.KEY_RBRACKET, () -> c.swaps.breachOnGround = !c.swaps.breachOnGround, () -> "Breach on the ground: " + onOff(c.swaps.breachOnGround));
        toggle("swap_back", InputConstants.KEY_0, () -> c.swaps.swapBack = next(c.swaps.swapBack), () -> "Swap back: " + c.swaps.swapBack.label);
        toggle("auto_attack", InputConstants.KEY_J, () -> c.autoAttack.enabled = !c.autoAttack.enabled, () -> "Auto attack: " + onOff(c.autoAttack.enabled));
        toggle("aim_assist", InputConstants.KEY_LBRACKET, () -> c.aimAssist.enabled = !c.aimAssist.enabled, () -> "Aim assist: " + onOff(c.aimAssist.enabled));
        toggle("fast_throw", UNBOUND, () -> c.fastThrow.enabled = !c.fastThrow.enabled, () -> "Fast throw: " + onOff(c.fastThrow.enabled));
        toggle("wind_jump", InputConstants.KEY_MINUS, () -> c.windJump.enabled = !c.windJump.enabled, () -> "Wind jump: " + onOff(c.windJump.enabled));
        toggle("bucket_clutch", UNBOUND, () -> c.bucketClutch.enabled = !c.bucketClutch.enabled, () -> "Water bucket clutch: " + onOff(c.bucketClutch.enabled));
        toggle("quick_use", UNBOUND, () -> c.quickUse.enabled = !c.quickUse.enabled, () -> "One-tap throw: " + onOff(c.quickUse.enabled));
        toggle("pearl_catch", UNBOUND, () -> c.pearlCatch.enabled = !c.pearlCatch.enabled, () -> "Pearl catch: " + onOff(c.pearlCatch.enabled));
        ClientTickEvents.END_CLIENT_TICK.register(BucketClutch::tick);
        PauseButton.register();
        Loadouts.load();
        KeyMapping loadoutKey = Compat.key("key.duelkit.loadouts", InputConstants.KEY_K, KEYS);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (loadoutKey.consumeClick()) {
                if (Config.get.loadouts && mc.player != null && !Compat.screenOpen(mc)) Compat.open(mc, new LoadoutScreen(loadoutKey));
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            Swaps.tick(mc);
            AutoAttack.tick(mc);
            FastThrow.tick(mc);
            PearlCatch.tick(mc);
            HoldToEat.tick(mc);
            QuickUse.tick(mc);
        });
    }

    public static void afterUseItem() {
        PearlCatch.afterUseItem(net.minecraft.client.Minecraft.getInstance());
    }

    public static void onUseItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack) {
        PearlCatch.onUseItem(player, stack);
    }

    /** Every frame, around your own mouse turn. */
    public static void beforeMouse() {
        AimAssist.beforeMouse();
    }

    public static void afterMouse() {
        AimAssist.afterMouse();
    }

    private static <E extends Enum<E>> E next(E e) {
        E[] all = e.getDeclaringClass().getEnumConstants();
        return all[(e.ordinal() + 1) % all.length];
    }

    private static String onOff(boolean b) {
        return b ? "on" : "off";
    }

    private static void toggle(String name, int key, Runnable flip, Supplier<String> message) {
        KeyMapping k = Compat.key("key.duelkit." + name, key, KEYS);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (k.consumeClick()) {
                flip.run();
                Config.save();
                if (mc.player != null) Compat.actionBar(mc.player, message.get());
            }
        });
    }
}
