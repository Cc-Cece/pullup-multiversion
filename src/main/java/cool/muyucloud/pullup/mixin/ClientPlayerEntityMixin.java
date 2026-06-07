package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.adapter.client.ConditionManagerScreen;
import cool.muyucloud.pullup.engine.PullupEngine;
import cool.muyucloud.pullup.engine.PullupEngineResult;
import cool.muyucloud.pullup.engine.PullupEngineState;
import cool.muyucloud.pullup.engine.snapshot.FlightSessionClock;
import cool.muyucloud.pullup.engine.snapshot.FlightSnapshot;
import cool.muyucloud.pullup.util.ConditionSetRuntime;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformSoundEvents;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.Condition;
import cool.muyucloud.pullup.util.network.PullupNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements ClientPlayerEntityAccess {
    @Shadow
    @Override
    public abstract float getYaw(float tickDelta);

    @Shadow
    @Override
    public abstract void tick();

    @Shadow
    @Final
    protected MinecraftClient client;

    @Unique
    private static final Logger LOGGER = Pullup.getLogger();
    @Unique
    private static final cool.muyucloud.pullup.util.Config PULLUP_CONFIG = Pullup.getConfig();
    @Unique
    private static final PullupEngine ENGINE = new PullupEngine();

    @Unique
    private boolean localConditionLoadAttempted = false;
    @Unique
    private boolean serverConditionRequestSent = false;
    @Unique
    private boolean pullupOrientationInitialized = false;
    @Unique
    private float pullupPreviousYaw;
    @Unique
    private float pullupPreviousPitch;
    @Unique
    private final FlightSessionClock flightSessionClock = new FlightSessionClock();
    @Unique
    private final PullupEngineState engineState = new PullupEngineState();

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        /*? if >=1.21.8 {*/
        super(world, profile);
        /*?} else {*/
        /*? if =1.19.2 {*/
        /*super(world, pos, yaw, profile, null);*/
        /*?} else {*/
        super(world, pos, yaw, profile);
        /*?}*/
        /*?}*/
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ConditionManagerScreen.openIfRequested();
        this.initializeOrientationIfNeeded();

        if (!this.flightSessionClock.update(this.isPullupFallFlying())) {
            this.captureOrientation();
            return;
        }

        if (!this.ensureConditionsReady()) {
            this.captureOrientation();
            return;
        }

        PullupEngineResult result = ENGINE.tick(this.buildSnapshot(), Registry.CONDITIONS.getAll(), this.engineState);
        this.playSounds(result);
        this.captureOrientation();
    }

    @Unique
    private boolean ensureConditionsReady() {
        if (!Registry.CONDITIONS.getAll().isEmpty()) {
            return true;
        }

        /*? if fabric {*/
        boolean loadServer = PULLUP_CONFIG.getAsBool("loadServer");
        /*?} else {*/
        /*boolean loadServer = false;*/
        /*?}*/
        if (loadServer) {
            if (!this.serverConditionRequestSent) {
                PullupNetworking.requestServerConditions();
                this.serverConditionRequestSent = true;
            }
            return false;
        }

        if (this.localConditionLoadAttempted) {
            return false;
        }
        this.localConditionLoadAttempted = true;

        try {
            return ConditionSetRuntime.loadConfiguredLocalSet();
        } catch (Exception e) {
            LOGGER.error("Failed to load local condition set in client tick fallback.", e);
            return false;
        }
    }

    @Unique
    private FlightSnapshot buildSnapshot() {
        Vec3d velocity = this.getVelocity();
        double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
        return new FlightSnapshot(
            this.isPullupFallFlying(),
            this.getY(),
            this.getRelativeHeight(),
            speed,
            horizontalSpeed,
            velocity.y,
            this.getYaw(),
            this.getDeltaYaw(),
            this.getPitch(),
            this.getDeltaPitch(),
            this.getPitchedDistanceAhead(0),
            this.getDistanceHorizontal(),
            this.flightSessionClock.getCurrentTick()
        );
    }

    @Unique
    private void playSounds(PullupEngineResult result) {
        if (this.client.world == null) {
            return;
        }

        for (String soundId : result.getSoundsToPlay()) {
            Identifier identifier = Identifier.tryParse(soundId);
            if (identifier == null) {
                continue;
            }
            /*? if >=1.21.5 {*/
            this.client.world.playSound(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                createSoundEvent(identifier),
                SoundCategory.VOICE,
                1.0F,
                1.0F
            );
            /*?} else {*/
            /*this.client.world.playSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                createSoundEvent(identifier),
                SoundCategory.VOICE,
                1.0F,
                1.0F,
                false
            );*/
            /*?}*/
        }
    }

    @Unique
    private static SoundEvent createSoundEvent(Identifier id) {
        return PlatformSoundEvents.create(id);
    }

    @Unique
    private void initializeOrientationIfNeeded() {
        if (this.pullupOrientationInitialized) {
            return;
        }

        this.pullupOrientationInitialized = true;
        this.pullupPreviousYaw = this.getYaw();
        this.pullupPreviousPitch = this.getPitch();
    }

    @Unique
    private void captureOrientation() {
        this.pullupPreviousYaw = this.getYaw();
        this.pullupPreviousPitch = this.getPitch();
    }

    @Unique
    @Override
    public double getDistanceHorizontal() {
        int maxDistance = PULLUP_CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d rotate = this.getRotationVector(0, this.getYaw());
        Vec3d endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3d target = this.getActiveWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getPitchedDistanceAhead(float pitch) {
        int maxDistance = PULLUP_CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d rotate = this.getRotationVector(this.getPitch() + pitch, this.getYaw());
        Vec3d endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3d target = this.getActiveWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getRelativeHeight() {
        int maxDistance = PULLUP_CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3d target = this.getActiveWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    private boolean isPullupFallFlying() {
        /*? if >=1.21.3 {*/
        return this.isGliding();
        /*?} else {*/
        /*return this.isFallFlying();*/
        /*?}*/
    }

    @Unique
    private World getActiveWorld() {
        /*? if >=1.21.11 {*/
        return this.client.world;
        /*?} else {*/
        /*return this.getWorld();*/
        /*?}*/
    }

    @Unique
    @Override
    public double getDeltaYaw() {
        return this.getYaw() - this.pullupPreviousYaw;
    }

    @Unique
    @Override
    public double getDeltaPitch() {
        return this.getPitch() - this.pullupPreviousPitch;
    }

    @Unique
    @Override
    public double getFlightTicks() {
        return this.flightSessionClock.getCurrentTick();
    }

    @Unique
    @Override
    public List<Condition.ColoredText> getHudTexts() {
        return this.engineState.getHudTexts();
    }
}
