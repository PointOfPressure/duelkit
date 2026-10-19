package io.github.pointofpressure.duelkit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Water bucket clutch, click only: falling with a water bucket in hand while looking at the top of a block, it
 * uses the bucket as soon as that block is in reach. No aiming. The reach check is a fresh raycast from where you
 * are at the end of the tick (where the server has you), not the crosshair target picked a tick of falling earlier.
 * Retries every tick until the bucket is empty or the fall ends.
 */
final class BucketClutch {
    private static BlockPos water;
    private static int waitTicks;
    /** Height of the last wind blast that launched you: falls only hurt below it. */
    private static double launchY = Double.NaN;

    private BucketClutch() { }

    /** End of every client tick. */
    static void tick(Minecraft mc) {
        Config.BucketClutch c = Config.get.bucketClutch;
        LocalPlayer p = mc.player;
        if (p != null && p.onGround()) launchY = Double.NaN;
        if (!c.enabled || p == null || mc.gameMode == null || Compat.screenOpen(mc)) return;
        if (water != null && pickUp(mc, p, c)) return;
        if (p.onGround() || p.isInWater() || p.getAbilities().flying || p.isFallFlying() || p.isSpectator()) return;
        if (!p.getMainHandItem().is(Items.WATER_BUCKET) || p.getDeltaMovement().y >= 0) return;
        HitResult hit = p.pick(p.blockInteractionRange(), 1.0F, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK || block.getDirection() != Direction.UP) return;
        double fall = p.fallDistance + (p.getY() - (block.getBlockPos().getY() + 1));
        if (fall <= c.minFall) return;
        if (c.onlyIfItHurts && fallDamage(p, block.getBlockPos(), fall) <= 0) return;
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        if (!p.getMainHandItem().is(Items.WATER_BUCKET)) {
            water = block.getBlockPos().above();
            waitTicks = 0;
        }
    }

    /** A wind charge or Wind Burst blast launched you from here. */
    static void launched(double y) {
        launchY = y;
    }

    /** Expected fall damage on landing on top of this block, after armour enchantments. */
    private static double fallDamage(LocalPlayer p, BlockPos ground, double fall) {
        if (p.hasEffect(MobEffects.SLOW_FALLING)) return 0;
        double top = ground.getY() + 1;
        if (!Double.isNaN(launchY)) fall = Math.min(fall, launchY - top);
        BlockState below = p.level().getBlockState(ground);
        Block b = below.getBlock();
        double scale = 1;
        if (b instanceof SlimeBlock || b == Blocks.COBWEB || b == Blocks.POWDER_SNOW || !p.level().getFluidState(ground.above()).isEmpty()) return 0;
        if (b == Blocks.HAY_BLOCK || b == Blocks.HONEY_BLOCK) scale = 0.2;
        if (b instanceof BedBlock) scale = 0.5;
        double damage = Math.ceil((fall - p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) * p.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER) * scale);
        if (damage <= 0) return 0;
        int epf = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack a = p.getItemBySlot(slot);
            epf += 3 * MaceMath.level(a, Enchantments.FEATHER_FALLING) + MaceMath.level(a, Enchantments.PROTECTION);
        }
        return damage * (1 - Math.min(20, epf) / 25.0);
    }

    /** Once landed, scoop the clutch water back up with the empty bucket if you are looking at it. True while waiting. */
    private static boolean pickUp(Minecraft mc, LocalPlayer p, Config.BucketClutch c) {
        if (!c.pickUp || ++waitTicks > 40 || !p.getMainHandItem().is(Items.BUCKET)) {
            water = null;
            return false;
        }
        // still in the air: never hold up the next clutch
        if (!(p.onGround() || p.isInWater())) return false;
        HitResult hit = p.pick(p.blockInteractionRange(), 1.0F, true);
        if (hit instanceof BlockHitResult b && b.getBlockPos().equals(water) && p.level().getFluidState(water).isSource()) {
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            water = null;
        }
        return true;
    }
}
