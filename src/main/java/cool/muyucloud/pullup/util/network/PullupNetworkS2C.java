package cool.muyucloud.pullup.util.network;

/*? if fabric {*/
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import cool.muyucloud.pullup.util.Registry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
/*? if >=1.20.6 {*/
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
/*?} else {*/
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
*/
/*?}*/
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
/*? if <=1.18.2 {*/
/*import net.minecraft.text.TranslatableText;
*/
/*?}*/
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
public class PullupNetworkS2C {
    private static final Config CONFIG = Pullup.getConfig();
    private static final Logger LOGGER = Pullup.getLogger();
    /*? if >=1.20.6 {*/
    /*private static boolean payloadsRegistered;*/
    /*?} else {*/
    public static final Identifier CLEAR_CONDITIONS = new Identifier("pullup:clear_conditions");
    public static final Identifier LOAD_CONDITIONS = new Identifier("pullup:load_conditions");
    public static final Identifier REFUSE = new Identifier("pullup:refuse");
    /*?}*/

    /*? if >=1.20.6 {*/
    /*public static void registerPayloadTypes() {
        if (payloadsRegistered) {
            return;
        }
        payloadsRegistered = true;
        PayloadTypeRegistry.playS2C().register(ClearConditionsPayload.ID, ClearConditionsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LoadConditionsPayload.ID, LoadConditionsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RefusePayload.ID, RefusePayload.CODEC);
    }*/
    /*?}*/

    public static void registerReceive() {
        /*? if >=1.20.6 {*/
        /*ClientPlayNetworking.registerGlobalReceiver(ClearConditionsPayload.ID,
            (payload, context) -> receiveClear(context.client()));
        ClientPlayNetworking.registerGlobalReceiver(LoadConditionsPayload.ID,
            (payload, context) -> receiveLoad(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(RefusePayload.ID,
            (payload, context) -> receiveRefuse(context.client()));*/
        /*?} else {*/
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_CONDITIONS,
            (client, handler, buf, responseSender) -> receiveClear(client));
        ClientPlayNetworking.registerGlobalReceiver(LOAD_CONDITIONS,
            (client, handler, buf, responseSender) -> receiveLoad(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(REFUSE,
            (client, handler, buf, responseSender) -> receiveRefuse(client));
        /*?}*/
    }

    private static void receiveClear(MinecraftClient client) {
        client.execute(() -> {
            if (CONFIG.getAsBool("loadServer")) {
                Registry.CONDITIONS.clear();
            }
        });
    }

    private static void receiveLoad(
        MinecraftClient client,
        /*? if >=1.20.6 {*/
        /*LoadConditionsPayload payload*/
        /*?} else {*/
        PacketByteBuf buf
        /*?}*/
    ) {
        /*? if >=1.20.6 {*/
        /*String spaceName = payload.spaceName();
        String json = payload.json();*/
        /*?} else {*/
        String spaceName = buf.readString();
        String json = buf.readString();
        /*?}*/

        client.execute(() -> {
            if (CONFIG.getAsBool("loadServer")) {
                new ConditionLoader(spaceName, json).load();
            }
        });
    }

    private static void receiveRefuse(MinecraftClient client) {
        client.execute(() -> client.inGameHud.getChatHud().addMessage(
            translatable("network.client.pullup.refuse.receive").formatted(Formatting.RED)));
    }

    private static MutableText translatable(String key, Object... args) {
        /*? if <=1.18.2 {*/
        /*return new TranslatableText(key, args);*/
        /*?} else {*/
        return Text.translatable(key, args);
        /*?}*/
    }

    /**
     * Let client side remove history registries of conditions.
     * This should be executed before this::sendLoad.
     * Otherwise, historical conditions will still be executed, along with newly loaded conditions.
     */
    public static void sendClear(ServerPlayerEntity player) {
        /*? if >=1.20.6 {*/
        /*ServerPlayNetworking.send(player, ClearConditionsPayload.INSTANCE);*/
        /*?} else {*/
        ServerPlayNetworking.send(player, CLEAR_CONDITIONS, PacketByteBufs.empty());
        /*?}*/
    }

    /**
     * Send current loaded condition set to player.
     * Load operation will be done on client side.
     */
    public static void sendLoad(ServerPlayerEntity player) {
        /*? if >=1.20.6 {*/
        /*ServerPlayNetworking.send(
            player,
            new LoadConditionsPayload(
                ConditionSetRuntime.readConfiguredSetName(),
                ConditionSetRuntime.readConfiguredSetContent()
            )
        );*/
        /*?} else {*/
        ServerPlayNetworking.send(player, LOAD_CONDITIONS, assembleLoadBuf());
        /*?}*/
    }

    public static void sendRefuse(ServerPlayerEntity player) {
        /*? if >=1.20.6 {*/
        /*ServerPlayNetworking.send(player, RefusePayload.INSTANCE);*/
        /*?} else {*/
        ServerPlayNetworking.send(player, REFUSE, PacketByteBufs.empty());
        /*?}*/
    }

    /*? if <1.20.6 {*/
    public static PacketByteBuf assembleLoadBuf() {
        PacketByteBuf buf = PacketByteBufs.create();

        String loadSet = ConditionSetRuntime.readConfiguredSetName();
        buf.writeString(loadSet);
        buf.writeString(ConditionSetRuntime.readConfiguredSetContent());

        return buf;
    }
    /*?}*/

    /*? if >=1.20.6 {*/
    /*private record ClearConditionsPayload() implements CustomPayload {
        private static final ClearConditionsPayload INSTANCE = new ClearConditionsPayload();
        private static final CustomPayload.Id<ClearConditionsPayload> ID = new CustomPayload.Id<>(
            PlatformIds.of("pullup", "clear_conditions")
        );
        private static final PacketCodec<RegistryByteBuf, ClearConditionsPayload> CODEC = PacketCodec.unit(INSTANCE);

        @Override
        public CustomPayload.Id<ClearConditionsPayload> getId() {
            return ID;
        }
    }

    private record RefusePayload() implements CustomPayload {
        private static final RefusePayload INSTANCE = new RefusePayload();
        private static final CustomPayload.Id<RefusePayload> ID = new CustomPayload.Id<>(
            PlatformIds.of("pullup", "refuse")
        );
        private static final PacketCodec<RegistryByteBuf, RefusePayload> CODEC = PacketCodec.unit(INSTANCE);

        @Override
        public CustomPayload.Id<RefusePayload> getId() {
            return ID;
        }
    }

    private record LoadConditionsPayload(String spaceName, String json) implements CustomPayload {
        private static final CustomPayload.Id<LoadConditionsPayload> ID = new CustomPayload.Id<>(
            PlatformIds.of("pullup", "load_conditions")
        );
        private static final PacketCodec<RegistryByteBuf, LoadConditionsPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeString(payload.spaceName);
                buf.writeString(payload.json);
            },
            buf -> new LoadConditionsPayload(buf.readString(), buf.readString())
        );

        @Override
        public CustomPayload.Id<LoadConditionsPayload> getId() {
            return ID;
        }
    }*/
    /*?}*/
}
/*?}*/
