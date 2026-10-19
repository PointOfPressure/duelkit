package io.github.pointofpressure.duelkit;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * The hold-K loadout panel, drawn the same on every version through {@link Canvas}. Each loadout is a row: its
 * name, then a vanilla hotbar with its items and the off-hand item to the left. Release K over a row (or click
 * it, or press its number) to equip it. Right-click deletes.
 */
public final class LoadoutUi {
    public interface Canvas {
        void hotbar(int x, int y);
        void slot(int x, int y);
        void item(ItemStack stack, int x, int y);
        void fill(int x0, int y0, int x1, int y1, int color);
        void text(String text, int x, int y, int color);
        int width(String text);
    }

    static final int ROW = 40, BAR_W = 182, BAR_H = 22;
    private int width, height;

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private int top() {
        return Math.max(20, height / 2 - Loadouts.list.size() * ROW / 2 - 10);
    }

    private int barX() {
        return width / 2 - BAR_W / 2 + 12;
    }

    /** Row under the mouse, or -1. */
    int rowAt(double mx, double my) {
        int top = top();
        for (int i = 0; i < Loadouts.list.size(); i++) {
            int y = top + i * ROW;
            if (mx >= barX() - 26 && mx <= barX() + BAR_W && my >= y && my <= y + ROW - 4) return i;
        }
        return -1;
    }

    void draw(Canvas c, int mx, int my) {
        int top = top();
        String title = Loadouts.list.isEmpty() ? "No loadouts yet" : "Loadouts";
        c.text(title, width / 2 - c.width(title) / 2, top - 14, 0xFFFFFFFF);
        int hover = rowAt(mx, my);
        for (int i = 0; i < Loadouts.list.size(); i++) {
            Loadouts.Loadout l = Loadouts.list.get(i);
            int y = top + i * ROW, x = barX();
            if (i == hover) c.fill(x - 28, y - 2, x + BAR_W + 2, y + ROW - 4, 0x40FFFFFF);
            c.text((i < 9 ? (i + 1) + "  " : "") + l.name, x, y, i == hover ? 0xFFFFFF55 : 0xFFE0E0E0);
            c.hotbar(x, y + 10);
            for (int s = 0; s < 9 && s < l.hotbar.size(); s++) c.item(Loadouts.icon(l.hotbar.get(s)), x + 3 + s * 20, y + 13);
            c.slot(x - 26, y + 10);
            c.item(Loadouts.icon(l.offhand), x - 23, y + 13);
        }
        String hint = "Release K over one to equip  ·  right-click deletes";
        c.text(hint, width / 2 - c.width(hint) / 2, height - 46, 0xFFA0A0A0);
    }

    void equip(int row) {
        if (row < 0 || row >= Loadouts.list.size()) return;
        Minecraft mc = Minecraft.getInstance();
        Loadouts.apply(mc, Loadouts.list.get(row));
        if (mc.player != null) Compat.actionBar(mc.player, "Equipped " + Loadouts.list.get(row).name);
    }

    void delete(int row) {
        if (row < 0 || row >= Loadouts.list.size()) return;
        Loadouts.list.remove(row);
        Loadouts.save();
    }

    void saveCurrent() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Loadouts.list.add(Loadouts.capture(mc.player));
        Loadouts.save();
    }
}
