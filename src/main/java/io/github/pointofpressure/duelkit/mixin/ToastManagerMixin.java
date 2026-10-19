package io.github.pointofpressure.duelkit.mixin;

import io.github.pointofpressure.duelkit.Config;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Pop-ups in the top right: chat verification warnings, recipe unlocks, advancements, tutorials.
@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void duelkit$hide(Toast toast, CallbackInfo ci) {
        if (Config.get.hideToasts) ci.cancel();
    }
}
