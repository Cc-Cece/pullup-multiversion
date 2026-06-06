package cool.muyucloud.pullup.v26;

/*? if >=26.1 {*/
/*import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.cloud.CloudApiModels;
import cool.muyucloud.pullup.cloud.CloudCommandSupport;
import cool.muyucloud.pullup.cloud.CloudImportState;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Pullup26ServerCommand {
    private static final SuggestionProvider<CommandSourceStack> CONDITION_SETS =
        (context, builder) -> SharedSuggestionProvider.suggest(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();

    private Pullup26ServerCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildRoot("pullupserver"));
        dispatcher.register(buildRoot("pus"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot(String rootName) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(rootName)
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        root.then(buildLoad());
        root.then(buildCloud());
        root.then(Commands.literal("enablesend").executes(Pullup26ServerCommand::enableSend));
        root.then(Commands.literal("disablesend").executes(Pullup26ServerCommand::disableSend));
        root.then(Commands.literal("status").executes(Pullup26ServerCommand::status));

        return root;
    }

    private static int enableSend(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("command.pullup.server.enableSend"), true);
        CONFIG.set("sendServer", true);
        return 1;
    }

    private static int disableSend(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("command.pullup.server.disableSend"), true);
        CONFIG.set("sendServer", false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildLoad() {
        LiteralArgumentBuilder<CommandSourceStack> load = Commands.literal("load");

        load.then(
            Commands.argument("setName", StringArgumentType.string())
                .suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        load.then(Commands.literal("default").executes(context -> loadDefault(context.getSource())));

        return load;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCloud() {
        LiteralArgumentBuilder<CommandSourceStack> cloud = Commands.literal("cloud");

        cloud.then(
            Commands.literal("import")
                .then(Commands.argument("code", StringArgumentType.word())
                    .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), false, context.getSource()))
                    .then(Commands.literal("temp")
                        .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), true, context.getSource()))))
        );
        cloud.then(Commands.literal("status").executes(Pullup26ServerCommand::cloudStatus));

        return cloud;
    }

    private static int loadSet(String name, CommandSourceStack source) {
        if (!ConditionLoader.containsFile(name)) {
            source.sendFailure(Component.translatable("command.pullup.client.load.specific.notExist", name));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.pullup.client.load.specific.loading", name), true);
        if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(name))) {
            return 0;
        }
        if (CONFIG.getAsBool("sendServer")) {
            Pullup26ServerSync.broadcastConfiguredConditions(source.getServer());
        }
        return 1;
    }

    private static int loadDefault(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("command.pullup.client.load.default"), true);
        if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of("default"))) {
            return 0;
        }
        if (CONFIG.getAsBool("sendServer")) {
            Pullup26ServerSync.broadcastConfiguredConditions(source.getServer());
        }
        return 1;
    }

    private static int cloudImport(String code, boolean temporary, CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.literal((temporary ? "Temporarily importing " : "Importing ") + "PullUp cloud pack " + code.toUpperCase() + " into the server..."),
            true
        );

        CompletableFuture.runAsync(() -> {
            try {
                CloudCommandSupport.ServerImportResult result = CloudCommandSupport.importPackToServer(code, temporary);
                source.getServer().execute(() -> {
                    if (result.isTemporary()) {
                        source.sendSuccess(() -> Component.literal("Temporarily imported cloud pack " + result.getCode() + " as server set " + result.getSetName() + "."), true);
                    } else {
                        source.sendSuccess(
                            () -> Component.literal("Imported cloud pack " + result.getCode() + " as server set " + result.getSaveResult().getConfigRelativeName() + "."),
                            true
                        );
                        source.sendSuccess(() -> Component.literal("Saved file: " + result.getSaveResult().getAbsolutePath()), true);
                    }

                    if (CONFIG.getAsBool("sendServer")) {
                        Pullup26ServerSync.broadcastConfiguredConditions(source.getServer());
                        source.sendSuccess(() -> Component.literal("Broadcasted the updated server conditions to online players."), true);
                    }

                    if (result.requiresCustomResourcePack()) {
                        source.sendSuccess(() -> Component.literal("This pack needs a custom resource pack to play custom sounds."), true);
                        if (CloudApiModels.RESOURCE_PACK_MODE_BROWSER_LOCAL.equals(result.getResourcePackMode())) {
                            source.sendSuccess(() -> Component.literal("Resource pack mode: browser-local. Players need to download it from the editor result page."), true);
                        } else if (!result.getResourcePackUrl().isBlank()) {
                            source.sendSuccess(() -> Component.literal("Resource pack URL: " + result.getResourcePackUrl()), true);
                        }
                    }
                });
            } catch (Exception e) {
                source.getServer().execute(() -> source.sendFailure(Component.literal("PullUp cloud server import failed: " + e.getMessage())));
            }
        });
        return 1;
    }

    private static int cloudStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        source.sendSuccess(() -> Component.literal("PullUp cloud base URL: " + CONFIG.getAsString("cloudBaseUrl")), true);
        if (!snapshot.hasImportedPack()) {
            source.sendSuccess(() -> Component.literal("No cloud pack has been imported on this server in this session."), true);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Last imported cloud code: " + snapshot.getCode()), true);
        source.sendSuccess(() -> Component.literal("Last imported set: " + snapshot.getSetName()), true);
        source.sendSuccess(() -> Component.literal("Last import mode: " + (snapshot.isTemporary() ? "temporary-memory-only" : "saved-and-configured")), true);
        if (!snapshot.getLocalFile().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last imported file: " + snapshot.getLocalFile()), true);
        }
        if (snapshot.requiresCustomResourcePack()) {
            source.sendSuccess(() -> Component.literal("Custom resource pack required (" + snapshot.getResourcePackMode() + ")."), true);
            if (!snapshot.getResourcePackUrl().isBlank()) {
                source.sendSuccess(() -> Component.literal("Resource pack URL: " + snapshot.getResourcePackUrl()), true);
            }
        }
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        List<String> enabledSets = ConditionSetRuntime.readConfiguredSetNames();

        source.sendSuccess(() -> Component.literal("Send server conditions: " + CONFIG.getAsBool("sendServer")), true);
        source.sendSuccess(() -> Component.literal("Enabled sets: " + (enabledSets.isEmpty() ? "(none)" : String.join(", ", enabledSets))), true);
        source.sendSuccess(() -> Component.literal("Loaded condition count: " + Registry.CONDITIONS.getAll().size()), true);
        source.sendSuccess(() -> Component.literal("Cloud base URL: " + CONFIG.getAsString("cloudBaseUrl")), true);

        if (!snapshot.hasImportedPack()) {
            source.sendSuccess(() -> Component.literal("Last imported cloud pack: none"), true);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("Last imported cloud code: " + snapshot.getCode()), true);
        source.sendSuccess(() -> Component.literal("Last imported set: " + snapshot.getSetName()), true);
        source.sendSuccess(() -> Component.literal("Last import mode: " + (snapshot.isTemporary() ? "temporary-memory-only" : "saved-and-configured")), true);
        if (!snapshot.getLocalFile().isBlank()) {
            source.sendSuccess(() -> Component.literal("Last imported file: " + snapshot.getLocalFile()), true);
        }
        if (snapshot.requiresCustomResourcePack()) {
            source.sendSuccess(() -> Component.literal("Custom resource pack required (" + snapshot.getResourcePackMode() + ")."), true);
            if (!snapshot.getResourcePackUrl().isBlank()) {
                source.sendSuccess(() -> Component.literal("Resource pack URL: " + snapshot.getResourcePackUrl()), true);
            }
        }
        return 1;
    }
}*/
/*?} else {*/
public final class Pullup26ServerCommand {
    private Pullup26ServerCommand() {}

    public static void register(Object dispatcher) {
    }
}
/*?}*/
