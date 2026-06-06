package cool.muyucloud.pullup.v26;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.engine.PullupEngine;
import cool.muyucloud.pullup.engine.PullupEngineResult;
import cool.muyucloud.pullup.engine.PullupEngineState;
import cool.muyucloud.pullup.engine.snapshot.FlightSessionClock;
import cool.muyucloud.pullup.engine.snapshot.FlightSnapshot;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformIds;
import cool.muyucloud.pullup.util.PlatformSoundEvents;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.Condition;
import java.util.List;
/*? if >=26.1 {*/
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;*/
/*?}*/

public final class Pullup26Runtime {
    private Pullup26Runtime() {}

    private static final Config CONFIG = Pullup.getConfig();
    private static final PullupEngine ENGINE = new PullupEngine();
    private static final PullupEngineState ENGINE_STATE = new PullupEngineState();
    private static final FlightSessionClock FLIGHT_CLOCK = new FlightSessionClock();

    /*? if >=26.1 {*/
    /*private static boolean localConditionLoadAttempted = false;*/
    /*?}*/

    public static void onFabricClientTick(Object ignored) {
        /*? if >=26.1 {*/
        /*tickClient();*/
        /*?}*/
    }

    public static void onNeoForgeClientTick(Object ignored) {
        /*? if >=26.1 {*/
        /*tickClient();*/
        /*?}*/
    }

    public static void onClientJoin(boolean loadServer) {
        /*? if >=26.1 {*/
        /*resetRuntimeState();
        if (!loadServer) {
            localConditionLoadAttempted = true;
            loadConfiguredLocalConditions();
        }*/
        /*?}*/
    }

    public static void onClientDisconnect() {
        /*? if >=26.1 {*/
        /*resetRuntimeState();
        ConditionSetRuntime.clear();*/
        /*?}*/
    }

    public static void clearLoadedConditions() {
        /*? if >=26.1 {*/
        /*ENGINE_STATE.clear();
        ConditionSetRuntime.clear();*/
        /*?}*/
    }

    public static boolean applyRemoteConditions(String setName, String json) {
        /*? if >=26.1 {*/
        /*ENGINE_STATE.clear();
        localConditionLoadAttempted = true;
        return ConditionSetRuntime.applySerializedSet(setName, json);*/
        /*?} else {*/
        return false;
        /*?}*/
    }

    public static List<Condition.ColoredText> getHudTexts() {
        return ENGINE_STATE.getHudTexts();
    }

    /*? if >=26.1 {*/
    /*private static void tickClient() {
        if (!CONFIG.getAsBool("enable")) {
            ENGINE_STATE.clear();
            return;
        }

        final Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) {
            return;
        }

        final LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (!FLIGHT_CLOCK.update(player.isFallFlying())) {
            return;
        }

        if (!ensureConditionsReady()) {
            return;
        }

        FlightSnapshot snapshot = buildSnapshot(player);
        PullupEngineResult result = ENGINE.tick(snapshot, Registry.CONDITIONS.getAll(), ENGINE_STATE);
        for (String soundId : result.getSoundsToPlay()) {
            client.level.playLocalSound(
                player.getX(),
                player.getY(),
                player.getZ(),
                createSoundEvent(PlatformIds.parse(soundId)),
                SoundSource.VOICE,
                1.0F,
                1.0F,
                false
            );
        }
    }

    private static boolean ensureConditionsReady() {
        if (!Registry.CONDITIONS.getAll().isEmpty()) {
            return true;
        }

        if (localConditionLoadAttempted) {
            return false;
        }
        localConditionLoadAttempted = true;
        return loadConfiguredLocalConditions();
    }

    private static FlightSnapshot buildSnapshot(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
        return new FlightSnapshot(
            player.isFallFlying(),
            player.getY(),
            getRelativeHeight(player),
            speed,
            horizontalSpeed,
            velocity.y,
            player.getYRot(),
            player.getYRot() - player.yRotO,
            player.getXRot(),
            player.getXRot() - player.xRotO,
            getPitchedDistanceAhead(player, 0),
            getDistanceHorizontal(player),
            FLIGHT_CLOCK.getCurrentTick()
        );
    }

    private static double getDistanceHorizontal(LocalPlayer player) {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = player.getEyePosition();
        Vec3 rotate = player.calculateViewVector(0, player.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = player.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getLocation();
        return cameraPos.distanceTo(target);
    }

    private static double getPitchedDistanceAhead(LocalPlayer player, float pitch) {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = player.getEyePosition();
        Vec3 rotate = player.calculateViewVector(player.getXRot() + pitch, player.getYRot());
        Vec3 endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3 target = player.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getLocation();
        return cameraPos.distanceTo(target);
    }

    private static double getRelativeHeight(LocalPlayer player) {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3 cameraPos = player.getEyePosition();
        Vec3 endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3 target = player.level().clip(new ClipContext(cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getLocation();
        return cameraPos.distanceTo(target);
    }

    private static SoundEvent createSoundEvent(Identifier id) {
        return PlatformSoundEvents.create(id);
    }

    private static boolean loadConfiguredLocalConditions() {
        ENGINE_STATE.clear();
        return ConditionSetRuntime.loadConfiguredLocalSet();
    }

    private static void resetRuntimeState() {
        ENGINE_STATE.clear();
        FLIGHT_CLOCK.reset();
        localConditionLoadAttempted = false;
    }*/
    /*?}*/
}
