package net.minecraftforge.forge.snapshots;

import net.minecraft.util.crash.CrashReport;

/**
 * 1.20.1-fabric only shim for Forge-patched crash report hooks in merged Minecraft jars.
 */
public final class ForgeSnapshotsMod {
    private ForgeSnapshotsMod() {
    }

    public static void addCrashReportHeader(StringBuilder reportBuilder, CrashReport crashReport) {
        // No-op on Fabric.
    }
}
