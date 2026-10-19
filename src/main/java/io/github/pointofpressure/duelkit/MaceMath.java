package io.github.pointofpressure.duelkit;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Picks the mace that deals the most damage for this hit, from the vanilla smash, Density, Breach and armour formulas. */
final class MaceMath {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private MaceMath() { }

    /** Hotbar slot of the best mace against this target, or -1. */
    static int best(LocalPlayer p, Inventory inv, LivingEntity target) {
        double armor = 0, toughness = 0;
        if (target != null) {
            for (EquipmentSlot s : ARMOR) {
                ItemStack a = target.getItemBySlot(s);
                armor += sum(a, Attributes.ARMOR);
                toughness += sum(a, Attributes.ARMOR_TOUGHNESS);
            }
        }
        // attributes are the held weapon's (the server applies last tick's), charge scales them; smash bonus is added unscaled
        float charge = p.getAttackStrengthScale(0.5F);
        double base = p.getAttributeValue(Attributes.ATTACK_DAMAGE) * (0.2 + 0.8 * charge * charge);
        double fall = p.fallDistance;
        double smash = fall <= 3 ? 4 * fall : fall <= 8 ? 12 + 2 * (fall - 3) : 22 + (fall - 8);

        int bestSlot = -1;
        double bestDamage = -1;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack m = inv.getItem(i);
            if (!m.is(Items.MACE) || !Weapons.usable(m)) continue;
            double total = base + smash + 0.5 * level(m, Enchantments.DENSITY) * fall;
            double a = armor * Math.max(0, 1 - 0.15 * level(m, Enchantments.BREACH));
            double effective = Math.min(20, Math.max(a * 0.2, a - total / (2 + toughness / 4)));
            double damage = total * (1 - effective / 25);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    static int level(ItemStack s, ResourceKey<Enchantment> key) {
        for (Object2IntMap.Entry<Holder<Enchantment>> e : s.getEnchantments().entrySet())
            if (e.getKey().is(key)) return e.getIntValue();
        return 0;
    }

    private static double sum(ItemStack s, Holder<Attribute> attribute) {
        ItemAttributeModifiers mods = s.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods == null) return 0;
        double total = 0;
        for (ItemAttributeModifiers.Entry e : mods.modifiers())
            if (e.attribute().equals(attribute) && e.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) total += e.modifier().amount();
        return total;
    }
}
