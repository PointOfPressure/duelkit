package io.github.pointofpressure.duelkit;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** A small square "D" button left of Options... in the pause menu, opening the settings. */
final class PauseButton {
    private PauseButton() { }

    static void register() {
        if (!FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) return;
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof PauseScreen) || !Config.get.pauseMenuButton) return;
            for (AbstractWidget w : Compat.widgets(screen)) {
                if (w.getMessage().getContents() instanceof TranslatableContents t && t.getKey().equals("menu.options")) {
                    Compat.widgets(screen).add(Button.builder(Component.literal("D"), b -> Compat.open(client, ConfigScreen.create(screen)))
                            .bounds(w.getX() - 24, w.getY(), 20, 20)
                            .tooltip(Tooltip.create(Component.literal("Duelkit")))
                            .build());
                    return;
                }
            }
        });
    }
}
