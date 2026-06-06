package cool.muyucloud.pullup.util.command;

import com.mojang.brigadier.CommandDispatcher;
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
import cool.muyucloud.pullup.util.network.PullupNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
/*? if <=1.18.2 {*/
/*import net.minecraft.text.TranslatableText;
import net.minecraft.text.LiteralText;
*/
/*?}*/

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerCommand {
    private static final SuggestionProvider<ServerCommandSource> CONDITION_SETS = (context, builder) -> CommandSource.suggestMatching(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(buildRoot("pullupserver"));
        dispatcher.register(buildRoot("pus"));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildRoot(String rootName) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal(rootName)
            .requires(source -> source.hasPermissionLevel(2));

        root.then(buildLoad());
        root.then(buildCloud());
        root.then(CommandManager.literal("enablesend").executes(ServerCommand::enableSend));
        root.then(CommandManager.literal("disablesend").executes(ServerCommand::disableSend));
        root.then(CommandManager.literal("status").executes(ServerCommand::status));

        return root;
    }

    private static int enableSend(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = translatable("command.pullup.server.enableSend");
        sendFeedback(source, text, true);
        CONFIG.set("sendServer", true);
        return 1;
    }

    private static int disableSend(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = translatable("command.pullup.server.disableSend");
        sendFeedback(source, text, true);
        CONFIG.set("sendServer", false);
        return 1;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildLoad() {
        LiteralArgumentBuilder<ServerCommandSource> conditionSet = CommandManager.literal("load");

        conditionSet.then(
            CommandManager.argument("setName", StringArgumentType.string()).suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        conditionSet.then(
            CommandManager.literal("default").executes(context -> loadDefault(context.getSource()))
        );

        return conditionSet;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildCloud() {
        LiteralArgumentBuilder<ServerCommandSource> cloud = CommandManager.literal("cloud");

        cloud.then(
            CommandManager.literal("import")
                .then(CommandManager.argument("code", StringArgumentType.word())
                    .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), false, context.getSource()))
                    .then(CommandManager.literal("temp")
                        .executes(context -> cloudImport(StringArgumentType.getString(context, "code"), true, context.getSource()))))
        );
        cloud.then(CommandManager.literal("status").executes(ServerCommand::cloudStatus));

        return cloud;
    }

    private static int loadSet(String name, ServerCommandSource source) {
        if (!ConditionLoader.containsFile(name)) {
            sendFeedback(source, translatable("command.pullup.client.load.specific.notExist"), false);
            return 0;
        }

        MutableText text = translatable("command.pullup.client.load.specific.loading");
        sendFeedback(source, text, true);
        if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(name))) {
            return 0;
        }
        if (CONFIG.getAsBool("sendServer")) {
            try {
                PullupNetworking.sendClear(source.getPlayer());
                PullupNetworking.sendLoad(source.getPlayer());
            } catch (Exception ignored) {
                return 0;
            }
        }
        return 1;
    }

    private static int loadDefault(ServerCommandSource source) {
        MutableText text = translatable("command.pullup.client.load.default");
        sendFeedback(source, text, true);
        if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of("default"))) {
            return 0;
        }
        if (CONFIG.getAsBool("sendServer")) {
            try {
                PullupNetworking.sendClear(source.getPlayer());
                PullupNetworking.sendLoad(source.getPlayer());
            } catch (Exception ignored) {
                return 0;
            }
        }
        return 1;
    }

    private static int cloudImport(String code, boolean temporary, ServerCommandSource source) {
        sendFeedback(
            source,
            literal((temporary ? "Temporarily importing " : "Importing ") + "PullUp cloud pack " + code.toUpperCase() + " into the server..."),
            true
        );

        CompletableFuture.runAsync(() -> {
            try {
                CloudCommandSupport.ServerImportResult result = CloudCommandSupport.importPackToServer(code, temporary);
                source.getServer().execute(() -> {
                    if (result.isTemporary()) {
                        sendFeedback(source, literal("Temporarily imported cloud pack " + result.getCode() + " as server set " + result.getSetName() + "."), true);
                    } else {
                        sendFeedback(
                            source,
                            literal("Imported cloud pack " + result.getCode() + " as server set " + result.getSaveResult().getConfigRelativeName() + "."),
                            true
                        );
                        sendFeedback(source, literal("Saved file: " + result.getSaveResult().getAbsolutePath()), true);
                    }

                    if (CONFIG.getAsBool("sendServer")) {
                        source.getServer().getPlayerManager().getPlayerList().forEach(player -> {
                            PullupNetworking.sendClear(player);
                            PullupNetworking.sendLoad(player);
                        });
                        sendFeedback(source, literal("Broadcasted the updated server conditions to online players."), true);
                    }

                    if (result.requiresCustomResourcePack()) {
                        sendFeedback(source, literal("This pack needs a custom resource pack to play custom sounds."), true);
                        if (CloudApiModels.RESOURCE_PACK_MODE_BROWSER_LOCAL.equals(result.getResourcePackMode())) {
                            sendFeedback(source, literal("Resource pack mode: browser-local. Players need to download it from the editor result page."), true);
                        } else if (!result.getResourcePackUrl().isBlank()) {
                            sendFeedback(source, literal("Resource pack URL: " + result.getResourcePackUrl()), true);
                        }
                    }
                });
            } catch (Exception e) {
                source.getServer().execute(() -> sendFeedback(source, literal("PullUp cloud server import failed: " + e.getMessage()), false));
            }
        });
        return 1;
    }

    private static int cloudStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        sendFeedback(source, literal("PullUp cloud base URL: " + CONFIG.getAsString("cloudBaseUrl")), true);
        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, literal("No cloud pack has been imported on this server in this session."), true);
            return 1;
        }

        sendFeedback(source, literal("Last imported cloud code: " + snapshot.getCode()), true);
        sendFeedback(source, literal("Last imported set: " + snapshot.getSetName()), true);
        sendFeedback(source, literal("Last import mode: " + (snapshot.isTemporary() ? "temporary-memory-only" : "saved-and-configured")), true);
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, literal("Last imported file: " + snapshot.getLocalFile()), true);
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, literal("Custom resource pack required (" + snapshot.getResourcePackMode() + ")."), true);
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, literal("Resource pack URL: " + snapshot.getResourcePackUrl()), true);
            }
        }
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        CloudImportState.Snapshot snapshot = CloudCommandSupport.readSnapshot();
        List<String> enabledSets = ConditionSetRuntime.readConfiguredSetNames();

        sendFeedback(source, literal("Send server conditions: " + CONFIG.getAsBool("sendServer")), true);
        sendFeedback(source, literal("Enabled sets: " + (enabledSets.isEmpty() ? "(none)" : String.join(", ", enabledSets))), true);
        sendFeedback(source, literal("Loaded condition count: " + Registry.CONDITIONS.getAll().size()), true);
        sendFeedback(source, literal("Cloud base URL: " + CONFIG.getAsString("cloudBaseUrl")), true);

        if (!snapshot.hasImportedPack()) {
            sendFeedback(source, literal("Last imported cloud pack: none"), true);
            return 1;
        }

        sendFeedback(source, literal("Last imported cloud code: " + snapshot.getCode()), true);
        sendFeedback(source, literal("Last imported set: " + snapshot.getSetName()), true);
        sendFeedback(source, literal("Last import mode: " + (snapshot.isTemporary() ? "temporary-memory-only" : "saved-and-configured")), true);
        if (!snapshot.getLocalFile().isBlank()) {
            sendFeedback(source, literal("Last imported file: " + snapshot.getLocalFile()), true);
        }
        if (snapshot.requiresCustomResourcePack()) {
            sendFeedback(source, literal("Custom resource pack required (" + snapshot.getResourcePackMode() + ")."), true);
            if (!snapshot.getResourcePackUrl().isBlank()) {
                sendFeedback(source, literal("Resource pack URL: " + snapshot.getResourcePackUrl()), true);
            }
        }
        return 1;
    }

    private static MutableText translatable(String key, Object... args) {
        /*? if <=1.18.2 {*/
        /*return new TranslatableText(key, args);*/
        /*?} else {*/
        return Text.translatable(key, args);
        /*?}*/
    }

    private static MutableText literal(String value) {
        /*? if <=1.18.2 {*/
        /*return new LiteralText(value);*/
        /*?} else {*/
        return Text.literal(value);
        /*?}*/
    }

    private static void sendFeedback(ServerCommandSource source, MutableText text, boolean broadcastToOps) {
        /*? if <=1.18.2 {*/
        /*source.sendFeedback(text, broadcastToOps);*/
        /*?} else {*/
        source.sendFeedback(() -> text, broadcastToOps);
        /*?}*/
    }
}
