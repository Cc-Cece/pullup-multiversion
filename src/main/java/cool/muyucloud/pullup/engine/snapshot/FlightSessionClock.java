package cool.muyucloud.pullup.engine.snapshot;

public class FlightSessionClock {
    private long flightStart = System.currentTimeMillis();
    private int currentTick = 0;
    private boolean newTick = false;

    public boolean update(boolean flying) {
        if (!flying) {
            this.currentTick = 0;
            this.newTick = false;
            this.flightStart = System.currentTimeMillis();
            return false;
        }

        int nextTick = (int) ((System.currentTimeMillis() - this.flightStart) / 50L);
        this.newTick = nextTick != this.currentTick;
        this.currentTick = nextTick;
        return this.newTick;
    }

    public int getCurrentTick() {
        return this.currentTick;
    }

    public void reset() {
        this.flightStart = System.currentTimeMillis();
        this.currentTick = 0;
        this.newTick = false;
    }
}
