package cool.muyucloud.pullup;
import cool.muyucloud.pullup.cloud.CloudImportState;
/*? if >=26.1 {*/
/*import cool.muyucloud.pullup.v26.Pullup26ClientCommand;
import cool.muyucloud.pullup.v26.Pullup26FabricNetworking;
import cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking;
import cool.muyucloud.pullup.v26.Pullup26Runtime;
import cool.muyucloud.pullup.v26.Pullup26ServerCommand;*/
/*?}*/
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
/*? if <26.1 {*/
import cool.muyucloud.pullup.util.command.ClientCommand;
import cool.muyucloud.pullup.util.command.ServerCommand;
import cool.muyucloud.pullup.util.network.PullupNetworking;
/*?}*/
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*? if fabric {*/
import cool.muyucloud.pullup.adapter.client.ConditionManagerScreen;
import net.fabricmc.api.ModInitializer;
/*? if <=1.18.2 {*/
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
*/
/*?} else {*/
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
/*?}*/
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
*/
/*?}*/

/*? if forge {*/
/*@net.minecraftforge.fml.common.Mod("pullup")
public class Pullup {*/
/*?}*/

/*? if neoforge {*/
/*@net.neoforged.fml.common.Mod("pullup")
public class Pullup {*/
/*?}*/

/*? if fabric {*/
public class Pullup implements ModInitializer {
/*?}*/

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();
    private static final CloudImportState CLOUD_IMPORT_STATE = new CloudImportState();
    private static final String MOD_VERSION = detectModVersion();

    /*? if forge {*/
    /*public Pullup() {
        bootstrap();
        MinecraftForge.EVENT_BUS.addListener(this::onServerStoppingEvent);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(cool.muyucloud.pullup.adapter.client.PullupHudRenderer::onForgeLegacyRender);
            MinecraftForge.EVENT_BUS.addListener(cool.muyucloud.pullup.util.command.ClientCommand::registerForgeClientCommands);
            MinecraftForge.EVENT_BUS.addListener(this::onForgeClientTick);
        }
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*? if <26.1 {*/
    /*public Pullup(net.neoforged.bus.api.IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        bootstrap();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onServerStoppingEvent);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.adapter.client.PullupHudRenderer::onNeoForgeLegacyRender);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.util.command.ClientCommand::registerNeoForgeClientCommands);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onNeoForgeClientTickLegacy);
        }
    }*/
    /*?}*/
    /*? if >=26.1 {*/
        /*public Pullup(net.neoforged.bus.api.IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        bootstrap();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onServerStoppingEvent);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking::onPlayerLoggedIn);
        modEventBus.addListener(cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking::registerPayloadHandlers);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::onClientTick26);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.v26.Pullup26ClientCommand::registerNeoForgeClientCommands);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking::onClientLoggingIn);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking::onClientLoggingOut);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cool.muyucloud.pullup.adapter.client.PullupHudRenderer::onNeoForgeRender);
            modEventBus.addListener(cool.muyucloud.pullup.v26.Pullup26NeoForgeNetworking::registerClientPayloadHandlers);
        }
    }*/
    /*?}*/
    /*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        bootstrap();

        /*? if <26.1 {*/
        LOGGER.info("Registering commands.");
        /*? if <=1.18.2 {*/
        /*CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ServerCommand.register(dispatcher));*/
        /*?} else {*/
        CommandRegistrationCallback.EVENT.register((dispatcher, access, dedicated) -> ServerCommand.register(dispatcher));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> ClientCommand.register(dispatcher));
        /*?}*/

        LOGGER.info("Registering lifecycle events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(Pullup::onServerStopping);
        /*? if >1.18.2 {*/
        ClientTickEvents.END_CLIENT_TICK.register(client -> ConditionManagerScreen.openIfRequested());
        /*?}*/
        /*?} else {*/
        /*LOGGER.info("Registering 26.1 commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, access, dedicated) -> Pullup26ServerCommand.register(dispatcher));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Pullup26ClientCommand.register(dispatcher));

        LOGGER.info("Registering 26.1 networking.");
        Pullup26FabricNetworking.register();

        LOGGER.info("Registering 26.1 engine-backed runtime.");
        ClientTickEvents.END_CLIENT_TICK.register(client -> ConditionManagerScreen.openIfRequested());
        ClientTickEvents.END_CLIENT_TICK.register(Pullup26Runtime::onFabricClientTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(Pullup::onServerStopping);*/
        /*?}*/
    }
    /*?}*/

    private static void bootstrap() {
        LOGGER.info("Loading config.");
        CONFIG.loadAndCorrect();

        LOGGER.info("Registering arguments.");
        Registry.registerArguments();

        LOGGER.info("Registering operators.");
        Registry.registerOperators();

        /*? if <26.1 {*/
        LOGGER.info("Registering network.");
        PullupNetworking.registerReceivers();
        /*?}*/

        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public static CloudImportState getCloudImportState() {
        return CLOUD_IMPORT_STATE;
    }

    public static String getModVersion() {
        return MOD_VERSION;
    }

    private static String detectModVersion() {
        Package modPackage = Pullup.class.getPackage();
        if (modPackage != null && modPackage.getImplementationVersion() != null) {
            return modPackage.getImplementationVersion();
        }
        return "dev";
    }

    private static void onServerStopping(MinecraftServer server) {
        LOGGER.info("Dumping current config into file.");
        CONFIG.save();
        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
    }

    /*? if forge {*/
    /*private void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        ServerCommand.register(event.getDispatcher());
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*? if <26.1 {*/
    /*private void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        ServerCommand.register(event.getDispatcher());
    }*/
    /*?}*/

    /*? if >=26.1 {*/
    /*private void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        Pullup26ServerCommand.register(event.getDispatcher());
    }*/
    /*?}*/
    /*? if <26.1 {*/
    /*private void onNeoForgeClientTickLegacy(net.neoforged.neoforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == net.neoforged.neoforge.event.TickEvent.Phase.END) {
            cool.muyucloud.pullup.adapter.client.ConditionManagerScreen.openIfRequested();
        }
    }*/
    /*?}*/
    /*? if >=26.1 {*/
    /*private void onClientTick26(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        cool.muyucloud.pullup.adapter.client.ConditionManagerScreen.openIfRequested();
        Pullup26Runtime.onNeoForgeClientTick(event);
    }*/
    /*?}*/
    /*?}*/

    /*? if forge {*/
    /*private void onServerStoppingEvent(net.minecraftforge.event.server.ServerStoppingEvent event) {
        onServerStopping(event.getServer());
    }*/
    /*private void onForgeClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            cool.muyucloud.pullup.adapter.client.ConditionManagerScreen.openIfRequested();
        }
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*private void onServerStoppingEvent(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        onServerStopping(event.getServer());
    }*/
    /*?}*/
}
