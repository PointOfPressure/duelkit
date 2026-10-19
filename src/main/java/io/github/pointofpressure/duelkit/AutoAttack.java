package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MinecraftInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Attacks whatever is under the crosshair as soon as the held weapon is charged, through the normal attack path. */
public final class AutoAttack {
    private AutoAttack() { }

    /** Start of every client tick, before the game handles the attack key. */
    public static void tick(Minecraft mc) {
        Config.AutoAttack c = Config.get.autoAttack;
        LocalPlayer p = mc.player;
        if (!c.enabled || p == null || mc.gameMode == null || Compat.screenOpen(mc)) return;
        if (!p.isAlive() || p.isSpectator() || p.isUsingItem()) return;
        if (!(mc.hitResult instanceof EntityHitResult hit) || !wanted(c, hit.getEntity())) return;
        if (c.weaponsOnly && !Weapons.isWeapon(p.getMainHandItem())) return;
        if (p.getAttackStrengthScale((float) c.leadTicks) < c.minCharge) return;
        ((MinecraftInvoker) mc).duelkit$startAttack();
    }

    private static boolean wanted(Config.AutoAttack c, Entity e) {
        if (!(e instanceof LivingEntity le) || !le.isAlive() || e instanceof ArmorStand) return false;
        if (e instanceof Player pl) return c.players && !pl.isSpectator();
        if (e instanceof Enemy || e.getType().getCategory() == MobCategory.MONSTER) return c.hostiles;
        return c.others;
    }
}
