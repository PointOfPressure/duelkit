package io.github.pointofpressure.duelkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Saved hotbar layouts (config/duelkit-loadouts.json), equipped by moving items around your own inventory. */
public final class Loadouts {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("duelkit-loadouts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static List<Loadout> list = new ArrayList<>();

    /** An item as remembered by a loadout: its id and, if it had one, its custom name. */
    public static final class Slot {
        public String item;
        public String name;
    }

    public static final class Loadout {
        public String name;
        /** Nine entries; null leaves that hotbar slot alone. */
        public List<Slot> hotbar = new ArrayList<>();
        public Slot offhand;
    }

    private Loadouts() { }

    static void load() {
        try {
            if (Files.exists(FILE)) list = GSON.fromJson(Files.readString(FILE), new TypeToken<List<Loadout>>() { }.getType());
        } catch (Exception e) {
            Duelkit.LOG.warn("config/duelkit-loadouts.json unreadable: {}", e.toString());
        }
        if (list == null) list = new ArrayList<>();
    }

    static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(list));
        } catch (Exception e) {
            Duelkit.LOG.warn("could not save config/duelkit-loadouts.json: {}", e.toString());
        }
    }

    static Loadout capture(LocalPlayer p) {
        Loadout l = new Loadout();
        l.name = "Loadout " + (list.size() + 1);
        Inventory inv = p.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) l.hotbar.add(slot(inv.getItem(i)));
        l.offhand = slot(p.getOffhandItem());
        return l;
    }

    private static Slot slot(ItemStack s) {
        if (s.isEmpty()) return null;
        Slot slot = new Slot();
        slot.item = BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
        Component name = s.get(DataComponents.CUSTOM_NAME);
        slot.name = name == null ? null : name.getString();
        return slot;
    }

    /** What a slot looks like, for drawing. */
    static ItemStack icon(Slot s) {
        if (s == null) return ItemStack.EMPTY;
        Identifier id = Identifier.tryParse(s.item);
        ItemStack stack = id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.getValue(id));
        if (s.name != null && !stack.isEmpty()) stack.set(DataComponents.CUSTOM_NAME, Component.literal(s.name));
        return stack;
    }

    /** 0 = no match, 1 = same item, 2 = same item and name. */
    private static int score(Slot want, ItemStack have) {
        if (want == null || have.isEmpty() || !BuiltInRegistries.ITEM.getKey(have.getItem()).toString().equals(want.item)) return 0;
        Component name = have.get(DataComponents.CUSTOM_NAME);
        return want.name == null || (name != null && want.name.equals(name.getString())) ? 2 : 1;
    }

    /** Swaps items into place with the same clicks as pressing number keys in the inventory screen. */
    static void apply(Minecraft mc, Loadout l) {
        LocalPlayer p = mc.player;
        MultiPlayerGameMode gm = mc.gameMode;
        if (p == null || gm == null) return;
        Inventory inv = p.getInventory();
        int container = p.inventoryMenu.containerId;
        boolean[] done = new boolean[36];
        for (int i = 0; i < Inventory.getSelectionSize() && i < l.hotbar.size(); i++) {
            Slot want = l.hotbar.get(i);
            if (want == null) continue;
            if (score(want, inv.getItem(i)) == 2) { done[i] = true; continue; }
            int from = best(inv, want, done, l);
            if (from < 0) continue;
            Compat.swapClick(gm, container, menuSlot(from), i, p);
            done[i] = true;
        }
        if (l.offhand != null && score(l.offhand, p.getOffhandItem()) < 2) {
            int from = best(inv, l.offhand, done, l);
            if (from >= 0) Compat.swapClick(gm, container, menuSlot(from), 40, p);
        }
    }

    /** Best source slot for an item, skipping slots already filled and hotbar slots that already hold their own item. */
    private static int best(Inventory inv, Slot want, boolean[] done, Loadout l) {
        int best = -1, bestScore = 0;
        for (int k = 0; k < 36; k++) {
            if (done[k]) continue;
            if (k < 9 && k < l.hotbar.size() && score(l.hotbar.get(k), inv.getItem(k)) == 2) continue;
            int s = score(want, inv.getItem(k));
            if (s > bestScore) {
                bestScore = s;
                best = k;
            }
        }
        return best;
    }

    /** Inventory index → slot number in the player's inventory menu (hotbar is 36-44 there). */
    private static int menuSlot(int index) {
        return index < 9 ? 36 + index : index;
    }
}
