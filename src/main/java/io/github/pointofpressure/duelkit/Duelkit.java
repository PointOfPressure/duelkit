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

    @Override
    public void onInitializeClient() {
        Config.load();
        Config.Swaps s = Config.get.swaps;
        toggle("swaps", InputConstants.KEY_T, () -> s.enabled = !s.enabled, () -> "Swaps: " + onOff(s.enabled));
        toggle("reach", InputConstants.UNKNOWN.getValue(), () -> s.reach = !s.reach, () -> "Spear reach: " + onOff(s.reach));
        toggle("breach", InputConstants.KEY_RBRACKET, () -> s.breach = !s.breach, () -> "Breach swap: " + onOff(s.breach));
        toggle("natural", InputConstants.KEY_0, () -> s.natural = !s.natural, () -> "Swap return: " + (s.natural ? "natural" : "perfect"));
        Config.AutoAttack a = Config.get.autoAttack;
        toggle("autoattack", InputConstants.KEY_J, () -> a.enabled = !a.enabled, () -> "Auto attack: " + onOff(a.enabled));
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            Swaps.tick(mc);
            AutoAttack.tick(mc);
        });
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
