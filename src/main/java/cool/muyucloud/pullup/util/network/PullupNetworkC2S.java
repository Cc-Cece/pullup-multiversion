package cool.muyucloud.pullup.util.network;

/*? if fabric {*/
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
/*? if >=1.20.6 {*/
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
/*?} else {*/
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.util.Identifier;
*/
/*?}*/
import net.minecraft.server.MinecraftServer;

import java.util.Date;

public class PullupNetworkC2S {
    private static final Config CONFIG = Pullup.getConfig();
    /*? if >=1.20.6 {*/
    /*private static boolean payloadsRegistered;*/
    /*?} else {*/
    public static final Identifier GRAB_CONDITIONS = new Identifier("pullup:grab_conditions");
    /*?}*/
    private static long LAST_SEND = new Date().getTime();

    /*? if >=1.20.6 {*/
    /*public static void registerPayloadTypes() {
        if (payloadsRegistered) {
            return;
        }
        payloadsRegistered = true;
        PayloadTypeRegistry.playC2S().register(GrabConditionsPayload.ID, GrabConditionsPayload.CODEC);
    }*/
    /*?}*/

    public static void registerReceive() {
        /*? if >=1.20.6 {*/
        /*ServerPlayNetworking.registerGlobalReceiver(
            GrabConditionsPayload.ID,
            (payload, context) -> receiveGrab(context.server(), context.player())
        );*/
        /*?} else {*/
        ServerPlayNetworking.registerGlobalReceiver(GRAB_CONDITIONS,
            (server, player, handler, buf, responseSender) -> receiveGrab(server, responseSender));
        /*?}*/
    }

    private static void receiveGrab(
        MinecraftServer server,
        /*? if >=1.20.6 {*/
        /*ServerPlayerEntity player*/
        /*?} else {*/
        PacketSender sender
        /*?}*/
    ) {
        server.execute(() -> {
            long tmp = new Date().getTime();
            if (LAST_SEND + CONFIG.getAsInt("sendDelay") >= tmp || !CONFIG.getAsBool("sendServer")) {
                /*? if >=1.20.6 {*/
                /*PullupNetworkS2C.sendRefuse(player);*/
                /*?} else {*/
                sender.sendPacket(PullupNetworkS2C.REFUSE, PacketByteBufs.empty());
                /*?}*/
                return;
            }
            LAST_SEND = tmp;
            /*? if >=1.20.6 {*/
            /*PullupNetworkS2C.sendClear(player);
            PullupNetworkS2C.sendLoad(player);*/
            /*?} else {*/
            sender.sendPacket(PullupNetworkS2C.CLEAR_CONDITIONS, PacketByteBufs.empty());
            sender.sendPacket(PullupNetworkS2C.LOAD_CONDITIONS, PullupNetworkS2C.assembleLoadBuf());
            /*?}*/
        });
    }

    public static void sendGrab() {
        /*? if >=1.20.6 {*/
        /*ClientPlayNetworking.send(GrabConditionsPayload.INSTANCE);*/
        /*?} else {*/
        ClientPlayNetworking.send(GRAB_CONDITIONS, PacketByteBufs.empty());
        /*?}*/
    }

    /*? if >=1.20.6 {*/
    /*private record GrabConditionsPayload() implements CustomPayload {
        private static final GrabConditionsPayload INSTANCE = new GrabConditionsPayload();
        private static final CustomPayload.Id<GrabConditionsPayload> ID = new CustomPayload.Id<>(
            PlatformIds.of("pullup", "grab_conditions")
        );
        private static final PacketCodec<RegistryByteBuf, GrabConditionsPayload> CODEC = PacketCodec.unit(INSTANCE);

        @Override
        public CustomPayload.Id<GrabConditionsPayload> getId() {
            return ID;
        }
    }*/
    /*?}*/
}
/*?}*/
