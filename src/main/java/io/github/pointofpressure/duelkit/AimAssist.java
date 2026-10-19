package io.github.pointofpressure.duelkit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Melee aim assist in the console style: it only ever adds to what your hand is doing.
 * Pull: moving the mouse toward the target turns you a bit further toward it. Never moves you on its own,
 * never pulls against you. Stickiness: while the crosshair is on the target your own turning is slowed.
 * Tracking: while on (or almost on) the target, follows part of its movement across your screen.
 * One target is kept until it leaves the cone, so it does not flick between people. Never attacks.
 * Mace dive: while falling onto someone the cone and range widen, tracking is full, and it also pulls on its own.
 */
final class AimAssist {
    private static float yawBefore, pitchBefore;
    private static long lastNanos;
    private static Entity target;
    private static float lastYawTo, lastPitchTo;

    private AimAssist() { }

    /** Before your own mouse turn this frame. */
    static void beforeMouse() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        yawBefore = p.getYRot();
        pitchBefore = p.getXRot();
    }

    /** After your own mouse turn this frame. */
    static void afterMouse() {
        Config.AimAssist c = Config.get.aimAssist;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel level = mc.level;
        long now = System.nanoTime();
        double dt = lastNanos == 0 ? 0 : Math.min((now - lastNanos) / 1e9, 0.05);
        lastNanos = now;
        if (!c.enabled || p == null || level == null || Compat.screenOpen(mc) || p.isSpectator() || !Weapons.isWeapon(p.getMainHandItem())) {
            target = null;
            return;
        }
        boolean dive = c.dive && !p.onGround() && !p.isFallFlying() && p.getDeltaMovement().y < 0 && p.fallDistance >= c.diveMinFall;
        double cone = dive ? c.diveCone : c.cone, range = dive ? c.diveRange : c.range;
        double tracking = dive ? c.diveTracking : c.tracking;
        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 eye = p.getEyePosition(pt);
        Vec3 look = p.getViewVector(pt);

        Entity prev = target;
        if (target == null || !valid(c, p, level, target, eye, look, pt, cone / 2 * c.keepTarget, range)) target = pick(c, p, level, eye, look, pt, cone, range);
        if (target == null) return;

        AABB box = box(target, pt, c.inset);
        Vec3 aim = closestPoint(box, eye, look);
        Vec3 center = box.getCenter().subtract(eye);
        float yawTo = yaw(center), pitchTo = pitch(center);

        // gap from the crosshair to the nearest point of the hitbox, zero when already on it
        Vec3 to = aim.subtract(eye);
        double gapYaw = Mth.wrapDegrees(yaw(to) - p.getYRot());
        double gapPitch = pitch(to) - p.getXRot();
        double gap = Math.hypot(gapYaw, gapPitch);
        boolean onTarget = gap < 0.05;

        double turnYaw = 0, turnPitch = 0;

        // tracking fades out as the crosshair drifts off the hitbox, instead of switching off at a hard edge
        if (target == prev && gap < c.trackWithin) {
            double t = tracking * (1 - gap / c.trackWithin);
            turnYaw += Mth.wrapDegrees(yawTo - lastYawTo) * t;
            turnPitch += (pitchTo - lastPitchTo) * t;
        }
        if (dive && !onTarget && dt > 0) {
            double f = 1 - Math.exp(-c.diveMagnet * dt);
            turnYaw += gapYaw * f;
            turnPitch += gapPitch * f;
        }

        double userYaw = Mth.wrapDegrees(p.getYRot() - yawBefore), userPitch = p.getXRot() - pitchBefore;
        double user = Math.hypot(userYaw, userPitch);
        if (user > 1e-4) {
            // stickiness ramps in over the last degree, so there is no hard edge to stutter on
            double stick = c.stickiness * Mth.clamp(1 - gap, 0, 1);
            turnYaw -= userYaw * stick;
            turnPitch -= userPitch * stick;
            if (!onTarget && gapYaw * userYaw + gapPitch * userPitch > 0) {
                double extra = Math.min(gap, c.pull * user);
                turnYaw += gapYaw / gap * extra;
                turnPitch += gapPitch / gap * extra;
            }
        }

        lastYawTo = yawTo;
        lastPitchTo = pitchTo;
        // turn() takes mouse units (0.15 degrees each) and keeps the interpolation in step
        if (turnYaw != 0 || turnPitch != 0) p.turn(turnYaw / 0.15, turnPitch / 0.15);
    }

    private static Entity pick(Config.AimAssist c, LocalPlayer p, ClientLevel level, Vec3 eye, Vec3 look, float pt, double cone, double range) {
        Entity best = null;
        double bestAngle = Double.MAX_VALUE;
        for (Entity e : level.entitiesForRendering()) {
            if (!valid(c, p, level, e, eye, look, pt, cone / 2, range)) continue;
            double a = angle(closestPoint(box(e, pt, c.inset), eye, look).subtract(eye), look);
            if (a < bestAngle) {
                bestAngle = a;
                best = e;
            }
        }
        return best;
    }

    private static boolean valid(Config.AimAssist c, LocalPlayer p, ClientLevel level, Entity e, Vec3 eye, Vec3 look, float pt, double halfCone, double range) {
        if (!Targets.matches(e, p, c.players, c.hostiles, false)) return false;
        Vec3 aim = closestPoint(box(e, pt, c.inset), eye, look);
        Vec3 to = aim.subtract(eye);
        double dist = to.length();
        if (dist < 1e-4 || Math.toDegrees(angle(to, look)) > halfCone) return false;
        if (c.rangeShape == Config.RangeShape.COLUMN) {
            if (Math.hypot(to.x, to.z) > range || (c.verticalRange > 0 && Math.abs(to.y) > c.verticalRange)) return false;
        } else if (dist > range) {
            return false;
        }
        BlockHitResult hit = level.clip(new ClipContext(eye, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceTo(eye) >= dist - 0.01;
    }

    private static AABB box(Entity e, float pt, double inset) {
        return e.getBoundingBox().move(e.getPosition(pt).subtract(e.getPosition(1.0F))).deflate(inset);
    }

    /** Point of the box closest to the look ray, at the box's distance. */
    private static Vec3 closestPoint(AABB box, Vec3 eye, Vec3 look) {
        Vec3 ray = eye.add(look.scale(eye.distanceTo(box.getCenter())));
        return new Vec3(Mth.clamp(ray.x, box.minX, box.maxX), Mth.clamp(ray.y, box.minY, box.maxY), Mth.clamp(ray.z, box.minZ, box.maxZ));
    }

    private static double angle(Vec3 to, Vec3 look) {
        return Math.acos(Mth.clamp(to.normalize().dot(look), -1, 1));
    }

    private static float yaw(Vec3 v) {
        return (float) Math.toDegrees(Math.atan2(-v.x, v.z));
    }

    private static float pitch(Vec3 v) {
        return (float) -Math.toDegrees(Math.atan2(v.y, Math.sqrt(v.x * v.x + v.z * v.z)));
    }
}
