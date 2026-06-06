package cool.muyucloud.pullup.adapter.client;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.ConditionSetRuntime;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/*? if >=26.1 {*/
/*import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
*/
/*?} else {*/
import net.minecraft.client.MinecraftClient;
/*? if <=1.18.2 {*/
/*import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
*/
/*?} else {*/
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
/*?}*/
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
/*? if <=1.18.2 {*/
/*import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
*/
/*?}*/
import net.minecraft.util.Formatting;
/*?}*/

public final class ConditionManagerScreen
/*? if >=26.1 {*/
/*extends Screen*/
/*?} else {*/
extends Screen
/*?}*/
{
    private static final Config CONFIG = Pullup.getConfig();
    private static final int LIST_TOP = 66;
    private static final int LIST_ROW_STEP = 24;
    private static final int BACKGROUND_OVERLAY_COLOR = 0xA0101018;
    // Opening from chat can be overridden by the chat screen closing in the same tick.
    private static boolean pendingOpen;

    private final
    /*? if >=26.1 {*/
    /*Screen*/
    /*?} else {*/
    Screen
    /*?}*/
    parent;
    private final LinkedHashSet<String> enabledSets;
    private final int requestedPage;
    private final ArrayList<String> availableSets = new ArrayList<>();
    private boolean alarmEnabled;
    private boolean loadServer;

    private ConditionManagerScreen(
        /*? if >=26.1 {*/
        /*Screen*/
        /*?} else {*/
        Screen
        /*?}*/
        parent,
        Collection<String> enabledSets,
        int requestedPage,
        boolean alarmEnabled,
        boolean loadServer
    ) {
        super(
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.title")*/
            /*?} else {*/
            translatable("gui.pullup.title")
            /*?}*/
        );
        this.parent = parent;
        this.enabledSets = new LinkedHashSet<>(enabledSets);
        this.requestedPage = Math.max(0, requestedPage);
        this.alarmEnabled = alarmEnabled;
        this.loadServer = loadServer;
        this.refreshAvailableSets();
    }

    public static void requestOpen() {
        pendingOpen = true;
    }

    public static void openIfRequested() {
        if (!pendingOpen) {
            return;
        }
        pendingOpen = false;
        open();
    }

    public static void open() {
        /*? if >=26.1 {*/
        /*Minecraft client = Minecraft.getInstance();
        client.setScreen(new ConditionManagerScreen(client.screen, ConditionSetRuntime.readConfiguredSetNames(), 0, CONFIG.getAsBool("enable"), CONFIG.getAsBool("loadServer")));*/
        /*?} else {*/
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new ConditionManagerScreen(client.currentScreen, ConditionSetRuntime.readConfiguredSetNames(), 0, CONFIG.getAsBool("enable"), CONFIG.getAsBool("loadServer")));
        /*?}*/
    }

    @Override
    protected void init() {
        super.init();
        /*? if >=26.1 {*/
        /*this.clearWidgets();*/
        /*?} else {*/
        this.clearChildren();
        /*?}*/

        int pageSize = this.getPageSize();
        int pageCount = Math.max(1, (int) Math.ceil(this.availableSets.size() / (double) pageSize));
        int page = Math.min(this.requestedPage, pageCount - 1);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, this.availableSets.size());

        int buttonWidth = Math.min(320, this.width - 40);
        int x = (this.width - buttonWidth) / 2;

        int colWidth = (buttonWidth - 16) / 3;
        this.addButton(
            x,
            40,
            colWidth,
            20,
            this.toggleLabel("gui.pullup.toggle_alarm", this.alarmEnabled),
            () -> this.toggleAlarm(page)
        );
        this.addButton(
            x + colWidth + 8,
            40,
            colWidth,
            20,
            this.toggleLabel("gui.pullup.toggle_sync", this.loadServer),
            () -> this.toggleLoadServer(page)
        );
        this.addButton(
            x + (colWidth + 8) * 2,
            40,
            colWidth,
            20,
            this.cloudButtonLabel(),
            () -> {
                /*? if >=26.1 {*/
                /*Minecraft.getInstance().setScreen(null);*/
                /*?} else {*/
                MinecraftClient.getInstance().setScreen(null);
                /*?}*/
                startCloudEdit();
            }
        );

        int y = LIST_TOP;
        for (int index = start; index < end; index += 1) {
            String setName = this.availableSets.get(index);
            boolean enabled = this.enabledSets.contains(setName);
            this.addButton(
                x,
                y,
                buttonWidth,
                20,
                this.buttonLabel(setName, enabled),
                () -> this.toggleSet(setName, page)
            );
            y += LIST_ROW_STEP;
        }

        int smallWidth = Math.min(120, Math.max(90, (buttonWidth - 10) / 2));
        this.addButton(
            x,
            this.height - 80,
            smallWidth,
            20,
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.previous")*/
            /*?} else {*/
            translatable("gui.pullup.previous")
            /*?}*/,
            () -> this.reopen(page - 1)
        );
        this.addButton(
            x + buttonWidth - smallWidth,
            this.height - 80,
            smallWidth,
            20,
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.next")*/
            /*?} else {*/
            translatable("gui.pullup.next")
            /*?}*/,
            () -> this.reopen(page + 1)
        );

        int lowerButtonWidth = Math.max(100, (buttonWidth - 20) / 3);
        this.addButton(
            x,
            this.height - 50,
            lowerButtonWidth,
            20,
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.disable_all")*/
            /*?} else {*/
            translatable("gui.pullup.disable_all")
            /*?}*/,
            () -> this.disableAll(page)
        );
        this.addButton(
            x + lowerButtonWidth + 10,
            this.height - 50,
            lowerButtonWidth,
            20,
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.save_apply")*/
            /*?} else {*/
            translatable("gui.pullup.save_apply")
            /*?}*/,
            this::saveAndApply
        );
        this.addButton(
            x + (lowerButtonWidth + 10) * 2,
            this.height - 50,
            lowerButtonWidth,
            20,
            /*? if >=26.1 {*/
            /*Component.translatable("gui.pullup.cancel")*/
            /*?} else {*/
            translatable("gui.pullup.cancel")
            /*?}*/,
            this::closeToParent
        );
    }

    private int getPageSize() {
        int maxRowY = this.height - 130;
        return Math.max(1, ((maxRowY - LIST_TOP) / LIST_ROW_STEP) + 1);
    }

    /*? if >=26.1 {*/
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, BACKGROUND_OVERLAY_COLOR);
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        guiGraphics.text(this.font, Component.translatable("gui.pullup.enabled_sets_count", this.enabledSets.size()), 20, this.height - 106, 0xB0B0B0, false);
    }*/
    /*?} else {*/
    /*? if <=1.18.2 {*/
    /*@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fillGradient(matrices, 0, 0, this.width, this.height, BACKGROUND_OVERLAY_COLOR, BACKGROUND_OVERLAY_COLOR);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        drawTextWithShadow(matrices, this.textRenderer, translatable("gui.pullup.enabled_sets_count", this.enabledSets.size()), 20, this.height - 106, 0xB0B0B0);
    }*/
    /*?} else {*/
    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, BACKGROUND_OVERLAY_COLOR);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        drawContext.drawTextWithShadow(this.textRenderer, translatable("gui.pullup.enabled_sets_count", this.enabledSets.size()), 20, this.height - 106, 0xB0B0B0);
    }
    /*?}*/
    /*?}*/

    /*? if >=26.1 {*/
    /*@Override
    public void onClose() {
        this.closeToParent();
    }*/
    /*?} else {*/
    @Override
    public void close() {
        this.closeToParent();
    }
    /*?}*/

    private void refreshAvailableSets() {
        this.availableSets.clear();
        this.availableSets.addAll(ConditionSetRuntime.listAvailableSetNames());
        for (String enabledSet : this.enabledSets) {
            if (!this.availableSets.contains(enabledSet)) {
                this.availableSets.add(enabledSet);
            }
        }
    }

    private void toggleSet(String setName, int page) {
        LinkedHashSet<String> next = new LinkedHashSet<>(this.enabledSets);
        if (!next.add(setName)) {
            next.remove(setName);
        }
        this.reopen(next, page);
    }

    private void disableAll(int page) {
        this.reopen(new LinkedHashSet<>(), page);
    }

    private void saveAndApply() {
        CONFIG.set("enable", this.alarmEnabled);
        CONFIG.set("loadServer", this.loadServer);
        CONFIG.save();
        ConditionSetRuntime.saveEnabledSetsAndLoad(this.enabledSets);
        this.closeToParent();
    }

    private void reopen(int page) {
        this.reopen(this.enabledSets, page);
    }

    private void reopen(Collection<String> enabledSets, int page) {
        int nextPage = Math.max(0, page);
        /*? if >=26.1 {*/
        /*Minecraft.getInstance().setScreen(new ConditionManagerScreen(this.parent, enabledSets, nextPage, this.alarmEnabled, this.loadServer));*/
        /*?} else {*/
        MinecraftClient.getInstance().setScreen(new ConditionManagerScreen(this.parent, enabledSets, nextPage, this.alarmEnabled, this.loadServer));
        /*?}*/
    }

    private void closeToParent() {
        /*? if >=26.1 {*/
        /*Minecraft.getInstance().setScreen(this.parent);*/
        /*?} else {*/
        MinecraftClient.getInstance().setScreen(this.parent);
        /*?}*/
    }

    private void toggleAlarm(int page) {
        this.alarmEnabled = !this.alarmEnabled;
        this.reopen(this.enabledSets, page);
    }

    private void toggleLoadServer(int page) {
        this.loadServer = !this.loadServer;
        this.reopen(this.enabledSets, page);
    }

    private static void sendPlayerMessage(
        /*? if >=26.1 {*/
        /*Component*/
        /*?} else {*/
        Text
        /*?}*/
        message
    ) {
        /*? if >=26.1 {*/
        /*Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(message);
        }*/
        /*?} else {*/
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(message, false);
        }
        /*?}*/
    }

    private static void startCloudEdit() {
        sendPlayerMessage(
            /*? if >=26.1 {*/
            /*Component.translatable("command.pullup.client.cloud.edit.creating")*/
            /*?} else {*/
            translatable("command.pullup.client.cloud.edit.creating")
            /*?}*/
        );

        CompletableFuture.runAsync(() -> {
            try {
                cool.muyucloud.pullup.cloud.CloudApiModels.SessionCreateResponse response =
                    cool.muyucloud.pullup.cloud.CloudCommandSupport.createEditSession();

                /*? if >=26.1 {*/
                /*Minecraft.getInstance().execute(() -> {
                    sendPlayerMessage(Component.translatable("command.pullup.client.cloud.edit.ready"));

                    Component clickable = Component.literal(response.editUrl).withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(response.editUrl)))
                    );
                    sendPlayerMessage(clickable);

                    if (response.expiresAt != null && !response.expiresAt.isBlank()) {
                        sendPlayerMessage(Component.translatable("command.pullup.client.cloud.edit.expires", response.expiresAt));
                    }
                });*/
                /*?} else {*/
                MinecraftClient.getInstance().execute(() -> {
                    sendPlayerMessage(translatable("command.pullup.client.cloud.edit.ready"));

                    MutableText clickable = literal(response.editUrl).formatted(Formatting.AQUA, Formatting.UNDERLINE)
                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, response.editUrl)));
                    sendPlayerMessage(clickable);

                    if (response.expiresAt != null && !response.expiresAt.isBlank()) {
                        sendPlayerMessage(translatable("command.pullup.client.cloud.edit.expires", response.expiresAt));
                    }
                });
                /*?}*/
            } catch (Exception e) {
                /*? if >=26.1 {*/
                /*Minecraft.getInstance().execute(() -> {
                    sendPlayerMessage(Component.translatable("command.pullup.client.cloud.edit.failed", e.getMessage()));
                });*/
                /*?} else {*/
                MinecraftClient.getInstance().execute(() -> {
                    sendPlayerMessage(translatable("command.pullup.client.cloud.edit.failed", e.getMessage()));
                });
                /*?}*/
            }
        });
    }

    private
    /*? if >=26.1 {*/
    /*Component*/
    /*?} else {*/
    MutableText
    /*?}*/
    buttonLabel(String setName, boolean enabled) {
        /*? if >=26.1 {*/
        /*return Component.translatable(
            "gui.pullup.button_label",
            Component.literal(enabled ? "[x] " : "[ ] ").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY),
            Component.literal(setName),
            "default".equals(setName) ? Component.translatable("gui.pullup.builtin").withStyle(ChatFormatting.GRAY) : Component.empty()
        ).withStyle(ChatFormatting.WHITE);*/
        /*?} else {*/
        return translatable(
            "gui.pullup.button_label",
            literal(enabled ? "[x] " : "[ ] ").formatted(enabled ? Formatting.GREEN : Formatting.DARK_GRAY),
            literal(setName),
            "default".equals(setName) ? translatable("gui.pullup.builtin").formatted(Formatting.GRAY) : emptyText()
        ).formatted(Formatting.WHITE);
        /*?}*/
    }

    private
    /*? if >=26.1 {*/
    /*Component*/
    /*?} else {*/
    MutableText
    /*?}*/
    toggleLabel(String key, boolean enabled) {
        /*? if >=26.1 {*/
        /*return Component.translatable(
            key,
            Component.translatable(enabled ? "gui.pullup.toggle.on" : "gui.pullup.toggle.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        ).withStyle(ChatFormatting.GRAY);*/
        /*?} else {*/
        return translatable(
            key,
            translatable(enabled ? "gui.pullup.toggle.on" : "gui.pullup.toggle.off")
                .formatted(enabled ? Formatting.GREEN : Formatting.RED)
        ).formatted(Formatting.GRAY);
        /*?}*/
    }

    private
    /*? if >=26.1 {*/
    /*Component*/
    /*?} else {*/
    MutableText
    /*?}*/
    cloudButtonLabel() {
        /*? if >=26.1 {*/
        /*return Component.translatable("gui.pullup.btn_cloud").withStyle(ChatFormatting.AQUA);*/
        /*?} else {*/
        return translatable("gui.pullup.btn_cloud").formatted(Formatting.AQUA);
        /*?}*/
    }

    /*? if <26.1 {*/
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

    private static MutableText emptyText() {
        /*? if <=1.18.2 {*/
        /*return new LiteralText("");*/
        /*?} else {*/
        return Text.empty();
        /*?}*/
    }
    /*?}*/

    private void addButton(
        int x,
        int y,
        int width,
        int height,
        /*? if >=26.1 {*/
        /*Component*/
        /*?} else {*/
        Text
        /*?}*/
        label,
        Runnable onPress
    ) {
        /*? if >=26.1 {*/
        /*this.addRenderableWidget(Button.builder(label, button -> onPress.run()).bounds(x, y, width, height).build());*/
        /*?} else {*/
        /*? if <=1.18.2 {*/
        /*this.addDrawableChild(new ButtonWidget(x, y, width, height, label, button -> onPress.run()));*/
        /*?} else {*/
        this.addDrawableChild(ButtonWidget.builder(label, button -> onPress.run()).dimensions(x, y, width, height).build());
        /*?}*/
        /*?}*/
    }
}
