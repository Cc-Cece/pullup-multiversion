package cool.muyucloud.pullup.engine;

import cool.muyucloud.pullup.util.condition.Condition;

import java.util.*;

public class PullupEngineState {
    private final Map<String, TriggerRuntime> triggers = new HashMap<>();
    private final TreeMap<String, Condition.ColoredText> hudTexts = new TreeMap<>();

    public TriggerRuntime getOrCreateTrigger(String conditionId) {
        return this.triggers.computeIfAbsent(conditionId, ignored -> new TriggerRuntime());
    }

    public void putHudText(String conditionId, Condition.ColoredText hudText) {
        this.hudTexts.put(conditionId, hudText);
    }

    public void removeHudText(String conditionId) {
        this.hudTexts.remove(conditionId);
    }

    public List<Condition.ColoredText> getHudTexts() {
        return List.copyOf(this.hudTexts.values());
    }

    public void removeMissingConditions(Collection<Condition> conditions) {
        Set<String> activeConditionIds = new HashSet<>();
        for (Condition condition : conditions) {
            activeConditionIds.add(condition.getId().toString());
        }
        this.triggers.keySet().removeIf(id -> !activeConditionIds.contains(id));
        this.hudTexts.keySet().removeIf(id -> !activeConditionIds.contains(id));
    }

    public void clear() {
        this.triggers.clear();
        this.hudTexts.clear();
    }
}
