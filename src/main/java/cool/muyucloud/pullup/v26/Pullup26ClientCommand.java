package cool.muyucloud.pullup.v26;

/*? if >=26.1 && (fabric || neoforge) {*/
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.adapter.client.ConditionManagerScreen;
import cool.muyucloud.pullup.cloud.CloudApiModels;
import cool.muyucloud.pullup.cloud.CloudCommandSupport;
import cool.muyucloud.pullup.cloud.CloudImportState;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
/*? if fabric {*/
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
/*?}*/
/*? if neoforge {*/
/*import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;*/
/*?}*/
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Pullup26ClientCommand {
    private static final SuggestionProvider<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > CONDITION_SETS =
        (context, builder) -> SharedSuggestionProvider.suggest(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();

    private Pullup26ClientCommand() {}

    public static void register(CommandDispatcher<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > dispatcher) {
        dispatcher.register(buildRoot("pullupclient"));
        dispatcher.register(buildRoot("pullup"));
        dispatcher.register(buildRoot("puc"));
    }

    /*? if neoforge {*/
    /*public static void registerNeoForgeClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }*/
    /*?}*/

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > buildRoot(String rootName) {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*CommandSourceStack*/
            /*?}*/
            > root = literal(rootName);

        root.then(buildLoad());
        root.then(literal("enable").executes(Pullup26ClientCommand::enable));
        root.then(literal("disable").executes(Pullup26ClientCommand::disable));
        root.then(literal("grab").executes(Pullup26ClientCommand::grab));
        root.then(literal("enableserver").executes(Pullup26ClientCommand::enableServer));
        root.then(literal("disableserver").executes(Pullup26ClientCommand::disableServer));
        root.then(literal("status").executes(Pullup26ClientCommand::status));
        root.then(literal("gui").executes(Pullup26ClientCommand::manage));
        root.then(buildCloud());

        return root;
    }

    private static int enable(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.enable", ChatFormatting.GREEN));
        CONFIG.set("enable", true);
        return 1;
    }

    private static int disable(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.disable", ChatFormatting.RED));
        CONFIG.set("enable", false);
        return 1;
    }

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > buildLoad() {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*CommandSourceStack*/
            /*?}*/
            > load = literal("load");

        load.then(
            argument("setName", StringArgumentType.string())
                .suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        load.then(literal("default").executes(context -> loadDefault(context.getSource())));

        return load;
    }

    private static int loadSet(
        String name,
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        source
    ) {
        if (!ConditionLoader.containsFile(name)) {
            sendError(source, Component.translatable("command.pullup.client.load.specific.notExist", name));
            return 0;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.load.specific.loading", ChatFormatting.GRAY, coloredLiteral(name, ChatFormatting.YELLOW)));
        return ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(name)) ? 1 : 0;
    }

    private static int loadDefault(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        source
    ) {
        sendFeedback(source, styledTranslatable("command.pullup.client.load.default", ChatFormatting.GRAY));
        return ConditionSetRuntime.saveEnabledSetsAndLoad(List.of("default")) ? 1 : 0;
    }

    private static int grab(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*CommandSourceStack source = context.getSource();*/
        /*?}*/
        if (!CONFIG.getAsBool("loadServer")) {
            sendError(source, styledTranslatable("command.pullup.client.grab.enableLoadServer", ChatFormatting.RED));
            return 0;
        }

        /*? if fabric {*/
        Pullup26FabricNetworking.requestServerConditions();
        /*?} else {*/
        /*Pullup26NeoForgeNetworking.requestServerConditions();*/
        /*?}*/
        sendFeedback(source, styledTranslatable("command.pullup.client.grab.sent", ChatFormatting.GREEN));
        return 1;
    }

    private static int enableServer(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.loadServer.enable", ChatFormatting.GREEN));
        CONFIG.set("loadServer", true);
        return 1;
    }

    private static int disableServer(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        sendFeedback(context.getSource(), styledTranslatable("command.pullup.client.loadServer.disabled", ChatFormatting.RED));
        CONFIG.set("loadServer", false);
        return 1;
    }

    private static int status(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*CommandSourceStack source = context.getSource();*/
        /*?}*/
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        List<String> enabledSets = ConditionSetRuntime.readConfiguredSetNames();

        MutableComponent enabledValue = CONFIG.getAsBool("enable")
            ? styledTranslatable("command.pullup.client.status.value.true", ChatFormatting.GREEN)
            : styledTranslatable("command.pullup.client.status.value.false", ChatFormatting.RED);
        MutableComponent loadServerValue = CONFIG.getAsBool("loadServer")
            ? styledTranslatable("command.pullup.client.status.value.true", ChatFormatting.GREEN)
            : styledTranslatable("command.pullup.client.status.value.false", ChatFormatting.RED);
        MutableComponent enabledSetsValue = enabledSets.isEmpty()
            ? styledTranslatable("command.pullup.client.status.value.none", ChatFormatting.DARK_GRAY)
            : coloredLiteral(String.join(", ", enabledSets), ChatFormatting.YELLOW);

        sendFeedback(source, styledTranslatable("command.pullup.client.status.enabled", ChatFormatting.GRAY, enabledValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.loadServer", ChatFormatting.GRAY, loadServerValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.enabledSets", ChatFormatting.GRAY, enabledSetsValue));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.conditionCount", ChatFormatting.GRAY, coloredLiteral(String.valueOf(Registry.CONDITIONS.getAll().size()), ChatFormatting.AQUA)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.cloudUrl", ChatFormatting.GRAY, coloredLiteral(CONFIG.getAsString("cloudBaseUrl"), ChatFormatting.WHITE)));

        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudPackNone", ChatFormatting.GRAY));
            return 1;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudCode", ChatFormatting.GRAY, coloredLiteral(snapshot.getCode(), ChatFormatting.GOLD)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedSet", ChatFormatting.GRAY, coloredLiteral(snapshot.getSetName(), ChatFormatting.YELLOW)));
        sendFeedback(source, styledTranslatable(
            "command.pullup.client.status.lastImportMode",
            ChatFormatting.GRAY,
            snapshot.isTemporary()
                ? styledTranslatable("command.pullup.client.status.mode.temporary", ChatFormatting.LIGHT_PURPLE)
                : styledTranslatable("command.pullup.client.status.mode.saved", ChatFormatting.GREEN)
        ));
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedFile", ChatFormatting.GRAY, coloredLiteral(snapshot.getLocalFile(), ChatFormatting.WHITE)));
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackRequired", ChatFormatting.RED, coloredLiteral(snapshot.getResourcePackMode(), ChatFormatting.YELLOW)));
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackUrl", ChatFormatting.GRAY, coloredLiteral(snapshot.getResourcePackUrl(), ChatFormatting.AQUA)));
            }
        }
        return 1;
    }

    private static int manage(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        ConditionManagerScreen.requestOpen();
        return 1;
    }

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > buildCloud() {
        LiteralArgumentBuilder<
            /*? if fabric {*/
            FabricClientCommandSource
            /*?} else {*/
            /*CommandSourceStack*/
            /*?}*/
            > cloud = literal("cloud");

        cloud.then(literal("edit").executes(Pullup26ClientCommand::cloudEdit));
        cloud.then(
            literal("import")
                .then(argument("code", StringArgumentType.word())
                    .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), false, context.getSource()))
                    .then(literal("temp")
                        .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), true, context.getSource()))))
        );
        cloud.then(literal("status").executes(Pullup26ClientCommand::cloudStatus));

        return cloud;
    }

    private static int cloudEdit(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*CommandSourceStack source = context.getSource();*/
        /*?}*/
        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.creating", ChatFormatting.GRAY));

        CompletableFuture.runAsync(() -> {
            try {
                CloudApiModels.SessionCreateResponse response = CloudCommandSupport.createEditSession();
                runOnClient(() -> {
                    sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.ready", ChatFormatting.GREEN));
                    sendFeedback(source, buildClickableUrl(response.editUrl));
                    if (response.expiresAt != null && !response.expiresAt.isBlank()) {
                        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.edit.expires", ChatFormatting.GRAY, coloredLiteral(response.expiresAt, ChatFormatting.YELLOW)));
                    }
                });
            } catch (Exception e) {
                runOnClient(() -> sendError(source, styledTranslatable("command.pullup.client.cloud.edit.failed", ChatFormatting.RED, e.getMessage())));
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
        /*CommandSourceStack*/
        /*?}*/
        source
    ) {
        sendFeedback(
            source,
            styledTranslatable(
                "command.pullup.client.cloud.import.start",
                ChatFormatting.GRAY,
                coloredLiteral(code.toUpperCase(), ChatFormatting.GOLD),
                coloredLiteral(String.valueOf(temporary), temporary ? ChatFormatting.GREEN : ChatFormatting.RED)
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
                                ChatFormatting.GREEN,
                                coloredLiteral(result.getCode(), ChatFormatting.GOLD),
                                coloredLiteral(result.getSetName(), ChatFormatting.YELLOW)
                            )
                        );
                    } else {
                        sendFeedback(
                            source,
                            styledTranslatable(
                                "command.pullup.client.cloud.import.success.saved",
                                ChatFormatting.GREEN,
                                coloredLiteral(result.getCode(), ChatFormatting.GOLD),
                                coloredLiteral(result.getSetName(), ChatFormatting.YELLOW)
                            )
                        );
                        if (result.getSaveResult() != null) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.savedFile", ChatFormatting.GRAY, coloredLiteral(result.getSaveResult().getAbsolutePath().toString(), ChatFormatting.WHITE)));
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.configuredLoad", ChatFormatting.GRAY, coloredLiteral(result.getSaveResult().getConfigRelativeName(), ChatFormatting.YELLOW)));
                        }
                    }
                    if (result.requiresCustomResourcePack()) {
                        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.needed", ChatFormatting.YELLOW));
                        if (CloudApiModels.RESOURCE_PACK_MODE_BROWSER_LOCAL.equals(result.getResourcePackMode())) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.mode.local", ChatFormatting.YELLOW));
                        } else if (!result.getResourcePackUrl().isBlank()) {
                            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.import.rp.url", ChatFormatting.GRAY, coloredLiteral(result.getResourcePackUrl(), ChatFormatting.AQUA)));
                        }
                    }
                });
            } catch (Exception e) {
                runOnClient(() -> sendError(source, styledTranslatable("command.pullup.client.cloud.import.failed", ChatFormatting.RED, e.getMessage())));
            }
        });
        return 1;
    }

    private static int cloudStatus(CommandContext<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > context) {
        /*? if fabric {*/
        FabricClientCommandSource source = context.getSource();
        /*?} else {*/
        /*CommandSourceStack source = context.getSource();*/
        /*?}*/
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        sendFeedback(source, styledTranslatable("command.pullup.client.cloud.status.url", ChatFormatting.GRAY, coloredLiteral(CONFIG.getAsString("cloudBaseUrl"), ChatFormatting.WHITE)));
        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.cloud.status.none", ChatFormatting.GRAY));
            return 1;
        }

        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedCloudCode", ChatFormatting.GRAY, coloredLiteral(snapshot.getCode(), ChatFormatting.GOLD)));
        sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedSet", ChatFormatting.GRAY, coloredLiteral(snapshot.getSetName(), ChatFormatting.YELLOW)));
        sendFeedback(source, styledTranslatable(
            "command.pullup.client.status.lastImportMode",
            ChatFormatting.GRAY,
            snapshot.isTemporary()
                ? styledTranslatable("command.pullup.client.status.mode.temporary", ChatFormatting.LIGHT_PURPLE)
                : styledTranslatable("command.pullup.client.status.mode.saved", ChatFormatting.GREEN)
        ));
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.lastImportedFile", ChatFormatting.GRAY, coloredLiteral(snapshot.getLocalFile(), ChatFormatting.WHITE)));
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackRequired", ChatFormatting.RED, coloredLiteral(snapshot.getResourcePackMode(), ChatFormatting.YELLOW)));
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, styledTranslatable("command.pullup.client.status.resourcePackUrl", ChatFormatting.GRAY, coloredLiteral(snapshot.getResourcePackUrl(), ChatFormatting.AQUA)));
            }
        }
        return 1;
    }

    private static MutableComponent buildClickableUrl(String url) {
        return coloredLiteral(url, ChatFormatting.AQUA).withStyle(style -> style
            .withUnderlined(true)
            .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
        );
    }

    private static MutableComponent styledTranslatable(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    private static MutableComponent coloredLiteral(String value, ChatFormatting color) {
        return Component.literal(value).withStyle(color);
    }

    private static void sendFeedback(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        source,
        Component text
    ) {
        /*? if fabric {*/
        source.sendFeedback(text);
        /*?} else {*/
        /*source.sendSuccess(() -> text, false);*/
        /*?}*/
    }

    private static void sendError(
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        source,
        Component text
    ) {
        /*? if fabric {*/
        source.sendError(text);
        /*?} else {*/
        /*source.sendFailure(text);*/
        /*?}*/
    }

    private static LiteralArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        > literal(String name) {
        /*? if fabric {*/
        return ClientCommands.literal(name);
        /*?} else {*/
        /*return Commands.literal(name);*/
        /*?}*/
    }

    private static <T> com.mojang.brigadier.builder.RequiredArgumentBuilder<
        /*? if fabric {*/
        FabricClientCommandSource
        /*?} else {*/
        /*CommandSourceStack*/
        /*?}*/
        , T> argument(String name, com.mojang.brigadier.arguments.ArgumentType<T> type) {
        /*? if fabric {*/
        return ClientCommands.argument(name, type);
        /*?} else {*/
        /*return Commands.argument(name, type);*/
        /*?}*/
    }

    private static void runOnClient(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }
}
/*?} else {*/
public final class Pullup26ClientCommand {
    private Pullup26ClientCommand() {}

    public static void register(Object dispatcher) {
    }
}
/*?}*/
