package cool.muyucloud.pullup.util;

/*? if >=26.1 {*/
/*import net.minecraft.resources.Identifier;*/
/*?} else {*/
import net.minecraft.util.Identifier;
/*?}*/

public final class PlatformIds {
    private PlatformIds() {}

    public static Identifier of(String namespace, String path) {
        /*? if >=26.1 {*/
        /*return Identifier.fromNamespaceAndPath(namespace, path);*/
        /*?} else {*/
        /*? if >=1.21 {*/
        return Identifier.of(namespace, path);
        /*?} else {*/
        return new Identifier(namespace, path);
        /*?}*/
        /*?}*/
    }

    public static Identifier parse(String value) {
        /*? if >=26.1 {*/
        /*return Identifier.parse(value);*/
        /*?} else {*/
        /*? if >=1.21 {*/
        return Identifier.of(value);
        /*?} else {*/
        return new Identifier(value);
        /*?}*/
        /*?}*/
    }

    public static Identifier tryParse(String value) {
        /*? if >=26.1 {*/
        /*return Identifier.tryParse(value);*/
        /*?} else {*/
        return Identifier.tryParse(value);
        /*?}*/
    }
}
