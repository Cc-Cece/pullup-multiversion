package cool.muyucloud.pullup.v26;

import cool.muyucloud.pullup.util.ConditionSetRuntime;
import net.minecraft.server.MinecraftServer;

/*? if >=26.1 {*/
/*import net.minecraft.server.level.ServerPlayer;*/
/*?}*/

public final class Pullup26ServerSync {
    private Pullup26ServerSync() {}

    /*? if >=26.1 {*/
    /*public static void sendConfiguredConditions(ServerPlayer player) {
        sendClear(player);
        sendLoad(player);
    }

    public static void broadcastConfiguredConditions(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendConfiguredConditions(player);
        }
    }

    public static void sendClear(ServerPlayer player) {
        /^? if fabric {^/
        Pullup26FabricNetworking.sendClear(player);
        /^?} else if neoforge {^/
        Pullup26NeoForgeNetworking.sendClear(player);
        /^?}^/
    }

    public static void sendLoad(ServerPlayer player) {
        String loadSet = ConditionSetRuntime.readConfiguredSetName();
        String json = ConditionSetRuntime.readConfiguredSetContent();

        /^? if fabric {^/
        Pullup26FabricNetworking.sendLoad(player, loadSet, json);
        /^?} else if neoforge {^/
        Pullup26NeoForgeNetworking.sendLoad(player, loadSet, json);
        /^?}^/
    }*/
    /*?}*/
}
