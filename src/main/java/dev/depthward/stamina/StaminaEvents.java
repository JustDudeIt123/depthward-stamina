package dev.depthward.stamina;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;


public final class StaminaEvents {
    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        StaminaState.regenerate(p);
        if (p.tickCount % 2 == 0) StaminaNetwork.sync(p);
    }
    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent e) {
        if(!StaminaConfig.CONTROLS_PROFILE_APPLIED.get()){
            StaminaConfig.RECOVERY_BASE_DELAY.set(3.0);
            StaminaConfig.CONTROLS_PROFILE_APPLIED.set(true);
        }
        sync(e);
    }
    @SubscribeEvent
    public void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { sync(e); }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void respawn(PlayerEvent.PlayerRespawnEvent e) {
        // Iron's native spawn amount and max_mana no longer determine this pool.
        StaminaState.set(e.getEntity(), StaminaState.max(e.getEntity()));
        sync(e);
    }
    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone e) {
        if (!e.isWasDeath()) {
            StaminaState.set(e.getEntity(), StaminaState.get(e.getOriginal()));
            StaminaState.copyRecoveryDelay(e.getOriginal(), e.getEntity());
        }
    }
    private void sync(PlayerEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) StaminaNetwork.sync(p);
    }
}
