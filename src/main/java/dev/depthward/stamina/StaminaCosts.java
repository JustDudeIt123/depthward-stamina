package dev.depthward.stamina;

import net.minecraft.world.entity.player.Player;

public final class StaminaCosts {
    public static double parkourMultiplier(Player p) {
        return StaminaWeight.multiplier(p) * StaminaMath.reductionMultiplier(p.getAttributeValue(DepthwardStamina.PARCOOL_REDUCTION.get()));
    }
    public static double spellMultiplier(Player p) {
        return StaminaMath.reductionMultiplier(p.getAttributeValue(DepthwardStamina.SPELL_REDUCTION.get()));
    }
    public static double spellCost(Player p, double raw) { return StaminaMath.cost(raw, spellMultiplier(p)); }
    public static float availableSpellMana(Player p) {
        double multiplier = spellMultiplier(p);
        return multiplier == 0 ? Float.MAX_VALUE : (float)(StaminaState.get(p) / multiplier);
    }
    public static double shieldMultiplier(Player p) {
        return StaminaMath.reductionMultiplier(p.getAttributeValue(DepthwardStamina.SHIELD_REDUCTION.get()));
    }
    public static double shieldCost(Player p, double raw) { return StaminaMath.cost(raw, shieldMultiplier(p)); }
    public static double epicCost(Player p, double raw) {
        return StaminaMath.cost(raw, Math.max(0, 1-p.getAttributeValue(DepthwardStamina.EPIC_REDUCTION.get())/100));
    }
    private StaminaCosts() {}
}
