package io.github.pointofpressure.duelkit;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Duelkit implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("duelkit");
    private static final KeyMapping.Category KEYS = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("duelkit", "keys"));

    @Override
    public void onInitializeClient() {
        Config.load();
        Config.Swaps s = Config.get.swaps;
        toggle("swaps", GLFW.GLFW_KEY_T, () -> s.enabled = !s.enabled, () -> "Swaps: " + onOff(s.enabled));
        toggle("reach", InputConstants.UNKNOWN.getValue(), () -> s.reach = !s.reach, () -> "Spear reach: " + onOff(s.reach));
        toggle("breach", GLFW.GLFW_KEY_RIGHT_BRACKET, () -> s.breach = !s.breach, () -> "Breach swap: " + onOff(s.breach));
        toggle("natural", GLFW.GLFW_KEY_0, () -> s.natural = !s.natural, () -> "Swap return: " + (s.natural ? "natural" : "perfect"));
        ClientTickEvents.START_CLIENT_TICK.register(Swaps::tick);
    }

    private static String onOff(boolean b) {
        return b ? "on" : "off";
    }

    private static void toggle(String name, int key, Runnable flip, Supplier<String> message) {
        KeyMapping k = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.duelkit." + name, InputConstants.Type.KEYSYM, key, KEYS));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (k.consumeClick()) {
                flip.run();
                Config.save();
                if (mc.player != null) mc.player.displayClientMessage(Component.literal(message.get()), true);
            }
        });
    }
}
