package cool.muyucloud.pullup.cloud;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.PullupPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CloudLocalFiles {
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("[a-z0-9][a-z0-9._-]*");
    private static final Path CONDITION_DIR = PullupPaths.getConditionDir();

    private CloudLocalFiles() {}

    public static SaveResult saveImportedPack(String requestedFileName, String importedCode, String json) throws IOException {
        PullupPaths.migrateLegacyConditionFilesIfNeeded(Pullup.getLogger());
        String safeName = sanitizeFileName(requestedFileName, importedCode);
        Files.createDirectories(CONDITION_DIR);
        Path output = CONDITION_DIR.resolve(safeName);
        Files.writeString(output, json, StandardCharsets.UTF_8);
        return new SaveResult(output, safeName);
    }

    public static String defaultImportedFileName(String importedCode) {
        String normalizedCode = importedCode == null ? "cloud" : importedCode.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
        if (normalizedCode.isBlank()) {
            normalizedCode = "cloud";
        }
        return "cloud-" + normalizedCode + ".json";
    }

    private static String sanitizeFileName(String requestedFileName, String importedCode) {
        String candidate = requestedFileName == null || requestedFileName.isBlank()
            ? defaultImportedFileName(importedCode)
            : requestedFileName.trim().toLowerCase(Locale.ROOT);

        candidate = candidate.replace('\\', '/');
        if (candidate.contains("/") || candidate.contains("..")) {
            throw new IllegalArgumentException("Imported file name must be a plain JSON file name.");
        }
        if (!candidate.endsWith(".json")) {
            candidate = candidate + ".json";
        }
        if (!SAFE_FILE_NAME.matcher(candidate).matches()) {
            throw new IllegalArgumentException("Imported file name may only contain lowercase letters, digits, dots, underscores, and dashes.");
        }
        return candidate;
    }

    public static final class SaveResult {
        private final Path absolutePath;
        private final String configRelativeName;

        private SaveResult(Path absolutePath, String configRelativeName) {
            this.absolutePath = absolutePath;
            this.configRelativeName = configRelativeName;
        }

        public Path getAbsolutePath() {
            return this.absolutePath;
        }

        public String getConfigRelativeName() {
            return this.configRelativeName;
        }
    }
}
