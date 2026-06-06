package net.minecraftforge.fml.loading;

/**
 * 1.20.1-fabric only shim for Forge-patched main entry hooks in merged Minecraft jars.
 */
public final class FMLLoader {
    public static final Runnable progressWindowTick = () -> {
    };

    private FMLLoader() {
    }
}
