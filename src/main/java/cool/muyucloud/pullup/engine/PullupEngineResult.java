package cool.muyucloud.pullup.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PullupEngineResult {
    private final List<String> soundsToPlay = new ArrayList<>();

    public void addSound(String soundId) {
        this.soundsToPlay.add(soundId);
    }

    public List<String> getSoundsToPlay() {
        return Collections.unmodifiableList(this.soundsToPlay);
    }
}
