package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pressing the hotbar key of a wind charge (or pearl) throws it straight away and keeps your weapon in hand. */
final class QuickUse {
    private QuickUse() { }

    /** Start of every client tick, before the game handles the hotbar keys. */
    static void tick(Minecraft mc) {
        Config.QuickUse c = Config.get.quickUse;
        LocalPlayer p = mc.player;
        MultiPlayerGameMode gm = mc.gameMode;
        if (!c.enabled || p == null || gm == null || Compat.screenOpen(mc) || p.isSpectator()) return;
        Inventory inv = p.getInventory();
        KeyMapping[] keys = mc.options.keyHotbarSlots;
        for (int i = 0; i < keys.length && i < Inventory.getSelectionSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!wanted(c, s) || !keys[i].consumeClick()) continue;
            while (keys[i].consumeClick()) { }
            if (p.getCooldowns().isOnCooldown(s) || p.isUsingItem()) continue;
            int held = inv.getSelectedSlot();
            inv.setSelectedSlot(i);
            gm.useItem(p, InteractionHand.MAIN_HAND);
            Compat.swing(p);
            inv.setSelectedSlot(held);
            ((MultiPlayerGameModeAccessor) gm).duelkit$ensureHasSentCarriedItem();
        }
    }

    private static boolean wanted(Config.QuickUse c, ItemStack s) {
        return c.windCharges && s.is(Items.WIND_CHARGE) || c.enderPearls && s.is(Items.ENDER_PEARL);
    }
}
