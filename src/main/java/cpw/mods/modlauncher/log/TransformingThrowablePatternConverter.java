package cpw.mods.modlauncher.log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 1.20.1-fabric only shim for Forge-patched crash report hooks in merged Minecraft jars.
 */
public final class TransformingThrowablePatternConverter {
    private TransformingThrowablePatternConverter() {
    }

    public static String generateEnhancedStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
