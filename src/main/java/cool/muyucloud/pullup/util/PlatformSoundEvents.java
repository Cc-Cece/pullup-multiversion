package cool.muyucloud.pullup.util;

/*? if >=26.1 {*/
/*import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;*/
/*?} else {*/
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
/*?}*/

public final class PlatformSoundEvents {
    private PlatformSoundEvents() {}

    public static SoundEvent create(Identifier id) {
        /*? if <=1.19.2 {*/
        /*return new SoundEvent(id);*/
        /*?} else {*/
        /*? if >=26.1 {*/
        /*return SoundEvent.createVariableRangeEvent(id);*/
        /*?} else {*/
        return SoundEvent.of(id, 0);
        /*?}*/
        /*?}*/
    }
}
