package cool.muyucloud.pullup.util;

import java.nio.file.Path;

/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
*/
/*?}*/

/*? if neoforge {*/
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
*/
/*?}*/

public final class PlatformAccess {
    private PlatformAccess() {}

    public static Path getConfigDir() {
        /*? if fabric {*/
        return FabricLoader.getInstance().getConfigDir();
        /*?}*/
        /*? if forgeLike {*/
        /*return FMLPaths.CONFIGDIR.get();*/
        /*?}*/
    }

    public static Path getGameDir() {
        /*? if fabric {*/
        return FabricLoader.getInstance().getGameDir();
        /*?}*/
        /*? if forgeLike {*/
        /*return FMLPaths.GAMEDIR.get();*/
        /*?}*/
    }

    public static boolean isModLoaded(String modId) {
        /*? if fabric {*/
        return FabricLoader.getInstance().isModLoaded(modId);
        /*?}*/
        /*? if forgeLike {*/
        /*return ModList.get().isLoaded(modId);*/
        /*?}*/
    }

    public static String getLoaderName() {
        /*? if fabric {*/
        return "fabric";
        /*?}*/
        /*? if forge {*/
        /*return "forge";*/
        /*?}*/
        /*? if neoforge {*/
        /*return "neoforge";*/
        /*?}*/
    }

    public static String getMinecraftVersion() {
        /*? if fabric {*/
        return FabricLoader.getInstance()
            .getModContainer("minecraft")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
        /*?}*/
        /*? if forgeLike {*/
        /*return ModList.get()
            .getModContainerById("minecraft")
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown");*/
        /*?}*/
    }
}
