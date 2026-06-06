package cool.muyucloud.pullup.engine.snapshot;

public record FlightSnapshot(
    boolean isFlying,
    double absoluteHeight,
    double relativeHeight,
    double speed,
    double horizontalSpeed,
    double verticalSpeed,
    double yaw,
    double deltaYaw,
    double pitch,
    double deltaPitch,
    double distanceAhead,
    double distanceHorizontal,
    int flightTicks
) {
}
