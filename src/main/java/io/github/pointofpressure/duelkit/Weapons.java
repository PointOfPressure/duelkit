package io.github.pointofpressure.duelkit;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantment;

public final class Weapons {
    private Weapons() { }

    public static String kind(ItemStack s) {
        if (s.isEmpty()) return "empty";
        if (s.is(ItemTags.SWORDS)) return "sword";
        if (s.is(ItemTags.AXES)) return "axe";
        if (s.is(ItemTags.SPEARS)) return "spear";
        if (s.is(Items.MACE)) return "mace";
        if (s.is(Items.TRIDENT)) return "trident";
        return "other";
    }

    public static boolean disablesShields(ItemStack s) {
        Weapon w = s.get(DataComponents.WEAPON);
        return w != null && w.disableBlockingForSeconds() > 0;
    }

    public static boolean usable(ItemStack s) {
        return !s.isDamageableItem() || s.getDamageValue() < s.getMaxDamage() - 1;
    }

    public static boolean has(ItemStack s, ResourceKey<Enchantment> ench) {
        for (Holder<Enchantment> e : s.getEnchantments().keySet()) if (e.is(ench)) return true;
        return false;
    }

    /** Leftmost hotbar slot matching, or -1. */
    public static int find(Inventory inv, Predicate<ItemStack> p) {
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && usable(s) && p.test(s)) return i;
        }
        return -1;
    }

    /** The longest attack range in the hotbar (spears), or null. */
    public static AttackRange bestRange(Inventory inv) {
        AttackRange best = null;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            AttackRange r = inv.getItem(i).get(DataComponents.ATTACK_RANGE);
            if (r != null && (best == null || r.maxRange() > best.maxRange())) best = r;
        }
        return best;
    }
}
