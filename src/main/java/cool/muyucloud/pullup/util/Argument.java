package cool.muyucloud.pullup.util;

import cool.muyucloud.pullup.engine.snapshot.FlightSnapshot;

public interface Argument {
    double compute(FlightSnapshot snapshot);
}
