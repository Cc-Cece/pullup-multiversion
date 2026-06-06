package cool.muyucloud.pullup.cloud;

import cool.muyucloud.pullup.util.PlatformAccess;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*? if >=26.1 {*/
/*import net.minecraft.client.Minecraft;*/
/*?} else {*/
import net.minecraft.client.MinecraftClient;
/*?}*/

final class CloudClientContextCollector {
    private static final Pattern SOUND_ID_PATTERN = Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]+)");
    private static final int MAX_DISCOVERABLE_SOUNDS = 4096;

    private CloudClientContextCollector() {}

    static CloudApiModels.ClientContext collect() {
        CloudApiModels.ClientContext context = new CloudApiModels.ClientContext();
        context.protocolVersion = CloudApiModels.SUPPORTED_PROTOCOL_VERSION;
        context.isSnapshot = looksLikeSnapshot(PlatformAccess.getMinecraftVersion());
        context.resourcePackFormat = resolveResourcePackFormat(PlatformAccess.getMinecraftVersion());
        context.resourcePackFormatRange = createResourcePackFormatRange(context.resourcePackFormat);
        context.capabilities = createCapabilities(context.resourcePackFormat);
        context.discoverableSounds = collectDiscoverableSounds();
        return context;
    }

    static String detectLanguage() {
        String languageTag = Locale.getDefault().toLanguageTag();
        if (languageTag == null || languageTag.isBlank()) {
            return "en_us";
        }
        return languageTag.replace('-', '_').toLowerCase(Locale.ROOT);
    }

    private static CloudApiModels.ResourcePackFormatRange createResourcePackFormatRange(Integer format) {
        if (format == null) {
            return null;
        }
        CloudApiModels.ResourcePackFormatRange range = new CloudApiModels.ResourcePackFormatRange();
        range.min = format;
        range.max = format;
        return range;
    }

    private static CloudApiModels.ClientCapabilities createCapabilities(Integer resourcePackFormat) {
        CloudApiModels.ClientCapabilities capabilities = new CloudApiModels.ClientCapabilities();
        capabilities.supportsCustomSounds = true;
        capabilities.supportsCloudImport = true;
        capabilities.supportsResourcePackMinMaxFormat = resourcePackFormat != null && resourcePackFormat >= 65;
        return capabilities;
    }

    private static List<CloudApiModels.DiscoverableSound> collectDiscoverableSounds() {
        Object soundManager = getSoundManager();
        if (soundManager == null) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> soundIds = new LinkedHashSet<>();
        collectSoundIds(soundIds, invokeNoArg(soundManager, "getAvailableSounds"), 0);
        collectSoundIds(soundIds, invokeNoArg(soundManager, "getKeys"), 0);
        collectSoundIds(soundIds, invokeNoArg(soundManager, "getLoadedSounds"), 0);
        collectSoundIds(soundIds, invokeNoArg(soundManager, "getRegisteredSounds"), 0);

        if (soundIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CloudApiModels.DiscoverableSound> results = new ArrayList<>(soundIds.size());
        for (String soundId : soundIds) {
            CloudApiModels.DiscoverableSound sound = new CloudApiModels.DiscoverableSound();
            sound.id = soundId;
            sound.source = classifySoundSource(soundId);
            results.add(sound);
        }
        results.sort(Comparator.comparing(value -> value.id));
        return results;
    }

    private static Object getSoundManager() {
        /*? if >=26.1 {*/
        /*Minecraft client = Minecraft.getInstance();
        return client == null ? null : client.getSoundManager();*/
        /*?} else {*/
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null ? null : client.getSoundManager();
        /*?}*/
    }

    private static void collectSoundIds(Set<String> collector, Object value, int depth) {
        if (value == null || depth > 4 || collector.size() >= MAX_DISCOVERABLE_SOUNDS) {
            return;
        }

        if (value instanceof CharSequence sequence) {
            addSoundIdsFromText(collector, sequence.toString());
            return;
        }

        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                collectSoundIds(collector, entry.getKey(), depth + 1);
                if (collector.size() >= MAX_DISCOVERABLE_SOUNDS) {
                    return;
                }
                collectSoundIds(collector, entry.getValue(), depth + 1);
                if (collector.size() >= MAX_DISCOVERABLE_SOUNDS) {
                    return;
                }
            }
            return;
        }

        if (value instanceof Collection<?> collection) {
            for (Object element : collection) {
                collectSoundIds(collector, element, depth + 1);
                if (collector.size() >= MAX_DISCOVERABLE_SOUNDS) {
                    return;
                }
            }
            return;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index += 1) {
                collectSoundIds(collector, Array.get(value, index), depth + 1);
                if (collector.size() >= MAX_DISCOVERABLE_SOUNDS) {
                    return;
                }
            }
            return;
        }

        addSoundIdsFromText(collector, String.valueOf(value));
    }

    private static void addSoundIdsFromText(Set<String> collector, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        Matcher matcher = SOUND_ID_PATTERN.matcher(text);
        while (matcher.find() && collector.size() < MAX_DISCOVERABLE_SOUNDS) {
            collector.add(matcher.group(1));
        }
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String classifySoundSource(String soundId) {
        if (soundId.startsWith("pullup:")) {
            return "plugin_builtin";
        }
        if (soundId.startsWith("minecraft:")) {
            return "vanilla";
        }
        return "unknown";
    }

    private static boolean looksLikeSnapshot(String minecraftVersion) {
        String value = minecraftVersion == null ? "" : minecraftVersion.toLowerCase(Locale.ROOT);
        return value.contains("snapshot")
            || value.contains("pre")
            || value.contains("rc")
            || value.matches(".*\\d{2}w\\d{2}[a-z].*");
    }

    private static Integer resolveResourcePackFormat(String minecraftVersion) {
        int[] version = parseReleaseVersion(minecraftVersion);
        if (version == null) {
            return null;
        }

        int major = version[0];
        int minor = version[1];
        int patch = version[2];

        if (major == 1 && minor == 18) {
            return 8;
        }
        if (major == 1 && minor == 19) {
            return patch >= 3 ? 12 : 9;
        }
        if (major == 1 && minor == 20) {
            if (patch >= 5) {
                return 32;
            }
            if (patch >= 3) {
                return 22;
            }
            if (patch >= 2) {
                return 18;
            }
            return 15;
        }
        if (major == 1 && minor == 21) {
            if (patch >= 11) {
                return 75;
            }
            if (patch >= 9) {
                return 69;
            }
            if (patch >= 7) {
                return 64;
            }
            if (patch >= 6) {
                return 63;
            }
            if (patch >= 5) {
                return 55;
            }
            if (patch >= 4) {
                return 46;
            }
            if (patch >= 2) {
                return 42;
            }
            return 34;
        }
        if (major > 1 || (major == 1 && minor >= 22)) {
            return 75;
        }
        return null;
    }

    private static int[] parseReleaseVersion(String minecraftVersion) {
        if (minecraftVersion == null || minecraftVersion.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(minecraftVersion);
        if (!matcher.find()) {
            return null;
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new int[] { major, minor, patch };
    }
}
