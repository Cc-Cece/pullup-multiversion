package cool.muyucloud.pullup.cloud;

public final class CloudImportState {
    private String lastImportedCode = "";
    private String lastImportedSetName = "";
    private String lastImportedJson = "";
    private boolean requiresCustomResourcePack;
    private String resourcePackMode = "";
    private String resourcePackUrl = "";
    private boolean temporary = true;
    private String localFile = "";

    public synchronized void remember(
        String code,
        String setName,
        String json,
        boolean requiresCustomResourcePack,
        String resourcePackMode,
        String resourcePackUrl,
        boolean temporary,
        String localFile
    ) {
        this.lastImportedCode = blankToEmpty(code);
        this.lastImportedSetName = blankToEmpty(setName);
        this.lastImportedJson = blankToEmpty(json);
        this.requiresCustomResourcePack = requiresCustomResourcePack;
        this.resourcePackMode = blankToEmpty(resourcePackMode);
        this.resourcePackUrl = blankToEmpty(resourcePackUrl);
        this.temporary = temporary;
        this.localFile = blankToEmpty(localFile);
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(
            this.lastImportedCode,
            this.lastImportedSetName,
            this.lastImportedJson,
            this.requiresCustomResourcePack,
            this.resourcePackMode,
            this.resourcePackUrl,
            this.temporary,
            this.localFile
        );
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static final class Snapshot {
        private final String code;
        private final String setName;
        private final String json;
        private final boolean requiresCustomResourcePack;
        private final String resourcePackMode;
        private final String resourcePackUrl;
        private final boolean temporary;
        private final String localFile;

        private Snapshot(
            String code,
            String setName,
            String json,
            boolean requiresCustomResourcePack,
            String resourcePackMode,
            String resourcePackUrl,
            boolean temporary,
            String localFile
        ) {
            this.code = code;
            this.setName = setName;
            this.json = json;
            this.requiresCustomResourcePack = requiresCustomResourcePack;
            this.resourcePackMode = resourcePackMode;
            this.resourcePackUrl = resourcePackUrl;
            this.temporary = temporary;
            this.localFile = localFile;
        }

        public boolean hasImportedPack() {
            return !this.code.isBlank() && !this.json.isBlank();
        }

        public String getCode() {
            return this.code;
        }

        public String getSetName() {
            return this.setName;
        }

        public String getJson() {
            return this.json;
        }

        public boolean requiresCustomResourcePack() {
            return this.requiresCustomResourcePack;
        }

        public String getResourcePackMode() {
            return this.resourcePackMode;
        }

        public String getResourcePackUrl() {
            return this.resourcePackUrl;
        }

        public boolean isTemporary() {
            return this.temporary;
        }

        public String getLocalFile() {
            return this.localFile;
        }
    }
}
