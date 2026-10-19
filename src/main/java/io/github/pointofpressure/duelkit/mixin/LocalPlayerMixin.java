package io.github.pointofpressure.duelkit.mixin;

import io.github.pointofpressure.duelkit.Swaps;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The crosshair target is picked with the held item's attack_range component, if it has one (spears do).
// Give a weapon without one the spear's range; the server allows its own range plus 3 blocks anyway.
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Redirect(method = "raycastHitResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object duelkit$spearReach(ItemStack held, DataComponentType<?> type) {
        Object own = held.get(type);
        if (own != null || type != DataComponents.ATTACK_RANGE) return own;
        return Swaps.reach((LocalPlayer) (Object) this, held);
    }
}
