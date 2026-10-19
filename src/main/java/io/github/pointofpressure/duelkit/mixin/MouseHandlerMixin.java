package io.github.pointofpressure.duelkit.mixin;

import io.github.pointofpressure.duelkit.Duelkit;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Once per frame, around your own mouse turn.
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void duelkit$before(CallbackInfo ci) {
        Duelkit.beforeMouse();
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("TAIL"))
    private void duelkit$after(CallbackInfo ci) {
        Duelkit.afterMouse();
    }
}
