package cool.muyucloud.pullup.util.command;

/*? if <26.1 {*/
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
/*? if >1.18.2 {*/
import cool.muyucloud.pullup.adapter.client.ConditionManagerScreen;
/*?}*/
/*? if <=1.18.2 {*/
/*import cool.muyucloud.pullup.adapter.client.ConditionManagerScreen;*/
/*?}*/
import cool.muyucloud.pullup.cloud.CloudApiModels;
import cool.muyucloud.pullup.cloud.CloudCommandSupport;
import cool.muyucloud.pullup.cloud.CloudImportState;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.network.PullupNetworking;
/*? if fabric && >1.18.2 {*/
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
/*?}*/
/*? if fabric && <=1.18.2 {*/
/*import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
*/
/*?}*/
/*? if forge || neoforge {*/
/*import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;*/
/*?}*/
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
/*? if <=1.18.2 {*/
/*import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
*/
/*?}*/
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientCommand {
    private static final SuggestionProvider<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > CONDITION_SETS = (context, builder) -> CommandSource.suggestMatching(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();
    private static final Logger LOGGER = Pullup.getLogger();

    public static void register(CommandDispatcher<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > dispatcher) {
        dispatcher.register(buildRoot("pullupclient"));
        dispatcher.register(buildRoot("pullup"));
        dispatcher.register(buildRoot("puc"));
    }

    /*? if forge {*/
    /*public static void registerForgeClientCommands(net.minecraftforge.client.event.RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*public static void registerNeoForgeClientCommands(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }*/
    /*?}*/

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > buildRoot(String rootName) {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*ServerCommandSource*/
            /*?}*/
            > root = literal(rootName);

        root.then(buildLoad());
        root.then(literal("enable").executes(ClientCommand::executeEnable));
        root.then(literal("disable").executes(ClientCommand::executeDisable));
        root.then(literal("grab").executes(ClientCommand::grabConditions));
        root.then(literal("enableserver").executes(ClientCommand::enableServer));
        root.then(literal("disableserver").executes(ClientCommand::disableServer));
        root.then(literal("status").executes(ClientCommand::status));
        /*? if >1.18.2 {*/
        root.then(literal("gui").executes(ClientCommand::manage));
        /*?}*/
        /*? if <=1.18.2 {*/
        /*root.then(literal("gui").executes(ClientCommand::manageLegacy));*/
        /*?}*/
        root.then(buildCloud());

        return root;
    }

    private static int executeEnable(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.enable", Formatting.GREEN));
        CONFIG.set("enable", true);
        return 1;
    }

    private static int executeDisable(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.disable", Formatting.RED));
        CONFIG.set("enable", false);
        return 1;
    }

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > buildLoad() {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*ServerCommandSource*/
            /*?}*/
            > conditionSet = literal("load");

        conditionSet.then(
            argument("setName", StringArgumentType.string()).suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        conditionSet.then(
            literal("default").executes(context -> loadDefault(context.getSource()))
        );

        return conditionSet;
    }

    private static int loadSet(
        String name,
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        source
    ) {
        if (!ConditionLoader.containsFile(name)) {
            sendError(source, styledTranslatable("command.pullup.client.load.specific.notExist", Formatting.RED, name));
            return 0;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.load.specific.loading", Formatting.GRAY, coloredLiteral(name, Formatting.YELLOW)));
        return ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(name)) ? 1 : 0;
    }

    private static int loadDefault(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        source
    ) {
        sendFeedback(source, styledTranslatable("command.pullup.client.load.default", Formatting.GRAY));
        return ConditionSetRuntime.saveEnabledSetsAndLoad(List.of("default")) ? 1 : 0;
    }

    private static int grabConditions(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*ServerCommandSource source = context.getSource();*/
        /*?}*/

        if (!CONFIG.getAsBool("loadServer")) {
            sendError(source, styledTranslatable("command.pullup.client.grab.enableLoadServer", Formatting.RED));
            return 0;
        }

        PullupNetworking.requestServerConditions();
        sendFeedback(source, styledTranslatable("command.pullup.client.grab.sent", Formatting.GREEN));
        return 1;
    }

    private static int enableServer(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.loadServer.enable", Formatting.GREEN));
        CONFIG.set("loadServer", true);
        return 1;
    }

    private static int disableServer(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.loadServer.disabled", Formatting.RED));
        CONFIG.set("loadServer", false);
        return 1;
    }

    private static int status(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*ServerCommandSource source = context.getSource();*/
        /*?}*/
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        List<String> enabledSets = ConditionSetRuntime.readConfiguredSetNames();

        MutableText enabledValue = CONFIG.getAsBool("enable")
            ? styledTranslatable("command.pullup.client.status.value.true", Formatting.GREEN)
            : styledTranslatable("command.pullup.client.status.value.false", Formatting.RED);
        MutableText loadServerValue = CONFIG.getAsBool("loadServer")
            ? styledTranslatable("command.pullup.client.status.value.true", Formatting.GREEN)
            : styledTranslatable("command.pullup.client.status.value.false", Formatting.RED);
        MutableText enabledSetsValue = enabledSets.isEmpty()
            ? styledTranslatable("command.pullup.client.status.value.none", Formatting.DARK_GRAY)
            : coloredLiteral(String.join(", ", enabledSets), Formatting.YELLOW);

        sendFeedback(source, styledTranslatable("command.pullup.client.status.enabled", Formatting.GRAY, enabledValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.loadServer", Formatting.GRAY, loadServerValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.enabledSets", Formatting.GRAY, enabledSetsValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.conditionCount", Formatting.GRAY, coloredLiteral(String.valueOf(Registry.CONDITIONS.getAll().size()), Formatting.AQUA)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.cloudUrl", Formatting.GRAY, coloredLiteral(CONFIG.getAsString("cloudBaseUrl"), Formatting.WHITE)));

        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudPackNone", Formatting.GRAY));
            return 1;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudCode", Formatting.GRAY, coloredLiteral(snapshot.getCode(), Formatting.GOLD)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedSet", Formatting.GRAY, coloredLiteral(snapshot.getSetName(), Formatting.YELLOW)));
        sendFeedback(source, styledTranslatable(
            "command.pullup.client.status.lastImportMode",
            Formatting.GRAY,
            snapshot.isTemporary()
                ? styledTranslatable("command.pullup.client.status.mode.temporary", Formatting.LIGHT_PURPLE)
                : styledTranslatable("command.pullup.client.status.mode.saved", Formatting.GREEN)
        ));
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedFile", Formatting.GRAY, coloredLiteral(snapshot.getLocalFile(), Formatting.WHITE)));
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackRequired", Formatting.RED, coloredLiteral(snapshot.getResourcePackMode(), Formatting.YELLOW)));
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackUrl", Formatting.GRAY, coloredLiteral(snapshot.getResourcePackUrl(), Formatting.AQUA)));
            }
        }
        return 1;
    }

    /*? if >1.18.2 {*/
    private static int manage(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        ConditionManagerScreen.requestOpen();
        return 1;
    }
    /*?}*/

    /*? if <=1.18.2 {*/
    private static int manageLegacy(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        ConditionManagerScreen.requestOpen();
        return 1;
    }
    /*?}*/

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > buildCloud() {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*ServerCommandSource*/
            /*?}*/
            > cloud = literal("cloud");

        cloud.then(literal("edit").executes(ClientCommand::cloudEdit));
        cloud.then(
            literal("import")
                .then(argument("code", StringArgumentType.word())
                    .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), false, context.getSource()))
                    .then(literal("temp")
                        .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), true, context.getSource()))))
        );
        cloud.then(literal("status").executes(ClientCommand::cloudStatus));

        return cloud;
    }

    private static int cloudEdit(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*ServerCommandSource source = context.getSource();*/
        /*?}*/
        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.creating", Formatting.GRAY));

        CompletableFuture.runAsync(() -> {
            try {
                CloudApiModels.SessionCreateResponse response = CloudCommandSupport.createEditSession();
                runOnClient(() -> {
                    sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.ready", Formatting.GREEN));
                    sendFeedback(source, buildClickableUrl(response.editUrl));
                    if (response.expiresAt != null && !response.expiresAt.isBlank()) {
                        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.expires", Formatting.GRAY, coloredLiteral(response.expiresAt, Formatting.YELLOW)));
                    }
                });
            } catch (Exception e) {
                LOGGER.warn("Failed to create PullUp cloud edit session.", e);
                runOnClient(() -> sendError(source, styledTranslatable("command.pullup.client.cloud.edit.failed", Formatting.RED, e.getMessage())));
            }
        });
        return 1;
    }

    private static int cloudImport(
        String code,
        boolean temporary,
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        source
    ) {
        sendFeedback(
            source,
            styledTranslatable(
                "command.pullup.client.cloud.import.start",
                Formatting.GRAY,
                coloredLiteral(code.toUpperCase(), Formatting.GOLD),
                coloredLiteral(String.valueOf(temporary), temporary ? Formatting.GREEN : Formatting.RED)
            )
        );

        CompletableFuture.runAsync(() -> {
            try {
                CloudCommandSupport.ImportResult result = CloudCommandSupport.importPack(code, temporary);
                runOnClient(() -> {
                    if (result.isTemporary()) {
                        sendFeedback(
                            source,
                            styledTranslatable(
                                "command.pullup.client.cloud.import.success.temp",
                                Formatting.GREEN,
                                coloredLiteral(result.getCode(), Formatting.GOLD),
                                coloredLiteral(result.getSetName(), Formatting.YELLOW)
                            )
                        );
                    } else {
                        sendFeedback(
                            source,
                            styledTranslatable(
                                "command.pullup.client.cloud.import.success.saved",
                                Formatting.GREEN,
                                coloredLiteral(result.getCode(), Formatting.GOLD),
                                coloredLiteral(result.getSetName(), Formatting.YELLOW)
                            )
                        );
                        if (result.getSaveResult() != null) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.savedFile", Formatting.GRAY, coloredLiteral(result.getSaveResult().getAbsolutePath().toString(), Formatting.WHITE)));
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.configuredLoad", Formatting.GRAY, coloredLiteral(result.getSaveResult().getConfigRelativeName(), Formatting.YELLOW)));
                        }
                    }
                    if (result.requiresCustomResourcePack()) {
                        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.needed", Formatting.YELLOW));
                        if (CloudApiModels.RESOURCE_PACK_MODE_BROWSER_LOCAL.equals(result.getResourcePackMode())) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.mode.local", Formatting.YELLOW));
                        } else if (!result.getResourcePackUrl().isBlank()) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.url", Formatting.GRAY, coloredLiteral(result.getResourcePackUrl(), Formatting.AQUA)));
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.warn("Failed to import PullUp cloud pack {}.", code, e);
                runOnClient(() -> sendError(source, styledTranslatable("command.pullup.client.cloud.import.failed", Formatting.RED, e.getMessage())));
            }
        });
        return 1;
    }

    private static int cloudStatus(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*ServerCommandSource source = context.getSource();*/
        /*?}*/
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.status.url", Formatting.GRAY, coloredLiteral(CONFIG.getAsString("cloudBaseUrl"), Formatting.WHITE)));
        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.status.none", Formatting.GRAY));
            return 1;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudCode", Formatting.GRAY, coloredLiteral(snapshot.getCode(), Formatting.GOLD)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedSet", Formatting.GRAY, coloredLiteral(snapshot.getSetName(), Formatting.YELLOW)));
        sendFeedback(source, styledTranslatable(
            "command.pullup.client.status.lastImportMode",
            Formatting.GRAY,
            snapshot.isTemporary()
                ? styledTranslatable("command.pullup.client.status.mode.temporary", Formatting.LIGHT_PURPLE)
                : styledTranslatable("command.pullup.client.status.mode.saved", Formatting.GREEN)
        ));
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedFile", Formatting.GRAY, coloredLiteral(snapshot.getLocalFile(), Formatting.WHITE)));
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackRequired", Formatting.RED, coloredLiteral(snapshot.getResourcePackMode(), Formatting.YELLOW)));
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackUrl", Formatting.GRAY, coloredLiteral(snapshot.getResourcePackUrl(), Formatting.AQUA)));
            }
        }
        return 1;
    }

    private static MutableText buildClickableUrl(String url) {
        return coloredLiteral(url, Formatting.AQUA)
            .formatted(Formatting.UNDERLINE)
            /*? if >=1.21.5 {*/
            .styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create(url))));
            /*?} else {*/
            .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
            /*?}*/
    }

    private static MutableText styledTranslatable(String key, Formatting color, Object... args) {
        /*? if <=1.18.2 {*/
        /*return new TranslatableText(key, args).formatted(color);*/
        /*?} else {*/
        return Text.translatable(key, args).formatted(color);
        /*?}*/
    }

    private static MutableText coloredLiteral(String value, Formatting color) {
        /*? if <=1.18.2 {*/
        /*return new LiteralText(value).formatted(color);*/
        /*?} else {*/
        return Text.literal(value).formatted(color);
        /*?}*/
    }

    private static void sendFeedback(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        source,
        MutableText text
    ) {
        /*? if fabric {*/
        source.sendFeedback(text);
        /*?} else {*/
        /*? if <=1.19.4 {*/
        /*source.sendFeedback(text, false);*/
        /*?} else {*/
        /*source.sendFeedback(() -> text, false);*/
        /*?}*/
        /*?}*/
    }

    private static void sendError(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        source,
        MutableText text
    ) {
        /*? if fabric {*/
        source.sendError(text);
        /*?} else {*/
        /*source.sendError(text);*/
        /*?}*/
    }

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        > literal(String name) {
        /*? if fabric {*/
        return ClientCommandManager.literal(name);
        /*?} else {*/
        /*return CommandManager.literal(name);*/
        /*?}*/
    }

    private static <T> com.mojang.brigadier.builder.RequiredArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*ServerCommandSource*/
        /*?}*/
        , T> argument(String name, com.mojang.brigadier.arguments.ArgumentType<T> type) {
        /*? if fabric {*/
        return ClientCommandManager.argument(name, type);
        /*?} else {*/
        /*return CommandManager.argument(name, type);*/
        /*?}*/
    }

    private static void runOnClient(Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }
}
/*?}*/
