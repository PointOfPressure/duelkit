package io.github.pointofpressure.duelkit;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.SwingAnimation;

/** The few calls that differ between Minecraft versions. 26.3. */
final class Compat {
    private Compat() { }

    static KeyMapping key(String name, int key, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(name, InputConstants.Type.KEYBOARD, key, category));
    }

    static void actionBar(LocalPlayer p, String text) {
        p.sendOverlayMessage(Component.literal(text));
    }

    static void swing(LocalPlayer p) {
        p.swing(InteractionHand.MAIN_HAND, p.getMainHandItem().getOrDefault(DataComponents.ATTACK_ANIMATION, SwingAnimation.DEFAULT), false);
    }

    static boolean screenOpen(Minecraft mc) {
        return mc.gui.screen() != null;
    }

    static float maxRange(AttackRange r) {
        return r.maxReach();
    }

    /** Spear range with no minimum distance. */
    static AttackRange reachOf(AttackRange spear) {
        return new AttackRange(0, spear.maxReach(), 0, spear.maxCreativeReach(), spear.hitboxMargin(), spear.mobFactor());
    }

    static List<AbstractWidget> widgets(Screen screen) {
        return Screens.getWidgets(screen);
    }

    static void open(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }

    /** Same as pressing a number key (or F for 40, the off hand) over a slot in the inventory screen. */
    static void swapClick(net.minecraft.client.multiplayer.MultiPlayerGameMode gm, int container, int slot, int button, LocalPlayer p) {
        gm.handleContainerInput(container, slot, button, net.minecraft.world.inventory.ContainerInput.SWAP, p);
    }
}
