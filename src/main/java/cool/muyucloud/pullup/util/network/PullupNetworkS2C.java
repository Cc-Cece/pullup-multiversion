package cool.muyucloud.pullup.util.network;

/*? if fabric {*/
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
/*? if <=1.18.2 {*/
/*import net.minecraft.text.TranslatableText;
*/
/*?}*/
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
public class PullupNetworkS2C {
    private static final Config CONFIG = Pullup.getConfig();
    private static final Logger LOGGER = Pullup.getLogger();
    public static final Identifier CLEAR_CONDITIONS = new Identifier("pullup:clear_conditions");
    public static final Identifier LOAD_CONDITIONS = new Identifier("pullup:load_conditions");
    public static final Identifier REFUSE = new Identifier("pullup:refuse");

    public static void registerReceive() {
        ClientPlayNetworking.registerGlobalReceiver(CLEAR_CONDITIONS,
            (client, handler, buf, responseSender) -> receiveClear(client));
        ClientPlayNetworking.registerGlobalReceiver(LOAD_CONDITIONS,
            (client, handler, buf, responseSender) -> receiveLoad(client, buf));
        ClientPlayNetworking.registerGlobalReceiver(REFUSE,
            (client, handler, buf, responseSender) -> receiveRefuse(client));
    }

    private static void receiveClear(MinecraftClient client) {
        client.execute(() -> {
            if (CONFIG.getAsBool("loadServer")) {
                Registry.CONDITIONS.clear();
            }
        });
    }

    private static void receiveLoad(MinecraftClient client, PacketByteBuf buf) {
        String spaceName = buf.readString();
        String json = buf.readString();

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
        ServerPlayNetworking.send(player, CLEAR_CONDITIONS, PacketByteBufs.empty());
    }

    /**
     * Send current loaded condition set to player.
     * Load operation will be done on client side.
     */
    public static void sendLoad(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, LOAD_CONDITIONS, assembleLoadBuf());
    }

    public static PacketByteBuf assembleLoadBuf() {
        PacketByteBuf buf = PacketByteBufs.create();

        String loadSet = ConditionSetRuntime.readConfiguredSetName();
        buf.writeString(loadSet);
        buf.writeString(ConditionSetRuntime.readConfiguredSetContent());

        return buf;
    }
}
/*?}*/
