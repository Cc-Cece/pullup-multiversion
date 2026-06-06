package cool.muyucloud.pullup.util;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class PullupPaths {
    private static final Path CONFIG_DIR = PlatformAccess.getConfigDir();
    private static final Path ROOT_DIR = CONFIG_DIR.resolve("pullup");
    private static final Path CONFIG_FILE = ROOT_DIR.resolve("config.json");
    private static final Path LEGACY_CONFIG_FILE = CONFIG_DIR.resolve("pullup.json");
    private static final Path CONDITION_DIR = ROOT_DIR.resolve("conditions");

    private PullupPaths() {}

    public static Path getRootDir() {
        return ROOT_DIR;
    }

    public static Path getConfigFile() {
        return CONFIG_FILE;
    }

    public static Path getConditionDir() {
        return CONDITION_DIR;
    }

    public static void ensureRootDir(Logger logger) {
        try {
            Files.createDirectories(ROOT_DIR);
        } catch (IOException e) {
            logger.warn("Failed to create PullUp config directory at {}.", ROOT_DIR, e);
        }
    }

    public static void ensureConditionDir(Logger logger) {
        try {
            Files.createDirectories(CONDITION_DIR);
        } catch (IOException e) {
            logger.warn("Failed to create PullUp condition directory at {}.", CONDITION_DIR, e);
        }
    }

    public static void migrateLegacyConfigIfNeeded(Logger logger) {
        if (Files.exists(CONFIG_FILE) || !Files.exists(LEGACY_CONFIG_FILE)) {
            return;
        }

        ensureRootDir(logger);
        try {
            Files.move(LEGACY_CONFIG_FILE, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Migrated legacy PullUp config from {} to {}.", LEGACY_CONFIG_FILE, CONFIG_FILE);
        } catch (IOException e) {
            logger.warn("Failed to migrate legacy PullUp config from {} to {}.", LEGACY_CONFIG_FILE, CONFIG_FILE, e);
        }
    }

    public static void migrateLegacyConditionFilesIfNeeded(Logger logger) {
        if (!Files.exists(ROOT_DIR)) {
            return;
        }

        ArrayList<Path> legacyFiles = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ROOT_DIR)) {
            files.filter(Files::isRegularFile)
                .filter(path -> !path.startsWith(CONDITION_DIR))
                .filter(path -> !isCurrentConfigFile(path))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .forEach(legacyFiles::add);
        } catch (IOException e) {
            logger.warn("Failed to inspect PullUp legacy condition files under {}.", ROOT_DIR, e);
            return;
        }

        if (legacyFiles.isEmpty()) {
            return;
        }

        ensureConditionDir(logger);
        legacyFiles.sort(Comparator.comparing(path -> ROOT_DIR.relativize(path).toString()));

        for (Path legacyFile : legacyFiles) {
            Path relative = ROOT_DIR.relativize(legacyFile);
            Path migratedFile = CONDITION_DIR.resolve(relative);

            try {
                Files.createDirectories(migratedFile.getParent());
                Files.move(legacyFile, migratedFile, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Migrated PullUp condition set from {} to {}.", legacyFile, migratedFile);
            } catch (IOException e) {
                logger.warn("Failed to migrate PullUp condition set from {} to {}.", legacyFile, migratedFile, e);
            }
        }

        cleanupEmptyLegacyDirectories(logger);
    }

    private static boolean isCurrentConfigFile(Path path) {
        if (!path.equals(CONFIG_FILE)) {
            return false;
        }
        return !looksLikeConditionArray(path);
    }

    private static boolean looksLikeConditionArray(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            for (int index = 0; index < content.length(); index += 1) {
                char current = content.charAt(index);
                if (!Character.isWhitespace(current)) {
                    return current == '[';
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static void cleanupEmptyLegacyDirectories(Logger logger) {
        List<Path> directories;
        try (Stream<Path> stream = Files.walk(ROOT_DIR)) {
            directories = stream.filter(Files::isDirectory)
                .filter(path -> !path.equals(ROOT_DIR))
                .filter(path -> !path.startsWith(CONDITION_DIR))
                .sorted(Comparator.reverseOrder())
                .toList();
        } catch (IOException e) {
            logger.warn("Failed to inspect PullUp directories for cleanup under {}.", ROOT_DIR, e);
            return;
        }

        for (Path directory : directories) {
            try (Stream<Path> contents = Files.list(directory)) {
                if (contents.findAny().isEmpty()) {
                    Files.deleteIfExists(directory);
                }
            } catch (IOException e) {
                logger.warn("Failed to clean up empty PullUp directory {}.", directory, e);
            }
        }
    }
}
