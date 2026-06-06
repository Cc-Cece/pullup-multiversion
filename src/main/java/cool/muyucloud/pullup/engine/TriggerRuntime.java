package cool.muyucloud.pullup.engine;

public class TriggerRuntime {
    private int lastPlayTick = -1;
    private boolean triggered = false;

    public int lastPlayTick() {
        return this.lastPlayTick;
    }

    public void markPlayed(int tick) {
        this.lastPlayTick = tick;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isTriggered() {
        return this.triggered;
    }

    public void reset() {
        this.triggered = false;
        this.lastPlayTick = -1;
    }
}
