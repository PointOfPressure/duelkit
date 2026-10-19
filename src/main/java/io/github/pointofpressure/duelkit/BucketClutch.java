package io.github.pointofpressure.duelkit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
    private BucketClutch() { }

    /** End of every client tick. */
    static void tick(Minecraft mc) {
        Config.BucketClutch c = Config.get.bucketClutch;
        LocalPlayer p = mc.player;
        if (!c.enabled || p == null || mc.gameMode == null || Compat.screenOpen(mc)) return;
        if (p.onGround() || p.isInWater() || p.getAbilities().flying || p.isFallFlying() || p.isSpectator()) return;
        if (!p.getMainHandItem().is(Items.WATER_BUCKET) || p.getDeltaMovement().y >= 0) return;
        HitResult hit = p.pick(p.blockInteractionRange(), 1.0F, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK || block.getDirection() != Direction.UP) return;
        double fall = p.fallDistance + (p.getY() - (block.getBlockPos().getY() + 1));
        if (fall <= c.minFall) return;
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
    }
}
