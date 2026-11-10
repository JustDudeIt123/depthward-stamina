package dev.depthward.stamina;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class ShieldWeaponDamage {
    private static List<? extends String> cachedText;
    private static List<ShieldWeaponRule> cachedRules = List.of();
    private ShieldWeaponDamage() {}

    public static ItemStack attackingWeapon(LivingEntity attacker) {
        return attacker.getMainHandItem();
    }
    public static double meleeCost(DamageSource source) {
        return meleeCost(source, StaminaConfig.SHIELD_BASE_DAMAGE.get());
    }
    public static double meleeCost(DamageSource source, double base) {
        var direct = source.getDirectEntity();
        ItemStack weapon = direct instanceof LivingEntity attacker ? attackingWeapon(attacker) : ItemStack.EMPTY;
        return base * multiplier(weapon);
    }
    public static double multiplier(ItemStack weapon) {
        var text = StaminaConfig.SHIELD_WEAPON_RULES.get();
        if (!text.equals(cachedText)) {
            cachedRules = text.stream().map(ShieldWeaponRule::parse).toList();
            cachedText = List.copyOf(text);
        }
        return multiplier(weapon, cachedRules, StaminaConfig.SHIELD_DEFAULT_MULTIPLIER.get());
    }
    public static double multiplier(ItemStack weapon, List<ShieldWeaponRule> rules, double fallback) {
        return ShieldWeaponRule.firstMatch(rules, BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(),
            id -> weapon.is(TagKey.create(Registries.ITEM, new ResourceLocation(id))),
            path -> readNbt(weapon, path), fallback);
    }
    private static String readNbt(ItemStack weapon, List<String> path) {
        Tag value = weapon.getTag();
        for (String key : path) {
            if (!(value instanceof CompoundTag compound)) return null;
            value = compound.get(key);
        }
        // Do not stringify compound/list structures as if they were a matching string leaf.
        return value == null || value.getId() == Tag.TAG_COMPOUND || value.getId() == Tag.TAG_LIST
            || value.getId() == Tag.TAG_BYTE_ARRAY || value.getId() == Tag.TAG_INT_ARRAY || value.getId() == Tag.TAG_LONG_ARRAY
            ? null : value.getAsString();
    }
}
