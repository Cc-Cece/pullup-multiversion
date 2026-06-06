package cool.muyucloud.pullup.cloud;

public final class CloudApiException extends Exception {
    private final String code;

    public CloudApiException(String message) {
        this("INTERNAL_ERROR", message);
    }

    public CloudApiException(String code, String message) {
        super(message);
        this.code = code == null || code.isBlank() ? "INTERNAL_ERROR" : code;
    }

    public String getCode() {
        return this.code;
    }
}
