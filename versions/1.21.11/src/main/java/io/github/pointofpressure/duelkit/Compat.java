package io.github.pointofpressure.duelkit;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.AttackRange;

/** The few calls that differ between Minecraft versions. 1.21.11. */
final class Compat {
    private Compat() { }

    static KeyMapping key(String name, int key, KeyMapping.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(name, InputConstants.Type.KEYSYM, key, category));
    }

    static void actionBar(LocalPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    static void swing(LocalPlayer p) {
        p.swing(InteractionHand.MAIN_HAND);
    }

    static boolean screenOpen(Minecraft mc) {
        return mc.screen != null;
    }

    static float maxRange(AttackRange r) {
        return r.maxRange();
    }

    /** Spear range with no minimum distance. */
    static AttackRange reachOf(AttackRange spear) {
        return new AttackRange(0, spear.maxRange(), 0, spear.maxCreativeRange(), spear.hitboxMargin(), spear.mobFactor());
    }
}
