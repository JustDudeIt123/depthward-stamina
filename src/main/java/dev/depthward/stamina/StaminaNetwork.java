package dev.depthward.stamina;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class StaminaNetwork {
    private static final String VERSION = "7-epic";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(DepthwardStamina.ID, "stamina"), () -> VERSION, VERSION::equals, VERSION::equals);
    public static void register() {
        CHANNEL.messageBuilder(Snapshot.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(Snapshot::encode).decoder(Snapshot::decode).consumerMainThread(Snapshot::handle).add();
        CHANNEL.messageBuilder(Spend.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder((p, b) -> b.writeVarInt(p.rawCost)).decoder(b -> new Spend(b.readVarInt()))
            .consumerMainThread(Spend::handle).add();
        CHANNEL.messageBuilder(Interrupt.class, 2, NetworkDirection.PLAY_TO_SERVER)
            .encoder((p, b) -> {}).decoder(b -> new Interrupt())
            .consumerMainThread(Interrupt::handle).add();
        CHANNEL.messageBuilder(StopParkour.class,3,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((p,b)->{b.writeVarInt(p.entityId);b.writeUtf(p.action);})
            .decoder(b->new StopParkour(b.readVarInt(),b.readUtf(64)))
            .consumerMainThread(StopParkour::handle).add();
    }
    public record StopParkour(int entityId,String action) {
        static void handle(StopParkour p,Supplier<NetworkEvent.Context> ctx){
            StaminaBridge.hooks().stopParkour(p.entityId,p.action);
            ctx.get().setPacketHandled(true);
        }
    }
    public static void stopParkour(ServerPlayer p,String action){
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),new StopParkour(p.getId(),action));
    }
    public static void cancelCast() { CHANNEL.sendToServer(new Interrupt()); }
    public record Interrupt() {
        static void handle(Interrupt packet, Supplier<NetworkEvent.Context> ctx) {
            StaminaBridge.hooks().cancelCast(ctx.get().getSender());
            ctx.get().setPacketHandled(true);
        }
    }
    public static void sync(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Snapshot(StaminaState.get(player),
            StaminaState.max(player), GuardState.remaining(player)));
    }
    public static void spend(int rawCost) { CHANNEL.sendToServer(new Spend(rawCost)); }
    public record Snapshot(double current, double maximum, int guardCooldown) {
        void encode(FriendlyByteBuf b) { b.writeDouble(current); b.writeDouble(maximum); b.writeVarInt(guardCooldown); }
        static Snapshot decode(FriendlyByteBuf b) { return new Snapshot(b.readDouble(), b.readDouble(), b.readVarInt()); }
        static void handle(Snapshot p, Supplier<NetworkEvent.Context> ctx) {
            // This branch runs only on a physical client, on its main thread.
            dev.depthward.stamina.client.CoreStaminaClient.receive(p);
            ctx.get().setPacketHandled(true);
        }
    }
    public record Spend(int rawCost) {
        static void handle(Spend packet, Supplier<NetworkEvent.Context> ctx) {
            var player = ctx.get().getSender();
            // Clients may request spending, never supply a balance, weight, maximum or regeneration.
            if (player != null && packet.rawCost > 0 && packet.rawCost <= 1000000) {
                // Legacy raw-cost requests cannot bypass the authoritative action policy.
                sync(player);
            }
            ctx.get().setPacketHandled(true);
        }
    }
    private StaminaNetwork() {}
}
