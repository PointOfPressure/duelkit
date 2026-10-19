package io.github.pointofpressure.duelkit;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Hold-K loadout panel (1.21.11 drawing); the layout and actions live in {@link LoadoutUi}. */
final class LoadoutScreen extends Screen {
    private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier OFFHAND = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
    private final LoadoutUi ui = new LoadoutUi();
    private final KeyMapping key;
    private int mouseX, mouseY;

    LoadoutScreen(KeyMapping key) {
        super(Component.literal("Loadouts"));
        this.key = key;
    }

    @Override
    protected void init() {
        ui.resize(width, height);
        addRenderableWidget(Button.builder(Component.literal("Save current hotbar"), b -> ui.saveCurrent())
                .bounds(width / 2 - 75, height - 30, 150, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        mouseX = mx;
        mouseY = my;
        GuiGraphics gg = g;
        ui.draw(new LoadoutUi.Canvas() {
            public void hotbar(int x, int y) { gg.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, x, y, LoadoutUi.BAR_W, LoadoutUi.BAR_H); }
            public void slot(int x, int y) { gg.blitSprite(RenderPipelines.GUI_TEXTURED, OFFHAND, x, y - 1, 29, 24); }
            public void item(ItemStack stack, int x, int y) { gg.renderItem(stack, x, y); }
            public void fill(int x0, int y0, int x1, int y1, int color) { gg.fill(x0, y0, x1, y1, color); }
            public void text(String text, int x, int y, int color) { gg.drawString(font, text, x, y, color); }
            public int width(String text) { return font.width(text); }
        }, mx, my);
    }

    @Override
    public boolean keyReleased(KeyEvent e) {
        if (key.matches(e)) {
            int row = ui.rowAt(mouseX, mouseY);
            onClose();
            ui.equip(row);
            return true;
        }
        return super.keyReleased(e);
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        int n = e.key() - 49;
        if (n >= 0 && n < 9 && n < Loadouts.list.size()) {
            onClose();
            ui.equip(n);
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        int row = ui.rowAt(e.x(), e.y());
        if (row >= 0) {
            if (e.button() == 1) {
                ui.delete(row);
            } else {
                onClose();
                ui.equip(row);
            }
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }
}
