package io.github.pointofpressure.duelkit;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

final class Targets {
    private Targets() { }

    /** Living, visible-to-you targets of the chosen kinds. Never armour stands. */
    static boolean matches(Entity e, Player self, boolean players, boolean hostiles, boolean others) {
        if (e == self || !(e instanceof LivingEntity le) || !le.isAlive() || e instanceof ArmorStand || e.isInvisibleTo(self)) return false;
        if (e instanceof Player p) return players && !p.isSpectator();
        if (e instanceof Enemy || e.getType().getCategory() == MobCategory.MONSTER) return hostiles;
        return others;
    }
}
