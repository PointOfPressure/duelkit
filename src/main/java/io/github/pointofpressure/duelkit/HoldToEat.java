package io.github.pointofpressure.duelkit;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Hold the hotbar key of your apples: it switches to them and eats; let go and your previous slot comes back. */
final class HoldToEat {
    private static int eatingSlot = -1, previous = -1;

    private HoldToEat() { }

    /** Start of every client tick, before the game handles the hotbar and use keys. */
    static void tick(Minecraft mc) {
        Config.HoldToEat c = Config.get.holdToEat;
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) {
            eatingSlot = previous = -1;
            return;
        }
        KeyMapping[] keys = mc.options.keyHotbarSlots;
        Inventory inv = p.getInventory();
        if (eatingSlot >= 0) {
            while (keys[eatingSlot].consumeClick()) { }
            if (Compat.keyHeld(mc, keys[eatingSlot]) && !Compat.screenOpen(mc) && wanted(c, inv.getItem(eatingSlot))) {
                if (inv.getSelectedSlot() != eatingSlot) inv.setSelectedSlot(eatingSlot);
                mc.options.keyUse.setDown(true);
                return;
            }
            // let go (or ran out): stop eating and always go back
            mc.options.keyUse.setDown(false);
            if (p.isUsingItem()) mc.gameMode.releaseUsingItem(p);
            if (previous >= 0) inv.setSelectedSlot(previous);
            eatingSlot = previous = -1;
            return;
        }
        if (!c.enabled || Compat.screenOpen(mc)) return;
        for (int i = 0; i < keys.length && i < Inventory.getSelectionSize(); i++) {
            if (!wanted(c, inv.getItem(i)) || !(keys[i].consumeClick() || Compat.keyHeld(mc, keys[i]))) continue;
            while (keys[i].consumeClick()) { }
            previous = inv.getSelectedSlot() == i ? -1 : inv.getSelectedSlot();
            inv.setSelectedSlot(i);
            eatingSlot = i;
            mc.options.keyUse.setDown(true);
            return;
        }
    }

    private static boolean wanted(Config.HoldToEat c, ItemStack s) {
        if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE)) return c.goldenApples;
        return c.anyFood && s.has(DataComponents.FOOD);
    }
}
