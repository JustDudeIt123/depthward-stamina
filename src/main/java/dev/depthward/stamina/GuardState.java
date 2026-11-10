package dev.depthward.stamina;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
/** One shared sword/shield retry timer; swapping hands cannot bypass it. */
public final class GuardState {
    private static final String START="DepthwardGuardStart", OK="DepthwardGuardParried", UNTIL="DepthwardGuardUntil";
    public static long now(Player p) { return p.level().getGameTime(); }
    public static int remaining(Player p) { return (int)Math.max(0,p.getPersistentData().getLong(UNTIL)-now(p)); }
    public static boolean ready(Player p) { return remaining(p)==0; }
    public static void begin(Player p) { p.getPersistentData().putLong(START,now(p)); p.getPersistentData().putBoolean(OK,false); }
    public static boolean inWindow(Player p) { return !p.getPersistentData().getBoolean(OK) && p.getPersistentData().contains(START) && now(p)-p.getPersistentData().getLong(START)<StaminaConfig.PARRY_WINDOW.get(); }
    public static void success(Player p) { p.getPersistentData().putBoolean(OK,true); }
    public static void release(Player p) {
        if (!p.getPersistentData().contains(START)) return;
        if (!p.getPersistentData().getBoolean(OK)) cooldown(p,StaminaConfig.FAILED_PARRY_COOLDOWN.get());
        p.getPersistentData().remove(START);
    }
    public static void cooldown(Player p,int ticks) {
        p.getPersistentData().putLong(UNTIL,Math.max(now(p)+ticks,p.getPersistentData().getLong(UNTIL)));
        if(p instanceof ServerPlayer sp) StaminaNetwork.sync(sp);
    }
    public static void receive(Player p,int ticks) { p.getPersistentData().putLong(UNTIL,now(p)+ticks); }
    private GuardState() {}
}
