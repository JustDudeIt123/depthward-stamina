package dev.depthward.stamina.client;
import dev.depthward.stamina.*;
import net.minecraft.client.Minecraft;
public final class CoreStaminaClient {
 public static void receive(StaminaNetwork.Snapshot packet){
  var p=Minecraft.getInstance().player;if(p==null)return;
  StaminaState.receive(p,packet.current(),packet.maximum());GuardState.receive(p,packet.guardCooldown());
  StaminaBridge.hooks().received(packet);
 }
}
