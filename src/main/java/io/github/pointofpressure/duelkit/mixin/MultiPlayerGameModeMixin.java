package io.github.pointofpressure.duelkit.mixin;

import io.github.pointofpressure.duelkit.Swaps;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// attack() sends the selected slot itself before the attack packet, so selecting at HEAD is enough
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void duelkit$before(Player attacker, Entity target, CallbackInfo ci) {
        Swaps.before(attacker, target);
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void duelkit$after(Player attacker, Entity target, CallbackInfo ci) {
        Swaps.after((MultiPlayerGameMode) (Object) this, attacker, target);
    }
}
