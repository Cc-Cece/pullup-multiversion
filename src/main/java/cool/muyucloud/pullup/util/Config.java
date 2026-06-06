package cool.muyucloud.pullup.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cool.muyucloud.pullup.Pullup;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class Config {
    private static final Logger LOGGER = Pullup.getLogger();
    private static final Path PATH = PullupPaths.getConfigFile();

    private final JsonObject properties = new JsonObject();

    public Config() {
        this.properties.addProperty("enable", true);
        this.properties.addProperty("loadServer", true);
        this.properties.addProperty("sendServer", true);
        this.properties.addProperty("maxDistance", 500);
        this.properties.addProperty("sendDelay", 50);
        JsonArray enabledSets = new JsonArray();
        enabledSets.add("default");
        this.properties.add("enabledSets", enabledSets);
        this.properties.addProperty("hudTextDisplayX", 0.65f);
        this.properties.addProperty("hudTextDisplayY", 0.6f);
        this.properties.addProperty("cloudBaseUrl", "https://pullup.akihito.dpdns.org");
    }

    public String getAsString(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsString();
    }

    public boolean getAsBool(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsBoolean();
    }

    public int getAsInt(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsInt();
    }

    public float getAsFloat(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsFloat();
    }

    public List<String> getAsStringList(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        ArrayList<String> values = new ArrayList<>();
        for (var element : this.properties.getAsJsonArray(key)) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    public void set(String key, String value) {
        this.properties.addProperty(key, value);
    }

    public void set(String key, boolean value) {
        this.properties.addProperty(key, value);
    }

    public void set(String key, Collection<String> values) {
        JsonArray array = new JsonArray();
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                unique.add(trimmed);
            }
        }
        for (String value : unique) {
            array.add(value);
        }
        this.properties.add(key, array);
    }

    public void loadAndCorrect() {
        PullupPaths.migrateLegacyConditionFilesIfNeeded(LOGGER);
        PullupPaths.migrateLegacyConfigIfNeeded(LOGGER);
        if (!Files.exists(PATH)) {
            // try to create new config file
            LOGGER.info("PullUp config does not exist at {}, generating.", PATH);
            this.save();
            return;
        }
        this.readFile();
    }

    private void readFile() {
        try (InputStream inputStream = Files.newInputStream(PATH)) {
            JsonObject read = (new Gson()).fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

            // Analyzing properties
            for (String key : read.keySet()) {
                if (this.properties.has(key)) {
                    if (this.properties.get(key).isJsonArray()) {
                        if (read.get(key).isJsonArray()) {
                            ArrayList<String> values = new ArrayList<>();
                            for (var element : read.getAsJsonArray(key)) {
                                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                                    values.add(element.getAsString());
                                }
                            }
                            this.set(key, values);
                        }
                        continue;
                    }

                    JsonPrimitive dst = this.properties.getAsJsonPrimitive(key);
                    JsonPrimitive src = read.get(key).getAsJsonPrimitive();

                    try {
                        if (dst.isBoolean()) {
                            this.properties.addProperty(key, src.getAsBoolean());
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Problems occurred during analyzing property %s.".formatted(key));
                    }
                    this.properties.addProperty(key, src.getAsString());
                }
            }

            if (!read.has("enabledSets") && read.has("loadSet")) {
                this.set("enabledSets", List.of(read.get("loadSet").getAsString()));
            }
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during reading config file.");
        }
    }

    private void verifyOrGenFile() {
        PullupPaths.ensureRootDir(LOGGER);
        if (!Files.exists(PATH)) {
            // try to create new config file
            LOGGER.info("PullUp config does not exist at {}, generating.", PATH);
            this.genFile();
        }
    }

    private void genFile() {
        try {
            Files.createFile(PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to generate config file at %s.".formatted(PATH), e);
        }
    }

    public void save() {
        String json = (new GsonBuilder().setPrettyPrinting().create()).toJson(this.properties);
        this.verifyOrGenFile();
        this.writeFile(json);
    }

    private void writeFile(String json) {
        try (OutputStream outputStream = Files.newOutputStream(PATH)) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during writing config file.");
        }
    }
}
