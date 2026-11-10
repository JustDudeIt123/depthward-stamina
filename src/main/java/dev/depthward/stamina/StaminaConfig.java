package dev.depthward.stamina;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class StaminaConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue LIGHT_MAX, MEDIUM_MAX;
    public static final ForgeConfigSpec.DoubleValue LIGHT_COST, MEDIUM_COST, HEAVY_COST;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_HEAVY_ACTIONS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HEAVY_BLOCKED_ACTIONS;
    public static final ForgeConfigSpec.DoubleValue SHIELD_BASE_DAMAGE, SHIELD_DEFAULT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue RECOVERY_BASE_DELAY;
    public static final ForgeConfigSpec.BooleanValue CONTROLS_PROFILE_APPLIED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SHIELD_WEAPON_RULES;
    public static final ForgeConfigSpec.IntValue PARRY_WINDOW, FAILED_PARRY_COOLDOWN, GUARD_BREAK_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue BASIC_ATTACK_COST, BOW_HOLD_COST;
    public static final ForgeConfigSpec.DoubleValue WEAPON_GUARD_BASE_DAMAGE, WEAPON_GUARD_MELEE_DAMAGE, WEAPON_GUARD_PROJECTILE_DAMAGE;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("combatActions");
        BASIC_ATTACK_COST=b.defineInRange("basicAttackCost",4.0,0,1000000);
        BOW_HOLD_COST=b.defineInRange("bowHoldCostPerSecond",1.0,0,1000000);
        b.pop();
        b.push("recovery");
        CONTROLS_PROFILE_APPLIED=b.comment("One-time migration of existing worlds to the 3-second recovery preset. Afterwards baseDelaySeconds remains configurable.").define("controlsProfileApplied",false);
        RECOVERY_BASE_DELAY = b.comment("Delay in seconds after the last stamina expenditure at stamina_recovery_speed = 1. Actual delay = baseDelaySeconds * max(0, 2 - attribute), rounded up to game ticks. Attribute scaling is unchanged. 0 disables waiting, but never regenerates in the spending tick.")
            .defineInRange("baseDelaySeconds", 3.0, 0, 3600);
        b.pop();
        b.push("load");
        LIGHT_MAX = b.comment("Light load includes all weights <= this boundary, including negative values.")
            .defineInRange("lightMaxWeight", 40.0, -1000000, 1000000);
        MEDIUM_MAX = b.comment("Medium load: above lightMaxWeight up to this boundary inclusive. Heavy load: above this boundary, with no upper limit. Must be >= lightMaxWeight; lower values are treated as lightMaxWeight.")
            .defineInRange("mediumMaxWeight", 80.0, -1000000, 1000000);
        LIGHT_COST = b.comment("ParCool stamina cost multipliers: 1 = original cost, 1.4 = +40%, 2 = double. Spells are unaffected.")
            .defineInRange("lightCostMultiplier", 1.0, 0, 1000);
        MEDIUM_COST = b.defineInRange("mediumCostMultiplier", 1.4, 0, 1000);
        HEAVY_COST = b.defineInRange("heavyCostMultiplier", 2.0, 0, 1000);
        b.pop();
        b.push("heavyActions");
        RESTRICT_HEAVY_ACTIONS = b.comment("When true, listed ParCool actions cannot start or continue under heavy load. False allows them subject to normal ParCool checks.")
            .define("restrictListedActions", true);
        HEAVY_BLOCKED_ACTIONS = b.comment("ParCool action class names, case-insensitive: Dodge, FastRun, ClimbUp, etc. Empty list blocks nothing.")
            .defineListAllowEmpty("blockedActions", List.of("Dodge"), v -> v instanceof String s && s.matches("[A-Za-z][A-Za-z0-9]*"));
        b.pop();
        b.push("shield");
        SHIELD_BASE_DAMAGE = b.comment("Shared stamina damage per blocked melee hit = baseStaminaDamage * first matching weapon multiplier. Ignores hit damage and player weight. Parries stay free; projectiles and explosions keep their separate costs.")
            .defineInRange("baseStaminaDamage", 10.0, 0, 1000000);
        SHIELD_DEFAULT_MULTIPLIER = b.comment("Used when no weapon rule matches; 0 makes unmatched weapons free to block.")
            .defineInRange("defaultWeaponMultiplier", 1.0, 0, 1000);
        SHIELD_WEAPON_RULES = b.comment("Ordered rules, FIRST match wins. Remove a rule to disable it, use [] for only the default multiplier. Put Tetra/NBT rules before generic tags.",
                "Format: item=namespace:id;multiplier=1.5 OR tag=namespace:tag;multiplier=1.5.",
                "Optional NBT condition: ;nbt=sword/blade;contains=heavy_blade OR ;nbt=custom.type;equals=hammer.",
                "Dots traverse nested compounds; / is a literal character in Tetra keys. item=* matches any item. NBT comparisons are case-sensitive. Multiplier range 0..1000.")
            .defineListAllowEmpty("weaponRules", ShieldWeaponRule.defaults(), ShieldWeaponRule::valid);
        b.pop();
        b.push("epicGuard");
        WEAPON_GUARD_BASE_DAMAGE=b.comment("Fixed stamina cost of an ordinary weapon block, before shield.weaponRules and shield_cost_reduction. Default is twice shield.baseStaminaDamage. Independent of impact, weight and accumulated penalty; successful parries retain their native cost.").defineInRange("weaponBlockStaminaDamage",20.0,0,1000000);
        WEAPON_GUARD_MELEE_DAMAGE=b.comment("Fraction of incoming damage passing through an ordinary weapon block, before armor. Successful parries and vanilla shields are unaffected.").defineInRange("weaponBlockMeleeDamageFraction",0.3,0,1);
        WEAPON_GUARD_PROJECTILE_DAMAGE=b.defineInRange("weaponBlockProjectileDamageFraction",1.0,0,1);
        PARRY_WINDOW=b.defineInRange("shieldParryWindowTicks",8,1,40);
        FAILED_PARRY_COOLDOWN=b.comment("Shared sword/shield retry cooldown after releasing a guard attempt without a parry. Holding a normal block is allowed.").defineInRange("failedParryCooldownTicks",14,1,200);
        GUARD_BREAK_COOLDOWN=b.defineInRange("guardBreakCooldownTicks",40,1,1200);
        b.pop();
        SPEC = b.build();
    }
    private StaminaConfig() {}
}
