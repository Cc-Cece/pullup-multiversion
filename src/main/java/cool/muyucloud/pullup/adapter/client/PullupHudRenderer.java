package cool.muyucloud.pullup.adapter.client;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import cool.muyucloud.pullup.util.condition.Condition;
/*? if >=26.1 {*/
/*import cool.muyucloud.pullup.v26.Pullup26Runtime;*/
/*?}*/
import org.apache.logging.log4j.Logger;

import java.util.List;

/*? if <26.1 {*/
import cool.muyucloud.pullup.access.ClientPlayerEntityAccess;
/*? if fabric && <=1.19.4 || forge && <=1.19.4 {*/
/*import net.minecraft.client.util.math.MatrixStack;*/
/*?} else {*/
/*? if fabric && >=1.20.1 || forge && >=1.20.1 || neoforge {*/
import net.minecraft.client.gui.DrawContext;
/*?}*/
/*?}*/
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
/*?}*/

/*? if fabric {*/
/*? if >=26.1 {*/
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
*/
/*?} else {*/
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.Identifier;
/*?}*/
/*?}*/

/*? if forge && <=1.18.2 {*/
/*import net.minecraftforge.client.event.RenderGameOverlayEvent;*/
/*?}*/

/*? if forge && >1.18.2 && <26.1 {*/
/*import net.minecraftforge.client.event.RenderGuiEvent;*/
/*?}*/

/*? if neoforge && <26.1 {*/
/*import net.neoforged.neoforge.client.event.RenderGuiEvent;*/
/*?}*/

/*? if neoforge && >=26.1 {*/
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
*/
/*?}*/

public final class PullupHudRenderer {
    private static final Logger LOGGER = Pullup.getLogger();
    private static final Config CONFIG = Pullup.getConfig();

    /*? if fabric {*/
    private static boolean fabricRegistered = false;
    private static final Identifier FABRIC_ELEMENT_ID = PlatformIds.of("pullup", "hud_text");
    /*?}*/

    private PullupHudRenderer() {}

    /*? if fabric {*/
    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }
        fabricRegistered = true;

        /*? if >=26.1 {*/
        /*HudElementRegistry.addLast(FABRIC_ELEMENT_ID, (guiGraphics, deltaTracker) -> renderModern(guiGraphics));*/
        /*?} else {*/
        /*? if <=1.19.4 {*/
        /*HudRenderCallback.EVENT.register((matrices, tickDelta) -> renderLegacy118(matrices));*/
        /*?} else {*/
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> renderLegacy(drawContext));
        /*?}*/
        /*?}*/
        LOGGER.info("Registered PullUp HUD renderer.");
    }
    /*?}*/

    /*? if neoforge && >=26.1 {*/
    /*public static void onNeoForgeRender(RenderGuiEvent.Post event) {
        renderModern(event.getGuiGraphics());
    }*/
    /*?}*/

    /*? if forge && <=1.18.2 {*/
    /*public static void onForgeLegacyRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        renderLegacy118(event.getMatrixStack());
    }*/
    /*?}*/

    /*? if forge && >1.18.2 && <=1.19.4 {*/
    /*public static void onForgeLegacyRender(RenderGuiEvent.Post event) {
        renderLegacy118(event.getPoseStack());
    }*/
    /*?}*/

    /*? if forge && >=1.20.1 && <26.1 {*/
    /*public static void onForgeLegacyRender(RenderGuiEvent.Post event) {
        renderLegacyModern(event.getGuiGraphics());
    }*/
    /*?}*/

    /*? if neoforge && <26.1 {*/
    /*public static void onNeoForgeLegacyRender(RenderGuiEvent.Post event) {
        renderLegacyModern(event.getGuiGraphics());
    }*/
    /*?}*/

    /*? if <26.1 && (fabric && <=1.19.4 || forge && <=1.19.4) {*/
    /*private static void renderLegacy118(MatrixStack matrices) {
        LegacyHudContext context = createLegacyContext();
        if (context == null) {
            return;
        }

        int x = Math.round(context.client().getWindow().getScaledWidth() * CONFIG.getAsFloat("hudTextDisplayX"));
        int y = Math.round(context.client().getWindow().getScaledHeight() * CONFIG.getAsFloat("hudTextDisplayY"));

        for (Condition.ColoredText hudText : context.hudTexts()) {
            if (hudText.isEmpty()) {
                continue;
            }
            context.client().textRenderer.draw(matrices, (Text) hudText.text(), x, y, hudText.color());
            y += context.client().textRenderer.fontHeight;
        }
    }*/
    /*?}*/

    /*? if fabric && <26.1 && >=1.20.1 {*/
    private static void renderLegacy(DrawContext drawContext) {
        LegacyHudContext context = createLegacyContext();
        if (context == null) {
            return;
        }

        renderLegacyTexts(drawContext, context);
    }
    /*?}*/

    /*? if <26.1 && (forge && >=1.20.1 || neoforge) {*/
    /*private static void renderLegacyModern(DrawContext drawContext) {
        LegacyHudContext context = createLegacyContext();
        if (context == null) {
            return;
        }

        renderLegacyTexts(drawContext, context);
    }*/
    /*?}*/

    /*? if <26.1 && (fabric && >=1.20.1 || forge && >=1.20.1 || neoforge) {*/
    private static void renderLegacyTexts(DrawContext drawContext, LegacyHudContext context) {
        if (context.hudTexts().isEmpty()) {
            return;
        }

        int x = Math.round(context.client().getWindow().getScaledWidth() * CONFIG.getAsFloat("hudTextDisplayX"));
        int y = Math.round(context.client().getWindow().getScaledHeight() * CONFIG.getAsFloat("hudTextDisplayY"));

        for (Condition.ColoredText hudText : context.hudTexts()) {
            if (hudText.isEmpty()) {
                continue;
            }
            drawContext.drawText(context.client().textRenderer, (Text) hudText.text(), x, y, hudText.color(), false);
            y += context.client().textRenderer.fontHeight;
        }
    }
    /*?}*/

    /*? if <26.1 {*/
    private static LegacyHudContext createLegacyContext() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return null;
        }

        if (!(client.player instanceof ClientPlayerEntityAccess access)) {
            return null;
        }

        List<Condition.ColoredText> hudTexts = access.getHudTexts();
        if (hudTexts.isEmpty()) {
            return null;
        }

        return new LegacyHudContext(client, hudTexts);
    }

    private record LegacyHudContext(MinecraftClient client, List<Condition.ColoredText> hudTexts) {
    }
    /*?}*/

    /*? if >=26.1 {*/
    /*private static void renderModern(GuiGraphicsExtractor guiGraphics) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.options.hideGui) {
            return;
        }

        List<Condition.ColoredText> hudTexts = Pullup26Runtime.getHudTexts();
        if (hudTexts.isEmpty()) {
            return;
        }

        int x = Math.round(guiGraphics.guiWidth() * CONFIG.getAsFloat("hudTextDisplayX"));
        int y = Math.round(guiGraphics.guiHeight() * CONFIG.getAsFloat("hudTextDisplayY"));

        for (Condition.ColoredText hudText : hudTexts) {
            if (hudText.isEmpty()) {
                continue;
            }
            guiGraphics.text(client.font, (Component) hudText.text(), x, y, hudText.color(), false);
            y += client.font.lineHeight;
        }
    }*/
    /*?}*/
}
