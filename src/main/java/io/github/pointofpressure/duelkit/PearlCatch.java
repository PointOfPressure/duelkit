package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Pearl catch: right after you throw a pearl, a wind charge is thrown after it the same way, so its blast catches
 * the pearl. Delay 0 is the same tick. "At your feet" instead throws it straight down to launch yourself after it.
 */
final class PearlCatch {
    private static int pending = -1;
    private static boolean throwing;

    private PearlCatch() { }

    static void onUseItem(Player player, ItemStack stack) {
        Config.PearlCatch c = Config.get.pearlCatch;
        if (!c.enabled || player != Minecraft.getInstance().player || !stack.is(Items.ENDER_PEARL)) return;
        if (throwing || player.getCooldowns().isOnCooldown(stack)) return;
        pending = Math.max(0, c.delayTicks);
    }

    /** Right after the pearl's use packet: a delay of 0 throws the charge in the same tick, straight after it. */
    static void afterUseItem(Minecraft mc) {
        if (pending == 0 && !throwing) {
            pending = -1;
            fire(mc);
        }
    }

    /** Start of every client tick. */
    static void tick(Minecraft mc) {
        if (pending < 0) return;
        if (pending-- > 0) return;
        fire(mc);
    }

    private static void fire(Minecraft mc) {
        LocalPlayer p = mc.player;
        MultiPlayerGameMode gm = mc.gameMode;
        if (p == null || gm == null) return;
        if (p.getOffhandItem().is(Items.WIND_CHARGE) && !p.getCooldowns().isOnCooldown(p.getOffhandItem())) {
            throwCharge(gm, p, InteractionHand.OFF_HAND);
            return;
        }
        Inventory inv = p.getInventory();
        int slot = Weapons.find(inv, s -> s.is(Items.WIND_CHARGE));
        if (slot < 0 || p.getCooldowns().isOnCooldown(inv.getItem(slot))) return;
        int held = inv.getSelectedSlot();
        inv.setSelectedSlot(slot);
        throwCharge(gm, p, InteractionHand.MAIN_HAND);
        inv.setSelectedSlot(held);
        ((MultiPlayerGameModeAccessor) gm).duelkit$ensureHasSentCarriedItem();
    }

    private static void throwCharge(MultiPlayerGameMode gm, LocalPlayer p, InteractionHand hand) {
        throwing = true;
        try {
            if (!Config.get.pearlCatch.atFeet) {
                gm.useItem(p, hand);
                return;
            }
            float pitch = p.getXRot();
            p.setXRot(90);
            gm.useItem(p, hand);
            p.setXRot(pitch);
        } finally {
            throwing = false;
        }
    }
}
