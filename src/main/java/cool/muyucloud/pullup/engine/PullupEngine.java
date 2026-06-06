package cool.muyucloud.pullup.engine;

import cool.muyucloud.pullup.engine.snapshot.FlightSnapshot;
import cool.muyucloud.pullup.util.condition.Condition;

import java.util.Collection;

public class PullupEngine {
    public PullupEngineResult tick(FlightSnapshot snapshot, Collection<Condition> conditions, PullupEngineState state) {
        PullupEngineResult result = new PullupEngineResult();

        if (!snapshot.isFlying()) {
            state.removeMissingConditions(conditions);
            return result;
        }

        state.removeMissingConditions(conditions);
        for (Condition condition : conditions) {
            if (snapshot.flightTicks() % condition.getCheckDelay() != 0) {
                continue;
            }

            TriggerRuntime trigger = state.getOrCreateTrigger(condition.getId().toString());
            if (!condition.verifyExpressions(snapshot)) {
                trigger.reset();
                if (!condition.getHudText().isEmpty()) {
                    state.removeHudText(condition.getId().toString());
                }
                continue;
            }

            trigger.setTriggered(true);
            if (!condition.getHudText().isEmpty()) {
                state.putHudText(condition.getId().toString(), condition.getHudText());
            }

            if (!condition.shouldLoopPlay()) {
                if (trigger.lastPlayTick() == -1) {
                    trigger.markPlayed(snapshot.flightTicks());
                    result.addSound(condition.getSound().toString());
                }
                continue;
            }

            if (condition.getPlayDelay() < (snapshot.flightTicks() - trigger.lastPlayTick())) {
                trigger.markPlayed(snapshot.flightTicks());
                result.addSound(condition.getSound().toString());
            }
        }

        return result;
    }
}
