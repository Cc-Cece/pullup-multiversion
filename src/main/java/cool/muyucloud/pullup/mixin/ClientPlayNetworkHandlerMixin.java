package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Config;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique
    private static final Logger LOGGER = Pullup.getLogger();
    @Unique
    private static final Config CONFIG = Pullup.getConfig();

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        /*? if fabric {*/
        boolean loadServer = CONFIG.getAsBool("loadServer");
        /*?} else {*/
        /*boolean loadServer = false;*/
        /*?}*/
        if (!loadServer) {
            try {
                this.loadConditions();
            } catch (Exception e) {
                LOGGER.error("Try to load local condition set but failed.", e);
            }
        }
    }

    @Unique
    private void loadConditions() {
        ConditionSetRuntime.loadConfiguredLocalSet();
    }
}
