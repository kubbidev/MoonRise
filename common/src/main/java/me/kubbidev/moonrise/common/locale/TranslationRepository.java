package me.kubbidev.moonrise.common.locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.http.UnsuccessfulRequestException;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.LimitedInputStream;
import me.kubbidev.moonrise.common.util.gson.GsonProvider;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TranslationRepository {
    private static final String TRANSLATIONS_INFO_ENDPOINT = "https://metadata.kubbidev.me/moonrise/translations";
    private static final String TRANSLATIONS_DOWNLOAD_ENDPOINT = "https://metadata.kubbidev.me/moonrise/translation/";
    private static final long MAX_BUNDLE_SIZE = 0x100000L; // 1mb
    private static final long CACHE_MAX_AGE = TimeUnit.HOURS.toMillis(23);

    private final MoonRisePlugin plugin;

    public TranslationRepository(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets a list of available languages.
     *
     * @return a list of languages
     * @throws IOException if an i/o error occurs
     * @throws UnsuccessfulRequestException if the http request fails
     */
    public List<LanguageInfo> getAvailableLanguages() throws UnsuccessfulRequestException, IOException {
        return this.getTranslationsMetadata().languages;
    }

    /**
     * Schedules a refresh of the current translations if necessary.
     */
    public void scheduleRefresh() {
        if (!this.plugin.getConfiguration().get(ConfigKeys.AUTO_INSTALL_TRANSLATIONS)) {
            return; // skip
        }

        this.plugin.getBootstrap().getScheduler().executeAsync(() -> {
            // cleanup old translation files
            this.clearDirectory(this.plugin.getTranslationManager().getTranslationsDirectory(), Files::isRegularFile);

            try {
                this.refresh();
            } catch (Exception e) {
                // ignore
            }
        });
    }

    private void refresh() throws Exception {
        long lastRefresh = this.readLastRefreshTime();
        long timeSinceLastRefresh = System.currentTimeMillis() - lastRefresh;

        if (timeSinceLastRefresh <= CACHE_MAX_AGE) {
            return;
        }

        MetadataResponse metadata = this.getTranslationsMetadata();
        if (timeSinceLastRefresh <= metadata.cacheMaxAge) {
            return;
        }

        // perform a refresh!
        this.downloadAndInstallTranslations(metadata.languages, null, true);
    }

    private void clearDirectory(Path directory, Predicate<Path> predicate) {
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(predicate).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    // ignore
                }
            });
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Downloads and installs translations for the given languages.
     *
     * @param languages the languages to install translations for
     * @param sender the sender to report progress to
     * @param updateStatus if the status file should be updated
     */
    public void downloadAndInstallTranslations(List<LanguageInfo> languages, @Nullable Sender sender, boolean updateStatus) {
        TranslationManager manager = this.plugin.getTranslationManager();
        Path translationsDirectory = manager.getRepositoryTranslationsDirectory();

        // clear existing translations
        this.clearDirectory(translationsDirectory, TranslationManager::isTranslationFile);

        for (LanguageInfo language : languages) {
            if (sender != null) {
                Message.TRANSLATIONS_INSTALLING_SPECIFIC.send(sender, language.locale().toString());
            }
            this.downloadAndInstallTranslation(language, translationsDirectory, sender);
        }

        if (updateStatus) {
            this.writeLastRefreshTime();
        }

        manager.reload();
    }

    private void downloadAndInstallTranslation(LanguageInfo language, Path translationsDirectory, @Nullable Sender sender) {
        Path file = translationsDirectory.resolve(language.locale().toString() + ".properties");

        Request request = new Request.Builder()
                .header("User-Agent", this.plugin.getBytebin().getUserAgent())
                .url(TRANSLATIONS_DOWNLOAD_ENDPOINT + language.id())
                .build();

        try (Response response = this.plugin.getBytebin().makeHttpRequest(request)) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new RuntimeException("No response");
                }

                try (InputStream in = new LimitedInputStream(responseBody.byteStream(), MAX_BUNDLE_SIZE)) {
                    Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (UnsuccessfulRequestException | IOException e) {
            if (sender != null) {
                Message.TRANSLATIONS_DOWNLOAD_ERROR.send(sender, language.locale().toString());
                this.plugin.getLogger().warn("Unable to download translations", e);
            }
        }
    }

    private void writeLastRefreshTime() {
        Path statusFile = this.plugin.getTranslationManager().getRepositoryStatusFile();

        try (BufferedWriter writer = Files.newBufferedWriter(statusFile, StandardCharsets.UTF_8)) {
            JsonObject status = new JsonObject();
            status.addProperty("lastRefresh", System.currentTimeMillis());
            GsonProvider.prettyPrinting().toJson(status, writer);
        } catch (IOException e) {
            // ignore
        }
    }

    private long readLastRefreshTime() {
        Path statusFile = this.plugin.getTranslationManager().getRepositoryStatusFile();

        if (Files.exists(statusFile)) {
            try (BufferedReader reader = Files.newBufferedReader(statusFile, StandardCharsets.UTF_8)) {
                JsonObject status = GsonProvider.normal().fromJson(reader, JsonObject.class);
                if (status.has("lastRefresh")) {
                    return status.get("lastRefresh").getAsLong();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return 0L;
    }

    private MetadataResponse getTranslationsMetadata() throws UnsuccessfulRequestException, IOException {
        JsonObject response = this.plugin.getBytebin().getJsonContent(TRANSLATIONS_INFO_ENDPOINT).getAsJsonObject();

        List<LanguageInfo> languages = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : response.get("languages").getAsJsonObject().entrySet()) {
            languages.add(new LanguageInfo(e.getKey(), e.getValue().getAsJsonObject()));
        }

        languages.removeIf(language -> language.progress() <= 0);
        if (languages.size() >= 100) {
            // just a precaution: if more than 100 languages have been
            // returned then the metadata server is doing something silly
            throw new IOException("More than 100 languages - cancelling download");
        }

        long cacheMaxAge = response.get("cacheMaxAge").getAsLong();
        return new MetadataResponse(cacheMaxAge, languages);
    }

    private record MetadataResponse(long cacheMaxAge, List<LanguageInfo> languages) {
    }

    public static final class LanguageInfo {
        private final String id;
        private final String name;
        private final Locale locale;
        private final int progress;
        private final List<String> contributors;

        LanguageInfo(String id, JsonObject data) {
            this.id = id;
            this.name = data.get("name").getAsString();
            this.locale = Objects.requireNonNull(TranslationManager.parseLocale(id));
            this.progress = data.get("progress").getAsInt();
            this.contributors = new ArrayList<>();
            for (JsonElement contributor : data.getAsJsonArray("contributors")) {
                this.contributors.add(contributor.getAsString());
            }
        }

        public String id() {
            return this.id;
        }

        public String name() {
            return this.name;
        }

        public Locale locale() {
            return this.locale;
        }

        public int progress() {
            return this.progress;
        }

        public List<String> contributors() {
            return this.contributors;
        }
    }
}
