package dev.depthward.stamina;

import net.minecraft.world.entity.player.Player;

/** Weight comes only from the player's attribute, never from item IDs or armor slots. */
public final class StaminaWeight {
    public static int tier(Player player) {
        return StaminaMath.loadTier(player.getAttributeValue(DepthwardStamina.WEIGHT.get()),
            StaminaConfig.LIGHT_MAX.get(), StaminaConfig.MEDIUM_MAX.get());
    }
    public static double multiplier(Player player) {
        return switch (tier(player)) {
            case 0 -> StaminaConfig.LIGHT_COST.get();
            case 1 -> StaminaConfig.MEDIUM_COST.get();
            default -> StaminaConfig.HEAVY_COST.get();
        };
    }
    public static boolean blocksAction(Player player, String actionName) {
        return StaminaConfig.RESTRICT_HEAVY_ACTIONS.get() && tier(player) == 2
            && StaminaConfig.HEAVY_BLOCKED_ACTIONS.get().stream().anyMatch(actionName::equalsIgnoreCase);
    }
    private StaminaWeight() {}
}
