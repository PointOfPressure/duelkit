package io.github.pointofpressure.duelkit;

import io.github.pointofpressure.duelkit.mixin.MultiPlayerGameModeAccessor;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Attribute swapping. The server applies the attributes (damage, attack speed) of the item you held last tick,
 * but the enchantments, mace smash bonus and shield-disabling of the item in hand when the hit arrives. So the
 * hit weapon is selected right before the attack packet and the original slot comes back before the next tick.
 */
public final class Swaps {
    private static boolean busy;
    private static int origin = -1;
    private static int returnIn = -1, returnFrom = -1;
    private static int followSlot = -1, followIn = -1;
    private static Entity followTarget;
    // set in before(), used in after() of the same attack
    private static boolean swapped;
    private static int follow = -1;

    private Swaps() { }

    private static Config.Swaps cfg() {
        return Config.get.swaps;
    }

    public static void before(MultiPlayerGameMode gm, Player attacker, Entity target) {
        swapped = false;
        follow = -1;
        Minecraft mc = Minecraft.getInstance();
        if (busy || !cfg().enabled || attacker != mc.player || target == null) return;
        LocalPlayer p = mc.player;
        Inventory inv = p.getInventory();
        int held = inv.getSelectedSlot();
        ItemStack heldStack = p.getMainHandItem();
        if (!cfg().from.contains(Weapons.kind(heldStack))) return;

        int hit = hitSlot(p, inv, held, target);
        if (follow == held) follow = -1;
        int spear = spearSlot(p, inv, held, heldStack, target);
        boolean swapHit = hit >= 0 && hit != held;
        if (!swapHit && spear < 0 && follow < 0) return;

        // a delayed return still pending keeps the original slot
        if (returnIn < 0) origin = held;
        returnIn = -1;
        if (spear >= 0) {
            inv.setSelectedSlot(spear);
            ((MultiPlayerGameModeAccessor) gm).duelkit$ensureHasSentCarriedItem();
        }
        // attack() sends this slot itself, right before the attack packet
        inv.setSelectedSlot(swapHit ? hit : held);
        swapped = swapHit || spear >= 0;
    }

    /** Slot to land the hit with, or -1 for the held weapon. May set {@link #follow}. */
    private static int hitSlot(LocalPlayer p, Inventory inv, int held, Entity target) {
        boolean falling = falling(p);
        boolean player = target instanceof Player;
        boolean smash = cfg().smash && falling && (player || cfg().smashMobs);
        if (cfg().shieldBreak && target instanceof LivingEntity le && shieldUp(le, p)) {
            // no axe: a mace into a raised shield is wasted, so it stays a plain hit
            int axe = Weapons.disablesShields(p.getMainHandItem()) ? held : Weapons.find(inv, Weapons::disablesShields);
            if (axe >= 0 && cfg().stunSlam && smash) follow = mace(inv, true);
            return axe;
        }
        if (smash) return mace(inv, true);
        if (cfg().breach && !falling && (player || cfg().breachMobs))
            return Weapons.find(inv, s -> s.is(Items.MACE) && Weapons.has(s, Enchantments.BREACH));
        return -1;
    }

    /** Spear slot to pass through before the hit, as in a manual reach swap, or -1. */
    private static int spearSlot(LocalPlayer p, Inventory inv, int held, ItemStack heldStack, Entity target) {
        if (cfg().reach == Config.Reach.OFF || heldStack.has(DataComponents.ATTACK_RANGE)) return -1;
        int slot = Weapons.bestRangeSlot(inv);
        if (slot < 0 || slot == held) return -1;
        if (cfg().reach == Config.Reach.ALWAYS) return slot;
        return AttackRange.defaultFor(p).isInRange(p, target.getBoundingBox(), 0) ? -1 : slot;
    }

