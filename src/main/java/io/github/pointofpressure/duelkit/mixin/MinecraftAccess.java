package io.github.pointofpressure.duelkit.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccess {
    @Invoker("startAttack")
    boolean duelkit$startAttack();

    @Accessor("rightClickDelay")
    void duelkit$setRightClickDelay(int delay);
}
