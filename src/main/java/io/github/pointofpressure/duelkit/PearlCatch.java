package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Pearl catch: throw a pearl upward and a wind charge follows it; the charge (no gravity, no drag) catches the
 * slowing pearl and its blast kicks the pearl higher.
 *
 * Auto timing simulates both projectiles tick by tick with vanilla's numbers (throw speed 1.5, your current
 * momentum, pearl drag 0.99 and gravity 0.03, your own fall while you wait) for every delay up to maxDelay, and picks
 * the delay whose catch sends the pearl highest. When it fires, it aims that one charge at where your actual pearl
 * will be, so it catches at any angle. Works from the ground or mid-air (a second catch).
 */
final class PearlCatch {
    /** Wind charge blast: radius 1.2, knockback scaled by 1.22 for the push. */
    private static final double RADIUS = 1.2, KICK = 1.22;

    private static int pending = -1;
    private static boolean throwing;

    private PearlCatch() { }

    static void onUseItem(Player player, ItemStack stack) {
        Config.PearlCatch c = Config.get.pearlCatch;
        if (!c.enabled || throwing || !(player instanceof LocalPlayer p) || p != Minecraft.getInstance().player || !stack.is(Items.ENDER_PEARL)) return;
        if (p.getCooldowns().isOnCooldown(stack) || -p.getXRot() < c.minLookUp) return;
        pending = c.auto ? bestDelay(p, c.maxDelay) : Math.max(0, c.delayTicks);
    }

    /** Right after the pearl's use packet: a delay of 0 throws the charge in the same tick. */
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

    /** Delay (ticks) whose catch sends the pearl highest; -1 if no delay can catch it. */
    static int bestDelay(LocalPlayer p, int maxDelay) {
        Vec3 look = p.getLookAngle();
        boolean ground = p.onGround();
        Vec3 move = p.getKnownMovement();
        Vec3 pp = p.getEyePosition().subtract(0, 0.1, 0);
        Vec3 pv = look.scale(1.5).add(move.x, ground ? 0 : move.y, move.z);
        Vec3 me = p.getEyePosition(), mv = move;
        int best = -1;
        double bestApex = Double.NEGATIVE_INFINITY;
        for (int d = 1; d <= maxDelay; d++) {
            if (!ground) {
                me = me.add(mv);
                mv = new Vec3(mv.x * 0.91, (mv.y - 0.08) * 0.98, mv.z * 0.91);
            }
            // where the pearl is when the charge leaves, then solve the catch from there
            Vec3[] pearl = advance(pp, pv, d);
            Intercept i = intercept(me.subtract(0, 0.1, 0), ground ? new Vec3(move.x, 0, move.z) : mv, pearl[0], pearl[1], 40);
            if (i != null && i.apex > bestApex) {
                bestApex = i.apex;
                best = d;
            }
        }
        return best;
    }

    private static Vec3[] advance(Vec3 pos, Vec3 vel, int ticks) {
        for (int t = 0; t < ticks; t++) {
            pos = pos.add(vel);
            vel = vel.scale(0.99).subtract(0, 0.03, 0);
        }
        return new Vec3[]{pos, vel};
    }

    private record Intercept(Vec3 dir, double apex) { }

    /**
     * Earliest tick a wind charge thrown now (speed 1.5 plus the movement it inherits, no drag or gravity) can be
     * where the pearl is, the direction to throw it, and how high the kicked pearl then goes.
     */
    private static Intercept intercept(Vec3 from, Vec3 inherit, Vec3 pearlPos, Vec3 pearlVel, int horizon) {
        Vec3 pos = pearlPos, vel = pearlVel;
        for (int k = 1; k <= horizon; k++) {
            pos = pos.add(vel);
            vel = vel.scale(0.99).subtract(0, 0.03, 0);
            Vec3 need = pos.subtract(from).subtract(inherit.scale(k));
            double len = need.length();
            if (len <= 1.5 * k) {
                Vec3 dir = need.scale(1 / len);
                Vec3 v = vel.add(dir.scale(KICK * (1 - 0.2 / (2 * RADIUS))));
                double y = pos.y, vy = v.y;
                while (vy > 0) {
                    y += vy;
                    vy = vy * 0.99 - 0.03;
                }
                return new Intercept(dir, y);
            }
        }
        return null;
    }

    /** Your most recent pearl in flight, if the client can see it. */
    private static ThrownEnderpearl myPearl(LocalPlayer p) {
        ThrownEnderpearl best = null;
        for (Entity e : p.level().getEntities(p, p.getBoundingBox().inflate(48), e -> e instanceof ThrownEnderpearl)) {
            ThrownEnderpearl t = (ThrownEnderpearl) e;
            if (t.getOwner() == p && (best == null || t.getId() > best.getId())) best = t;
        }
        return best;
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

    /** The use packet carries your rotation, so only this throw is aimed; your view does not move. */
    private static void throwCharge(MultiPlayerGameMode gm, LocalPlayer p, InteractionHand hand) {
        Config.PearlCatch c = Config.get.pearlCatch;
        float yaw = p.getYRot(), pitch = p.getXRot();
        throwing = true;
        try {
            if (c.atFeet) {
                p.setXRot(90);
            } else if (c.auto) {
                ThrownEnderpearl pearl = myPearl(p);
                Vec3 move = p.getKnownMovement();
                if (pearl != null) {
                    Intercept i = intercept(p.getEyePosition().subtract(0, 0.1, 0), new Vec3(move.x, p.onGround() ? 0 : move.y, move.z),
                            pearl.position(), pearl.getDeltaMovement(), 40);
                    if (i != null) {
                        p.setYRot((float) Math.toDegrees(Math.atan2(-i.dir.x, i.dir.z)));
                        p.setXRot((float) -Math.toDegrees(Math.asin(i.dir.y)));
                    }
                }
            }
            gm.useItem(p, hand);
        } finally {
            p.setYRot(yaw);
            p.setXRot(pitch);
            throwing = false;
        }
    }
}
