package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MinecraftAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;

/** Attacks whatever is under the crosshair as soon as the held weapon is charged, through the normal attack path. */
final class AutoAttack {
    private AutoAttack() { }

    /** Start of every client tick, before the game handles the attack key. */
    static void tick(Minecraft mc) {
        Config.AutoAttack c = Config.get.autoAttack;
        LocalPlayer p = mc.player;
        if (!c.enabled || p == null || mc.gameMode == null || Compat.screenOpen(mc)) return;
        if (!p.isAlive() || p.isSpectator() || (p.isUsingItem() && !c.whileUsingItem)) return;
        if (c.onlyWhileAttackHeld && !mc.options.keyAttack.isDown()) return;
        if (!(mc.hitResult instanceof EntityHitResult hit) || !Targets.matches(hit.getEntity(), p, c.players, c.hostiles, c.others)) return;
        if (c.weaponsOnly && !Weapons.isWeapon(p.getMainHandItem())) return;
        if (p.getAttackStrengthScale((float) c.leadTicks) < c.charge) return;
        ((MinecraftAccess) mc).duelkit$startAttack();
    }
}
