package cool.muyucloud.pullup.util.network;

import net.minecraft.server.network.ServerPlayerEntity;

public final class PullupNetworking {
    private PullupNetworking() {}

    public static void registerReceivers() {
        /*? if fabric {*/
        /*? if >=1.20.6 {*/
        /*PullupNetworkS2C.registerPayloadTypes();
        PullupNetworkC2S.registerPayloadTypes();*/
        /*?}*/
        PullupNetworkS2C.registerReceive();
        PullupNetworkC2S.registerReceive();
        /*?}*/
    }

    public static void requestServerConditions() {
        /*? if fabric {*/
        PullupNetworkC2S.sendGrab();
        /*?}*/
    }

    public static void sendClear(ServerPlayerEntity player) {
        /*? if fabric {*/
        PullupNetworkS2C.sendClear(player);
        /*?}*/
    }

    public static void sendLoad(ServerPlayerEntity player) {
        /*? if fabric {*/
        PullupNetworkS2C.sendLoad(player);
        /*?}*/
    }
}
