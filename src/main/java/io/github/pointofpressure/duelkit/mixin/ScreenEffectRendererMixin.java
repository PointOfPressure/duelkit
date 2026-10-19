package io.github.pointofpressure.duelkit.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.pointofpressure.duelkit.Config;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The totem pop that fills the screen: shrink it.
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void duelkit$smallTotem(PoseStack pose, float x, float y, float z) {
        float k = (float) Config.get.totemPopSize;
        pose.scale(x * k, y * k, z * k);
    }
}
