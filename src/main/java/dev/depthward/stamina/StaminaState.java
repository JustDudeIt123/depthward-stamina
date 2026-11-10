package dev.depthward.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/** Server owns the saved pool. Client values are snapshots plus local consumption prediction. */
public final class StaminaState {
    private static final String KEY = "DepthwardStamina";
    private static CompoundTag data(Player p) {
        var root = p.getPersistentData();
        if (!root.contains(KEY)) {
            var tag = new CompoundTag();
            tag.putDouble("current", max(p));
            root.put(KEY, tag);
        }
        return root.getCompound(KEY);
    }
    public static double max(Player p) {
        if (p.level().isClientSide && p.getPersistentData().getCompound(KEY).contains("maximum"))
            return p.getPersistentData().getCompound(KEY).getDouble("maximum");
        return p.getAttribute(DepthwardStamina.MAX.get()) == null ? 100 : p.getAttributeValue(DepthwardStamina.MAX.get());
    }
    public static double get(Player p) { return StaminaMath.clamp(data(p).getDouble("current"), max(p)); }
    public static void set(Player p, double value) { data(p).putDouble("current", StaminaMath.clamp(value, max(p))); }
    public static void applyManaTarget(Player p, double target) {
        double current = get(p);
        set(p, target < current ? current - StaminaCosts.spellCost(p, current - target) : target);
        if (get(p) < current) markSpent(p);
    }
    public static boolean spend(Player p, double rawCost) {
        if (!Double.isFinite(rawCost) || rawCost < 0) return false;
        if (p.isCreative() || p.isSpectator()) return true;
        double cost = rawCost * StaminaCosts.parkourMultiplier(p);
        if (get(p) + 1e-6 < cost) return false;
        set(p, get(p) - cost);
        if (cost > 0) markSpent(p);
        return true;
    }
    public static boolean canAfford(Player p, double rawCost) {
        return p.isCreative() || p.isSpectator() || Double.isFinite(rawCost) && rawCost >= 0
            && get(p) + 1e-6 >= StaminaMath.cost(rawCost, StaminaCosts.parkourMultiplier(p));
    }
    /** Shield impacts ignore armor weight and can exhaust the pool. Other actions remain all-or-nothing. */
    public static void absorbImpact(Player p, double rawCost) {
        if (!Double.isFinite(rawCost) || rawCost <= 0 || p.isCreative() || p.isSpectator()) return;
        double before = get(p);
        set(p, before - StaminaCosts.shieldCost(p, rawCost));
        if (get(p) < before) markSpent(p);
    }
    /** Epic Fight amounts are already final; never apply mana or parkour discounts here. */
    public static void applyEpicTarget(Player p, double target) {
        if (!Double.isFinite(target)) return;
        double before = get(p);
        if ((p.isCreative() || p.isSpectator()) && target < before) return;
        set(p, target);
        if (get(p) < before) markSpent(p);
    }
    public static void regenerate(Player p) {
        // Holding guard continuously postpones recovery, including before any impact.
        if ((p.isUsingItem() && p.getUseItem().getUseAnimation()==net.minecraft.world.item.UseAnim.BLOCK)
                || StaminaBridge.hooks().blocksRecovery(p)) { markSpent(p); return; }
        var tag = data(p);
        if (tag.contains("lastSpendTick")) {
            long elapsed = p.level().getGameTime() - tag.getLong("lastSpendTick");
            int delay = StaminaMath.recoveryDelayTicks(p.getAttributeValue(DepthwardStamina.RECOVERY.get()),
                StaminaConfig.RECOVERY_BASE_DELAY.get());
            // Even with a zero delay, spending and regeneration cannot happen in the same tick.
            if (elapsed <= 0 || elapsed < delay) return;
        }
        set(p, StaminaMath.regenerate(get(p), max(p), p.getAttributeValue(DepthwardStamina.REGEN.get())));
    }
    private static void markSpent(Player p) { data(p).putLong("lastSpendTick", p.level().getGameTime()); }
    public static void copyRecoveryDelay(Player from, Player to) {
        if (data(from).contains("lastSpendTick")) data(to).putLong("lastSpendTick", data(from).getLong("lastSpendTick"));
    }
    public static void receive(Player p, double value, double maximum) {
        data(p).putDouble("maximum", maximum);
        set(p, value);
    }
    private StaminaState() {}
}
