package dev.depthward.stamina;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.ModList;

public final class WeightAttributes {
    public static Attribute create() {
        return StaminaBridge.weight();
    }
    private WeightAttributes() {}
}
