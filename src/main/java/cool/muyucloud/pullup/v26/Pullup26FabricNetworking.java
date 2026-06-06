package cool.muyucloud.pullup.v26;

/*? if >=26.1 && fabric {*/
/*import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class Pullup26FabricNetworking {
    private static final Config CONFIG = Pullup.getConfig();
    private static long lastSend = System.currentTimeMillis();

    private Pullup26FabricNetworking() {}

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(GrabConditionsPayload.TYPE, GrabConditionsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClearConditionsPayload.TYPE, ClearConditionsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LoadConditionsPayload.TYPE, LoadConditionsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RefuseConditionsPayload.TYPE, RefuseConditionsPayload.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClearConditionsPayload.TYPE, (payload, context) -> {
            if (CONFIG.getAsBool("loadServer")) {
                Pullup26Runtime.clearLoadedConditions();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(LoadConditionsPayload.TYPE, (payload, context) -> {
            if (CONFIG.getAsBool("loadServer")) {
                Pullup26Runtime.applyRemoteConditions(payload.setName(), payload.json());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(RefuseConditionsPayload.TYPE, (payload, context) ->
            context.player().sendSystemMessage(
                Component.translatable("network.client.pullup.refuse.receive").copy().withStyle(ChatFormatting.RED)
            )
        );
        ServerPlayNetworking.registerGlobalReceiver(GrabConditionsPayload.TYPE, (payload, context) -> handleGrab(context.player()));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
            Pullup26Runtime.onClientJoin(CONFIG.getAsBool("loadServer")));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Pullup26Runtime.onClientDisconnect());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (CONFIG.getAsBool("sendServer")) {
                Pullup26ServerSync.sendConfiguredConditions(handler.getPlayer());
            }
        });
    }

    public static void requestServerConditions() {
        ClientPlayNetworking.send(GrabConditionsPayload.INSTANCE);
    }

    public static void sendClear(ServerPlayer player) {
        ServerPlayNetworking.send(player, ClearConditionsPayload.INSTANCE);
    }

    public static void sendLoad(ServerPlayer player, String setName, String json) {
        ServerPlayNetworking.send(player, new LoadConditionsPayload(setName, json));
    }

    private static void handleGrab(ServerPlayer player) {
        long now = System.currentTimeMillis();
        if (lastSend + CONFIG.getAsInt("sendDelay") >= now || !CONFIG.getAsBool("sendServer")) {
            ServerPlayNetworking.send(player, RefuseConditionsPayload.INSTANCE);
            return;
        }

        lastSend = now;
        Pullup26ServerSync.sendConfiguredConditions(player);
    }

    private enum GrabConditionsPayload implements CustomPacketPayload {
        INSTANCE;

        private static final Type<GrabConditionsPayload> TYPE = new Type<>(PlatformIds.parse("pullup:grab_conditions"));
        private static final StreamCodec<RegistryFriendlyByteBuf, GrabConditionsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private enum ClearConditionsPayload implements CustomPacketPayload {
        INSTANCE;

        private static final Type<ClearConditionsPayload> TYPE = new Type<>(PlatformIds.parse("pullup:clear_conditions"));
        private static final StreamCodec<RegistryFriendlyByteBuf, ClearConditionsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private enum RefuseConditionsPayload implements CustomPacketPayload {
        INSTANCE;

        private static final Type<RefuseConditionsPayload> TYPE = new Type<>(PlatformIds.parse("pullup:refuse"));
        private static final StreamCodec<RegistryFriendlyByteBuf, RefuseConditionsPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private record LoadConditionsPayload(String setName, String json) implements CustomPacketPayload {
        private static final int MAX_JSON_SIZE = 1_048_576;
        private static final Type<LoadConditionsPayload> TYPE = new Type<>(PlatformIds.parse("pullup:load_conditions"));
        private static final StreamCodec<RegistryFriendlyByteBuf, LoadConditionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),
            LoadConditionsPayload::setName,
            ByteBufCodecs.stringUtf8(MAX_JSON_SIZE),
            LoadConditionsPayload::json,
            LoadConditionsPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}*/
/*?} else {*/
public final class Pullup26FabricNetworking {
    private Pullup26FabricNetworking() {}

    public static void register() {
    }

    public static void requestServerConditions() {
    }
}
/*?}*/
