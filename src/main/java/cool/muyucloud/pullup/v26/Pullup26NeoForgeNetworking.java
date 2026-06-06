package cool.muyucloud.pullup.v26;

/*? if >=26.1 && neoforge {*/
/*import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Pullup26NeoForgeNetworking {
    private static final Config CONFIG = Pullup.getConfig();
    private static long lastSend = System.currentTimeMillis();

    private Pullup26NeoForgeNetworking() {}

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(GrabConditionsPayload.TYPE, GrabConditionsPayload.STREAM_CODEC, Pullup26NeoForgeNetworking::handleGrab);
        registrar.playToClient(ClearConditionsPayload.TYPE, ClearConditionsPayload.STREAM_CODEC);
        registrar.playToClient(LoadConditionsPayload.TYPE, LoadConditionsPayload.STREAM_CODEC);
        registrar.playToClient(RefuseConditionsPayload.TYPE, RefuseConditionsPayload.STREAM_CODEC);
    }

    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ClearConditionsPayload.TYPE, (payload, context) -> {
            if (CONFIG.getAsBool("loadServer")) {
                Pullup26Runtime.clearLoadedConditions();
            }
        });
        event.register(LoadConditionsPayload.TYPE, (payload, context) -> {
            if (CONFIG.getAsBool("loadServer")) {
                Pullup26Runtime.applyRemoteConditions(payload.setName(), payload.json());
            }
        });
        event.register(RefuseConditionsPayload.TYPE, (payload, context) ->
            context.player().sendSystemMessage(
                Component.translatable("network.client.pullup.refuse.receive").copy().withStyle(ChatFormatting.RED)
            )
        );
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (CONFIG.getAsBool("sendServer") && event.getEntity() instanceof ServerPlayer player) {
            Pullup26ServerSync.sendConfiguredConditions(player);
        }
    }

    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Pullup26Runtime.onClientJoin(CONFIG.getAsBool("loadServer"));
    }

    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Pullup26Runtime.onClientDisconnect();
    }

    public static void sendClear(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ClearConditionsPayload.INSTANCE);
    }

    public static void requestServerConditions() {
        ClientPacketDistributor.sendToServer(GrabConditionsPayload.INSTANCE);
    }

    public static void sendLoad(ServerPlayer player, String setName, String json) {
        PacketDistributor.sendToPlayer(player, new LoadConditionsPayload(setName, json));
    }

    private static void handleGrab(GrabConditionsPayload payload, IPayloadContext context) {
        long now = System.currentTimeMillis();
        if (lastSend + CONFIG.getAsInt("sendDelay") >= now || !CONFIG.getAsBool("sendServer")) {
            context.reply(RefuseConditionsPayload.INSTANCE);
            return;
        }

        if (context.player() instanceof ServerPlayer player) {
            lastSend = now;
            Pullup26ServerSync.sendConfiguredConditions(player);
        }
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
public final class Pullup26NeoForgeNetworking {
    private Pullup26NeoForgeNetworking() {}
}
/*?}*/
