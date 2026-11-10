package dev.depthward.stamina;

/** Pure arithmetic, shared by integrations and the tests. */
public final class StaminaMath {
    private StaminaMath() {}
    public static double clamp(double value, double maximum) {
        if (!Double.isFinite(value)) return 0;
        return Math.max(0, Math.min(value, Math.max(0, maximum)));
    }
    public static int loadTier(double weight, double lightMax, double mediumMax) {
        if (weight <= lightMax) return 0;
        return weight <= Math.max(lightMax, mediumMax) ? 1 : 2;
    }
    public static double regenerate(double current, double maximum, double perSecond) {
        return clamp(current + Math.max(0, perSecond) / 20, maximum);
    }
    public static double cost(double raw, double multiplier) { return Math.max(0, raw) * multiplier; }
    public static double reductionMultiplier(double percent) { return 1 - Math.max(0, Math.min(100, percent)) / 100; }
    public static int recoveryDelayTicks(double recoverySpeed, double baseDelaySeconds) {
        // Preserve attribute scaling; only the base delay is configurable.
        return (int)Math.ceil(20 * baseDelaySeconds * Math.max(0, 2 - recoverySpeed) - 1e-9);
    }
}
