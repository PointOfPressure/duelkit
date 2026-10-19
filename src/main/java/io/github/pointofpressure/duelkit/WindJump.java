package io.github.pointofpressure.duelkit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Wind charge jump. A wind charge thrown on the ground while looking down is remembered; when its blast reaches
 * you, you jump just before the knockback is applied. The server measured the blast with you on the ground, and
 * jumping first means the knockback adds on top of the jump instead of replacing it. Aiming is up to you.
 */
public final class WindJump {
    private static long thrownAt;

    private WindJump() { }

    public static void onUseItem(Player player, ItemStack stack) {
        Config.WindJump c = Config.get.windJump;
        if (!c.enabled || player != Minecraft.getInstance().player || !stack.is(Items.WIND_CHARGE)) return;
        if (!player.onGround() || player.getXRot() < c.minPitch || player.getCooldowns().isOnCooldown(stack)) return;
        thrownAt = System.currentTimeMillis();
    }

    /** On the render thread, right before the blast's knockback is added. */
    public static void onExplosion(ClientboundExplodePacket packet) {
        LocalPlayer me = Minecraft.getInstance().player;
        if (me != null && packet.playerKnockback().isPresent() && packet.playerKnockback().get().y > 0) BucketClutch.launched(me.getY());
        if (!Config.get.windJump.enabled || thrownAt == 0) return;
        if (System.currentTimeMillis() - thrownAt > Config.get.windJump.windowMs) { thrownAt = 0; return; }
        if (packet.playerKnockback().isEmpty() || packet.playerKnockback().get().y <= 0) return;
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null || !p.onGround()) return;
        thrownAt = 0;
        p.jumpFromGround();
    }
}
