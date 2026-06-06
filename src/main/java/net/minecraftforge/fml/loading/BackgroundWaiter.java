package net.minecraftforge.fml.loading;

/**
 * 1.20.1-fabric only shim for Forge-patched main entry hooks in merged Minecraft jars.
 */
public final class BackgroundWaiter {
    private BackgroundWaiter() {
    }

    public static void runAndTick(Runnable task, Runnable ticker) {
        if (task != null) {
            task.run();
        }
    }
}
