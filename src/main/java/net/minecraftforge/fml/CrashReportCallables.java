package net.minecraftforge.fml;

import java.util.Collections;
import java.util.List;

/**
 * 1.20.1-fabric only shim for Forge-patched crash report hooks in merged Minecraft jars.
 */
public final class CrashReportCallables {
    private CrashReportCallables() {
    }

    public static List<ISystemReportExtender> allCrashCallables() {
        return Collections.emptyList();
    }
}
