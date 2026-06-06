package net.minecraftforge.fml;

import java.util.function.Supplier;

/**
 * 1.20.1-fabric only shim for Forge-patched crash report hooks in merged Minecraft jars.
 */
public interface ISystemReportExtender extends Supplier<String> {
    boolean isActive();

    String getLabel();
}
