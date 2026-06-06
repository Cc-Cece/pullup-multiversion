package cool.muyucloud.pullup.cloud;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.ConditionSetRuntime;

import java.io.IOException;
import java.util.List;

public final class CloudCommandSupport {
    private CloudCommandSupport() {}

    public static CloudApiModels.SessionCreateResponse createEditSession() throws CloudApiException {
        String sourceSetName = ConditionSetRuntime.readConfiguredSetName();
        String sourceJson = ConditionSetRuntime.readConfiguredSetContent();
        return CloudApiClient.fromConfig().createSession(sourceSetName, sourceJson);
    }

    public static ImportResult importPack(String code, boolean temporary) throws CloudApiException, IOException {
        CloudApiModels.PackResponse response = CloudApiClient.fromConfig().fetchPack(code);
        if (response == null) {
            throw new CloudApiException("INVALID_RESPONSE", "The PullUp cloud service returned an empty response.");
        }
        if (response.schemaVersion != CloudApiModels.SUPPORTED_SCHEMA_VERSION) {
            throw new CloudApiException(
                "SCHEMA_VERSION_UNSUPPORTED",
                "Expected schema version " + CloudApiModels.SUPPORTED_SCHEMA_VERSION + " but received " + response.schemaVersion + "."
            );
        }
        if (response.json == null || response.json.isBlank()) {
            throw new CloudApiException("INVALID_RESPONSE", "The PullUp cloud service returned an empty condition set.");
        }

        String importedSetName = response.setName == null || response.setName.isBlank()
            ? "cloud-" + code.toLowerCase()
            : response.setName;
        CloudLocalFiles.SaveResult saveResult = null;
        if (temporary) {
            if (!ConditionSetRuntime.applySerializedSet(importedSetName, response.json)) {
                throw new CloudApiException("APPLY_FAILED", "The imported condition set could not be applied in-game.");
            }
        } else {
            saveResult = CloudLocalFiles.saveImportedPack("", response.code, response.json);
            if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(saveResult.getConfigRelativeName()))) {
                throw new CloudApiException("APPLY_FAILED", "The imported condition set was saved, but could not be reloaded from disk.");
            }
        }

        Pullup.getCloudImportState().remember(
            response.code,
            importedSetName,
            response.json,
            response.requiresCustomResourcePack,
            response.resourcePackMode,
            response.resourcePackUrl,
            temporary,
            saveResult == null ? "" : saveResult.getConfigRelativeName()
        );

        return new ImportResult(
            response.code,
            importedSetName,
            response.requiresCustomResourcePack,
            response.resourcePackMode,
            response.resourcePackUrl,
            temporary,
            saveResult
        );
    }

    public static ServerImportResult importPackToServer(String code, boolean temporary) throws CloudApiException, IOException {
        CloudApiModels.PackResponse response = CloudApiClient.fromConfig().fetchPack(code);
        if (response == null) {
            throw new CloudApiException("INVALID_RESPONSE", "The PullUp cloud service returned an empty response.");
        }
        if (response.schemaVersion != CloudApiModels.SUPPORTED_SCHEMA_VERSION) {
            throw new CloudApiException(
                "SCHEMA_VERSION_UNSUPPORTED",
                "Expected schema version " + CloudApiModels.SUPPORTED_SCHEMA_VERSION + " but received " + response.schemaVersion + "."
            );
        }
        if (response.json == null || response.json.isBlank()) {
            throw new CloudApiException("INVALID_RESPONSE", "The PullUp cloud service returned an empty condition set.");
        }

        String importedSetName = response.setName == null || response.setName.isBlank()
            ? "cloud-" + code.toLowerCase()
            : response.setName;
        CloudLocalFiles.SaveResult saveResult = null;
        if (temporary) {
            if (!ConditionSetRuntime.applySerializedSet(importedSetName, response.json)) {
                throw new CloudApiException("APPLY_FAILED", "The imported condition set could not be applied on the server.");
            }
        } else {
            saveResult = CloudLocalFiles.saveImportedPack("", response.code, response.json);
            if (!ConditionSetRuntime.saveEnabledSetsAndLoad(List.of(saveResult.getConfigRelativeName()))) {
                throw new CloudApiException("APPLY_FAILED", "The imported condition set was saved, but could not be reloaded on the server.");
            }
        }

        Pullup.getCloudImportState().remember(
            response.code,
            importedSetName,
            response.json,
            response.requiresCustomResourcePack,
            response.resourcePackMode,
            response.resourcePackUrl,
            temporary,
            saveResult == null ? "" : saveResult.getConfigRelativeName()
        );

        return new ServerImportResult(
            response.code,
            importedSetName,
            saveResult,
            response.requiresCustomResourcePack,
            response.resourcePackMode,
            response.resourcePackUrl,
            temporary
        );
    }

    public static CloudImportState.Snapshot readSnapshot() {
        return Pullup.getCloudImportState().snapshot();
    }

    public static final class ImportResult {
        private final String code;
        private final String setName;
        private final boolean requiresCustomResourcePack;
        private final String resourcePackMode;
        private final String resourcePackUrl;
        private final boolean temporary;
        private final CloudLocalFiles.SaveResult saveResult;

        private ImportResult(
            String code,
            String setName,
            boolean requiresCustomResourcePack,
            String resourcePackMode,
            String resourcePackUrl,
            boolean temporary,
            CloudLocalFiles.SaveResult saveResult
        ) {
            this.code = code;
            this.setName = setName;
            this.requiresCustomResourcePack = requiresCustomResourcePack;
            this.resourcePackMode = resourcePackMode == null ? "" : resourcePackMode;
            this.resourcePackUrl = resourcePackUrl == null ? "" : resourcePackUrl;
            this.temporary = temporary;
            this.saveResult = saveResult;
        }

        public String getCode() {
            return this.code;
        }

        public String getSetName() {
            return this.setName;
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

        public CloudLocalFiles.SaveResult getSaveResult() {
            return this.saveResult;
        }
    }

    public static final class ServerImportResult {
        private final String code;
        private final String setName;
        private final CloudLocalFiles.SaveResult saveResult;
        private final boolean requiresCustomResourcePack;
        private final String resourcePackMode;
        private final String resourcePackUrl;
        private final boolean temporary;

        private ServerImportResult(
            String code,
            String setName,
            CloudLocalFiles.SaveResult saveResult,
            boolean requiresCustomResourcePack,
            String resourcePackMode,
            String resourcePackUrl,
            boolean temporary
        ) {
            this.code = code;
            this.setName = setName;
            this.saveResult = saveResult;
            this.requiresCustomResourcePack = requiresCustomResourcePack;
            this.resourcePackMode = resourcePackMode == null ? "" : resourcePackMode;
            this.resourcePackUrl = resourcePackUrl == null ? "" : resourcePackUrl;
            this.temporary = temporary;
        }

        public String getCode() {
            return this.code;
        }

        public String getSetName() {
            return this.setName;
        }

        public CloudLocalFiles.SaveResult getSaveResult() {
            return this.saveResult;
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
    }
}
