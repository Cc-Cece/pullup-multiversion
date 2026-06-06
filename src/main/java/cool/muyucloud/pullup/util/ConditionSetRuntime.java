package cool.muyucloud.pullup.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class ConditionSetRuntime {
    private static final Logger LOGGER = Pullup.getLogger();
    private static final Config CONFIG = Pullup.getConfig();
    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConditionSetRuntime() {}

    public static void clear() {
        Registry.CONDITIONS.clear();
    }

    public static boolean loadConfiguredLocalSet() {
        return loadLocalSets(readConfiguredSetNames());
    }

    public static boolean loadLocalSet(String loadSet) {
        return loadLocalSets(List.of(loadSet));
    }

    public static boolean loadLocalSets(Collection<String> loadSets) {
        clear();
        boolean attemptedAny = false;

        for (String loadSet : normalizeSetNames(loadSets)) {
            attemptedAny = true;
            try {
                if (Objects.equals(loadSet, "default")) {
                    new ConditionLoader().load();
                } else {
                    new ConditionLoader(loadSet).load();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load local condition set {}.", loadSet, e);
            }
        }

        return attemptedAny && !Registry.CONDITIONS.getAll().isEmpty();
    }

    public static boolean applySerializedSet(String setName, String json) {
        clear();

        try {
            new ConditionLoader(setName, json).load();
            return !Registry.CONDITIONS.getAll().isEmpty();
        } catch (Exception e) {
            LOGGER.error("Failed to apply serialized condition set {}.", setName, e);
            return false;
        }
    }

    public static List<String> readConfiguredSetNames() {
        return normalizeSetNames(CONFIG.getAsStringList("enabledSets"));
    }

    public static String readConfiguredSetName() {
        List<String> enabledSets = readConfiguredSetNames();
        if (enabledSets.isEmpty()) {
            return "none";
        }
        if (enabledSets.size() == 1) {
            return enabledSets.get(0);
        }
        return "combined";
    }

    public static String readConfiguredSetContent() {
        JsonArray merged = new JsonArray();

        for (String loadSet : readConfiguredSetNames()) {
            String content = readSingleSetContent(loadSet);
            if (content.isBlank()) {
                continue;
            }

            try {
                JsonArray array = GSON.fromJson(content, JsonArray.class);
                if (array == null) {
                    continue;
                }
                for (JsonElement element : array) {
                    merged.add(element.deepCopy());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to merge configured condition set {}.", loadSet, e);
            }
        }

        return PRETTY_GSON.toJson(merged);
    }

    public static boolean saveEnabledSetsAndLoad(Collection<String> enabledSets) {
        List<String> normalized = normalizeSetNames(enabledSets);
        CONFIG.set("enabledSets", normalized);
        CONFIG.save();
        return loadLocalSets(normalized);
    }

    public static List<String> listAvailableSetNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add("default");
        names.addAll(
            ConditionLoader.getFileList().stream()
                .sorted(Comparator.naturalOrder())
                .toList()
        );
        return names;
    }

    private static String readSingleSetContent(String loadSet) {
        try {
            if (Objects.equals(loadSet, "default")) {
                return new ConditionLoader().getFileContent();
            }
            return new ConditionLoader(loadSet).getFileContent();
        } catch (Exception e) {
            LOGGER.error("Failed to read configured condition set {}.", loadSet, e);
            return "";
        }
    }

    private static List<String> normalizeSetNames(Collection<String> loadSets) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();

        if (loadSets != null) {
            for (String loadSet : loadSets) {
                if (loadSet == null) {
                    continue;
                }
                String normalized = loadSet.trim().replace('\\', '/');
                if (!normalized.isEmpty()) {
                    unique.add(normalized);
                }
            }
        }
        return List.copyOf(unique);
    }
}