    public static void after(MultiPlayerGameMode gm, Player attacker, Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (busy || attacker != mc.player) return;
        LocalPlayer p = mc.player;
        int f = follow;
        follow = -1;
        if (f >= 0) {
            if (cfg().natural) {
                followSlot = f;
                followTarget = target;
                followIn = naturalTicks();
                return;
            }
            hit(gm, p, f, target);
            swapped = true;
        }
        if (swapped) returnHome(gm, p);
    }

    /** Start of every client tick: delayed follow-ups and returns. */
    public static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        MultiPlayerGameMode gm = mc.gameMode;
        if (p == null || gm == null) {
            followIn = returnIn = origin = -1;
            followTarget = null;
            return;
        }
        if (followIn >= 0 && --followIn < 0) {
            Entity t = followTarget;
            followTarget = null;
            boolean onTarget = mc.hitResult instanceof EntityHitResult h && h.getEntity() == t;
            if (t != null && t.isAlive() && onTarget && falling(p)) hit(gm, p, followSlot, t);
            returnHome(gm, p);
        }
        if (returnIn >= 0 && --returnIn < 0) {
            // only if you have not changed slot yourself in the meantime
            if (origin >= 0 && p.getInventory().getSelectedSlot() == returnFrom) p.getInventory().setSelectedSlot(origin);
            origin = -1;
        }
    }

    private static void hit(MultiPlayerGameMode gm, LocalPlayer p, int slot, Entity target) {
        busy = true;
        try {
            p.getInventory().setSelectedSlot(slot);
            gm.attack(p, target);
            Compat.swing(p);
        } finally {
            busy = false;
        }
    }

    private static void returnHome(MultiPlayerGameMode gm, LocalPlayer p) {
        if (origin < 0) return;
        if (cfg().natural) {
            returnIn = naturalTicks();
            returnFrom = p.getInventory().getSelectedSlot();
            return;
        }
        if (p.getInventory().getSelectedSlot() != origin) p.getInventory().setSelectedSlot(origin);
        // send it now so the server never ticks with the swapped item in hand
        ((MultiPlayerGameModeAccessor) gm).duelkit$ensureHasSentCarriedItem();
        origin = -1;
    }

    private static int naturalTicks() {
        int lo = Math.max(1, cfg().naturalMinTicks), hi = Math.max(lo, cfg().naturalMaxTicks);
        return ThreadLocalRandom.current().nextInt(lo, hi + 1);
    }

    static boolean falling(LocalPlayer p) {
        return p.fallDistance > cfg().maceMinFall && !p.isFallFlying();
    }

    /** Density mace when falling, otherwise any mace. */
    private static int mace(Inventory inv, boolean preferDensity) {
        int d = preferDensity ? Weapons.find(inv, s -> s.is(Items.MACE) && Weapons.has(s, Enchantments.DENSITY)) : -1;
        return d >= 0 ? d : Weapons.find(inv, s -> s.is(Items.MACE));
    }

    /** Raised shield that actually covers you: shields block the front 180 degrees around where the head faces. */
    private static boolean shieldUp(LivingEntity t, LocalPlayer p) {
        boolean raised = t.isBlocking() || (t.isUsingItem() && t.getUseItem().has(DataComponents.BLOCKS_ATTACKS));
        if (!raised) return false;
        double yaw = Math.toRadians(t.getYHeadRot());
        double dx = p.getX() - t.getX(), dz = p.getZ() - t.getZ();
        return dx * -Math.sin(yaw) + dz * Math.cos(yaw) >= 0;
    }

    /** Spear reach for the held weapon: the hotbar spear's range with no minimum. Null when it does not apply. */
    public static AttackRange reach(LocalPlayer p, ItemStack held) {
        if (!cfg().enabled || cfg().reach == Config.Reach.OFF || !cfg().from.contains(Weapons.kind(held))) return null;
        int slot = Weapons.bestRangeSlot(p.getInventory());
        return slot < 0 ? null : Compat.reachOf(p.getInventory().getItem(slot).get(DataComponents.ATTACK_RANGE));
    }
}
