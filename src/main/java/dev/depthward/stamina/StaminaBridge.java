package dev.depthward.stamina;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
/** Small vanilla/Forge-only boundary. The optional compatibility mod supplies integrations. */
public final class StaminaBridge {
 public interface Hooks {
  default boolean blocksRecovery(Player p){return false;}
  default void cancelCast(ServerPlayer p){}
  default void received(StaminaNetwork.Snapshot snapshot){}
  default void stopParkour(int entityId,String action){}
 }
 private static Hooks hooks=new Hooks(){};
 private static Supplier<Attribute> weight=()->new RangedAttribute("attribute.depthward_stamina.weight",0,-1000000,1000000).setSyncable(true);
 public static Hooks hooks(){return hooks;}
 public static void install(Hooks value,Supplier<Attribute> factory){hooks=value;if(factory!=null)weight=factory;}
 public static Attribute weight(){return weight.get();}
 private StaminaBridge(){}
}
