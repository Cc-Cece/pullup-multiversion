package cool.muyucloud.pullup.client;

/*? if fabric {*/
import cool.muyucloud.pullup.adapter.client.PullupHudRenderer;
import cool.muyucloud.pullup.compat.CompatHandler;
/*? if <26.1 {*/
import cool.muyucloud.pullup.util.command.ClientCommand;
/*?}*/
import net.fabricmc.api.ClientModInitializer;
/*? if <=1.18.2 {*/
/*import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;*/
/*?}*/

public class PullupClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PullupHudRenderer.registerFabric();
        /*? if <=1.18.2 && <26.1 {*/
        /*ClientCommand.register(ClientCommandManager.DISPATCHER);*/
        /*?}*/
        CompatHandler.init();
    }
}
/*?}*/
