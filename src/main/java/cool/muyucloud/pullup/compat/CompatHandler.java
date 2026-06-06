package cool.muyucloud.pullup.compat;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.PlatformAccess;

public class CompatHandler {

    public static void init() {
        if (PlatformAccess.isModLoaded("flighthud")) {
            Pullup.getLogger().info("Detected optional FlightHUD companion mod.");
        }
    }
}
