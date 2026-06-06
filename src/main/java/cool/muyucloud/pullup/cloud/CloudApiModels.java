package cool.muyucloud.pullup.cloud;

public final class CloudApiModels {
    public static final int SUPPORTED_SCHEMA_VERSION = 1;
    public static final int SUPPORTED_PROTOCOL_VERSION = 1;
    public static final String RESOURCE_PACK_MODE_BROWSER_LOCAL = "browser-local";

    private CloudApiModels() {}

    public static final class SessionCreateRequest {
        public String client;
        public String modVersion;
        public String minecraftVersion;
        public String loader;
        public String language;
        public int schemaVersion;
        public String sourceSetName;
        public String sourceJson;
        public ClientContext clientContext;
    }

    public static final class SessionCreateResponse {
        public String sessionId;
        public String editUrl;
        public String expiresAt;
    }

    public static final class PackResponse {
        public String code;
        public int schemaVersion;
        public String setName;
        public String json;
        public boolean requiresCustomResourcePack;
        public String resourcePackMode;
        public String resourcePackUrl;
        public String previewUrl;
    }

    public static final class ErrorEnvelope {
        public ErrorBody error;
    }

    public static final class ErrorBody {
        public String code;
        public String message;
    }

    public static final class ClientContext {
        public int protocolVersion;
        public boolean isSnapshot;
        public Integer resourcePackFormat;
        public ResourcePackFormatRange resourcePackFormatRange;
        public ClientCapabilities capabilities;
        public java.util.List<DiscoverableSound> discoverableSounds;
    }

    public static final class ResourcePackFormatRange {
        public Integer min;
        public Integer max;
    }

    public static final class ClientCapabilities {
        public boolean supportsCustomSounds;
        public boolean supportsCloudImport;
        public boolean supportsResourcePackMinMaxFormat;
    }

    public static final class DiscoverableSound {
        public String id;
        public String source;
    }
}
