package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MinecraftAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Holding use with a throwable: throw on the first tick it is off cooldown instead of vanilla's 4-tick repeat. */
final class FastThrow {
    private FastThrow() { }

    /** Start of every client tick, before the game handles the use key. */
    static void tick(Minecraft mc) {
        Config.FastThrow c = Config.get.fastThrow;
        LocalPlayer p = mc.player;
        if (!c.enabled || p == null || mc.gameMode == null || Compat.screenOpen(mc) || !mc.options.keyUse.isDown() || p.isUsingItem()) return;
        // the game tries the main hand first; the off hand only gets used when the main hand does nothing
        ItemStack s = p.getMainHandItem();
        if (!wanted(c, s)) {
            if (!c.offHand || !(s.isEmpty() || Weapons.isWeapon(s))) return;
            s = p.getOffhandItem();
            if (!wanted(c, s)) return;
        }
        if (p.getCooldowns().isOnCooldown(s)) return;
        ((MinecraftAccess) mc).duelkit$setRightClickDelay(0);
    }

    private static boolean wanted(Config.FastThrow c, ItemStack s) {
        return c.enderPearls && s.is(Items.ENDER_PEARL)
                || c.windCharges && s.is(Items.WIND_CHARGE)
                || c.xpBottles && s.is(Items.EXPERIENCE_BOTTLE)
                || c.snowballsAndEggs && (s.is(Items.SNOWBALL) || s.is(Items.EGG) || s.is(Items.BLUE_EGG) || s.is(Items.BROWN_EGG))
                || c.splashPotions && (s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION));
    }
}
