package dev.depthward.stamina;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StaminaMathTest {
    @Test void percentageSavingsHaveNeutralDefaultAndFreeMaximum() {
        assertEquals(1, StaminaMath.reductionMultiplier(0));
        assertEquals(0.5, StaminaMath.reductionMultiplier(50));
        assertEquals(0, StaminaMath.reductionMultiplier(100));
        assertEquals(0, StaminaMath.reductionMultiplier(200));
        assertEquals(1, StaminaMath.reductionMultiplier(-20));
    }

    @Test void configurableRecoveryBasePreservesAttributeScaling() {
        assertEquals(60, StaminaMath.recoveryDelayTicks(1, 3));
        assertEquals(48, StaminaMath.recoveryDelayTicks(1.2, 3));
        assertEquals(30, StaminaMath.recoveryDelayTicks(1.5, 3));
        assertEquals(0, StaminaMath.recoveryDelayTicks(2, 3));
        assertEquals(120, StaminaMath.recoveryDelayTicks(0, 3));
        assertEquals(144000, StaminaMath.recoveryDelayTicks(0, 3600));
    }
    @Test void configurableRecoveryBaseSupportsZeroAndRoundsUpToTicks() {
        for (double speed : new double[] {0, 1, 1.2, 2}) assertEquals(0, StaminaMath.recoveryDelayTicks(speed, 0));
        assertEquals(2, StaminaMath.recoveryDelayTicks(1, 0.051));
        assertEquals(1, StaminaMath.recoveryDelayTicks(1, 0.025));
        assertEquals(15, StaminaMath.recoveryDelayTicks(1.5, 1.5));
    }
    @Test void loadBoundariesIncludeNegativesAndFractionalWeights() {
        for (double weight : new double[] {-100, 0, 40}) assertEquals(0, StaminaMath.loadTier(weight, 40, 80));
        for (double weight : new double[] {40.5, 41, 80}) assertEquals(1, StaminaMath.loadTier(weight, 40, 80));
        for (double weight : new double[] {80.5, 81, 1000000}) assertEquals(2, StaminaMath.loadTier(weight, 40, 80));
    }
    @Test void loadBoundariesAreConfigurable() {
        assertEquals(0, StaminaMath.loadTier(20, 20, 60));
        assertEquals(1, StaminaMath.loadTier(30, 20, 60));
        assertEquals(2, StaminaMath.loadTier(61, 20, 60));
    }
    @Test void recoveryDelayDecreasesLinearly() {
        assertEquals(30, StaminaMath.recoveryDelayTicks(1, 1.5));
        assertEquals(24, StaminaMath.recoveryDelayTicks(1.2, 1.5));
        assertEquals(15, StaminaMath.recoveryDelayTicks(1.5, 1.5));
        assertEquals(0, StaminaMath.recoveryDelayTicks(2, 1.5));
        assertEquals(60, StaminaMath.recoveryDelayTicks(0, 1.5));
    }
    @Test void fullRecoveryTakesFiveSeconds() {
        double current = 0;
        for (int i = 0; i < 100; i++) current = StaminaMath.regenerate(current, 100, 20);
        assertEquals(100, current, 1e-9);
    }
    @Test void recoveryUsesModifiedAttributes() {
        assertEquals(102, StaminaMath.regenerate(100, 150, 40), 1e-9);
    }
    @Test void recoveryNeverOverflowsMaximum() {
        assertEquals(100, StaminaMath.regenerate(99.5, 100, 20));
    }
    @Test void weightRaisesCostWithoutChangingPool() {
        assertEquals(1, StaminaMath.loadTier(60, 40, 80));
        assertEquals(12, StaminaMath.cost(10, 1.2));
    }
    @Test void unarmoredCostIsOneToOne() {
        assertEquals(80, StaminaMath.cost(80, 1));
    }
    @Test void fractionalSpellCostsRemainPrecise() {
        assertEquals(1.01, StaminaMath.cost(1, 1.01), 1e-9);
    }
    @Test void invalidOrNegativeResourcesCannotFillPool() {
        assertEquals(0, StaminaMath.clamp(Double.NaN, 100));
        assertEquals(0, StaminaMath.clamp(-5, 100));
        assertEquals(0, StaminaMath.cost(-5, 1));
    }
    @Test void integerCostCannotOverflowToNegative() {
        assertTrue(StaminaMath.cost(Integer.MAX_VALUE, 100) > Integer.MAX_VALUE);
    }
}
