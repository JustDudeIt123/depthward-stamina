package dev.depthward.stamina;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(DepthwardStamina.ID)
public final class DepthwardStamina {
    public static final String ID = "depthward_stamina";
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ID);
    public static final RegistryObject<Attribute> MAX = ATTRIBUTES.register("max_stamina",
        () -> new RangedAttribute("attribute.depthward_stamina.max_stamina", 100, 1, 1000000).setSyncable(true));
    public static final RegistryObject<Attribute> REGEN = ATTRIBUTES.register("stamina_regeneration",
        () -> new RangedAttribute("attribute.depthward_stamina.stamina_regeneration", 20, 0, 1000000).setSyncable(true));
    public static final RegistryObject<Attribute> RECOVERY = ATTRIBUTES.register("stamina_recovery_speed",
        () -> new RangedAttribute("attribute.depthward_stamina.stamina_recovery_speed", 1, 0, 2).setSyncable(true));
    public static final RegistryObject<Attribute> WEIGHT = ATTRIBUTES.register("weight",
        WeightAttributes::create);
    public static final RegistryObject<Attribute> PARCOOL_REDUCTION = ATTRIBUTES.register("parcool_cost_reduction",
        () -> new RangedAttribute("attribute.depthward_stamina.parcool_cost_reduction", 0, 0, 100).setSyncable(true));
    public static final RegistryObject<Attribute> SPELL_REDUCTION = ATTRIBUTES.register("spell_cost_reduction",
        () -> new RangedAttribute("attribute.depthward_stamina.spell_cost_reduction", 0, 0, 100).setSyncable(true));
    public static final RegistryObject<Attribute> SHIELD_REDUCTION = ATTRIBUTES.register("shield_cost_reduction",
        () -> new RangedAttribute("attribute.depthward_stamina.shield_cost_reduction", 0, 0, 100).setSyncable(true));
    public static final RegistryObject<Attribute> EPIC_REDUCTION = ATTRIBUTES.register("epicfight_cost_reduction",
        () -> new RangedAttribute("attribute.depthward_stamina.epicfight_cost_reduction", 0, -1000, 100).setSyncable(true));
    public static final RegistryObject<Attribute> BOW_REDUCTION = ATTRIBUTES.register("bow_hold_cost_reduction",
        () -> new RangedAttribute("attribute.depthward_stamina.bow_hold_cost_reduction",0,0,100).setSyncable(true));
    public DepthwardStamina() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ATTRIBUTES.register(bus);
        bus.addListener(this::attributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, StaminaConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new StaminaEvents());
        StaminaNetwork.register();
    }
    private void attributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, BOW_REDUCTION.get());
        event.add(EntityType.PLAYER, MAX.get());
        event.add(EntityType.PLAYER, REGEN.get());
        event.add(EntityType.PLAYER, RECOVERY.get());
        event.add(EntityType.PLAYER, WEIGHT.get());
        event.add(EntityType.PLAYER, PARCOOL_REDUCTION.get());
        event.add(EntityType.PLAYER, SPELL_REDUCTION.get());
        event.add(EntityType.PLAYER, SHIELD_REDUCTION.get());
        event.add(EntityType.PLAYER, EPIC_REDUCTION.get());
    }
}
