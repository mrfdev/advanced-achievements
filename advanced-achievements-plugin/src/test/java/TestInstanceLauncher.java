import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class TestInstanceLauncher {
    @SuppressWarnings("EmptyClassInitializer")
    public static final Map<String, PluginInfo> PLUGINS_TO_DOWNLOAD = new LinkedHashMap<>() {{
    }};
    private static final Logger LOGGER = Logger.getLogger("");
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path PLUGIN_PROJECT_DIR = Paths.get("").toAbsolutePath();
    private static final Path PLUGIN_TARGET_JAR = PLUGIN_PROJECT_DIR.resolve("advanced-achievements-plugin").resolve("target");
    private static final Path CONFIG_YML = PLUGIN_PROJECT_DIR.resolve("advanced-achievements-plugin").resolve("src").resolve("main").resolve("resources").resolve("config.yml");
    private static final Long UNIX_TIME = System.currentTimeMillis() / 1000L;
    private static String originalConfigSettings = null;

    static void main() throws IOException, InterruptedException, ExecutionException {
        LOGGER.setLevel(Level.ALL);
        Path tempServerDir = Files.createTempDirectory("mc-server-" + UNIX_TIME);
        LOGGER.info("Temp server directory: " + tempServerDir);
        Path pluginDir = tempServerDir.resolve("plugins");
        Files.createDirectories(pluginDir);
        setRestrictCreative();
        addDisabledCategories(List.of("JobsReborn"));
        String mcVersion = fetchLatestVersion();
        int build = fetchLatestStableBuild(mcVersion);
        CompletableFuture<Void> packageFuture = CompletableFuture.runAsync(() -> {
            try {
                packagePlugin();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to package plugin", e);
            }
        });
        CompletableFuture<Path> paperDownloadFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return downloadPaper(mcVersion, build, tempServerDir);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to download paper", e);
            }
        });
        CompletableFuture.allOf(packageFuture, paperDownloadFuture).join();
        Path pluginJar = findPluginJar();
        if (pluginJar == null) {
            throw new RuntimeException("Could not find plugin JAR after packaging");
        }
        LOGGER.info("Found plugin JAR: " + pluginJar);
        Path paperJar = paperDownloadFuture.get();
        LOGGER.info("Copying plugin jar to server plugins folder " + pluginDir.toAbsolutePath());
        Files.copy(pluginJar, pluginDir.resolve(pluginJar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        for (Map.Entry<String, PluginInfo> entry : PLUGINS_TO_DOWNLOAD.entrySet()) {
            String pluginFileName = entry.getKey();
            PluginInfo pluginInfo = entry.getValue();
            try {
                downloadAndAddPlugin(pluginInfo, pluginFileName, pluginDir);
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Failed to download plugin " + pluginFileName + " from " + pluginInfo.url(), e);
                throw new RuntimeException(e);
            }
        }
        Path eulaFile = tempServerDir.resolve("eula.txt");
        Files.writeString(eulaFile, "eula=true\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        ProcessBuilder serverBuilder = new ProcessBuilder("java", "-Xmx4G", "-XX:+UseZGC", "-jar", paperJar.toString(), "nogui");
        serverBuilder.directory(tempServerDir.toFile());
        serverBuilder.redirectErrorStream(true);
        Process serverProcess = serverBuilder.start();
        Thread serverOutputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()))) {
                String line;
                boolean launched = false;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (!launched && line.contains("Done (")) {
                        System.out.println("Server ready");
                        writer.write("op Greymagic27\n");
                        writer.flush();
                        launched = true;
                    }
                    if (line.contains("left the game")) {
                        try {
                            writer.write("stop\n");
                            writer.flush();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to send stop command to server", e);
                        }
                        break;
                    }
                    if (line.contains("joined the game")) {
                        try {
                            writer.write("gamemode creative Greymagic27\n");
                            writer.flush();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to send gamemode command to server", e);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading output", e);
            }
        });
        Thread consoleInputThread = getThread(serverProcess, tempServerDir);
        consoleInputThread.start();
        serverOutputThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Shutdown triggered. Stopping server");
                if (serverProcess.isAlive()) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()))) {
                        writer.write("stop\n");
                        writer.flush();
                    }
                    serverProcess.waitFor(30, TimeUnit.SECONDS);
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Error stopping server", e);
            }

            if (originalConfigSettings != null) {
                try {
                    Files.writeString(CONFIG_YML, originalConfigSettings);
                    LOGGER.info("Restored original config.yml settings");
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to restore original config.yml", e);
                }
            }

            try {
                LOGGER.info("Cleaning up temporary server files...");
                deleteDirectoryRecursive(tempServerDir);
                LOGGER.info("Cleanup complete.");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to cleanup temp directory", e);
            }
        }));
        int exitCode = serverProcess.waitFor();
        LOGGER.info("Server exited with code: " + exitCode);
        System.exit(exitCode);
    }

    private static @NonNull Thread getThread(Process serverProcess, Path tempServerDir) {
        Thread consoleInputThread = new Thread(() -> {
            try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in)); BufferedWriter serverWriter = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()))) {
                String inputLine;
                while ((inputLine = consoleReader.readLine()) != null) {
                    if ("m".equalsIgnoreCase(inputLine.trim())) {
                        System.out.println("Opening server directory " + tempServerDir);
                        try {
                            openFolder(tempServerDir);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to open server directory", e);
                        }
                    } else {
                        serverWriter.write(inputLine + System.lineSeparator());
                        serverWriter.flush();
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error forwarding console input to server", e);
            }
        });
        consoleInputThread.setDaemon(true);
        return consoleInputThread;
    }

    private static void packagePlugin() throws IOException, InterruptedException {
        LOGGER.info("Running mvn clean package");
        String mvnCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        ProcessBuilder mvnBuilder = new ProcessBuilder(mvnCommand, "clean", "package");
        mvnBuilder.directory(PLUGIN_PROJECT_DIR.toFile());
        mvnBuilder.inheritIO();
        Process mvnProcess = mvnBuilder.start();
        boolean completed = mvnProcess.waitFor(2, TimeUnit.MINUTES);
        if (!completed || mvnProcess.exitValue() != 0) {
            throw new RuntimeException("Failed to package plugin with maven");
        }
        LOGGER.info("Plugin packaged");
    }

    private static Path findPluginJar() throws IOException {
        try (Stream<Path> files = Files.list(TestInstanceLauncher.PLUGIN_TARGET_JAR)) {
            return files.filter(f -> f.getFileName().toString().endsWith(".jar")).findFirst().orElse(null);
        }
    }

    private static String fetchLatestVersion() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://fill.papermc.io/v3/projects/paper")).build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(res.body());
        JsonNode versions = root.get("versions");
        if (versions == null || !versions.isObject()) throw new RuntimeException("Invalid JSON Response: 'versions' is missing or is not an object");
        List<String> majorVersions = new ArrayList<>();
        versions.fieldNames().forEachRemaining(v -> {
            if (isStableVersion(v)) majorVersions.add(v);
        });
        if (majorVersions.isEmpty()) throw new RuntimeException("No major versions found");
        majorVersions.sort((v1, v2) -> compareVersions(v2, v1));
        JsonNode patchVersions = versions.get(majorVersions.getFirst());
        List<String> stablePatches = new ArrayList<>();
        for (JsonNode n : patchVersions) {
            String v = n.asText();
            if (isStableVersion(v)) stablePatches.add(v);
        }
        if (stablePatches.isEmpty()) throw new RuntimeException("No stable patch versions found for " + majorVersions.getFirst());
        stablePatches.sort((v1, v2) -> compareVersions(v2, v1));
        String latestVersion = stablePatches.getFirst();
        LOGGER.info("Latest Version " + latestVersion);
        return latestVersion;
    }

    private static int fetchLatestStableBuild(String version) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://fill.papermc.io/v3/projects/paper/versions/" + version + "/builds")).build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode builds = MAPPER.readTree(res.body());
        if (builds == null || !builds.isArray() || builds.isEmpty()) throw new RuntimeException("No builds found for version " + version);
        int maxBuild = -1;
        for (JsonNode build : builds) {
            int buildNum = build.get("id").asInt();
            if (buildNum > maxBuild) maxBuild = buildNum;
        }
        if (maxBuild < 0) throw new RuntimeException("No valid builds found for version " + version);
        LOGGER.info("Highest build for version " + version + " is " + maxBuild);
        return maxBuild;
    }

    private static @NonNull Path downloadPaper(String version, int build, @NonNull Path dir) throws IOException, InterruptedException {
        String fileName = "paper" + "-" + version + "-" + build + ".jar";
        Path jarPath = dir.resolve(fileName);
        if (!Files.exists(jarPath)) {
            String url = "https://fill.papermc.io/v3/projects/paper/versions/" + version + "/builds/" + build;
            LOGGER.info("Fetching download metadata from " + url);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = MAPPER.readTree(res.body());
            JsonNode downloads = root.get("downloads");
            if (downloads == null || downloads.get("server:default") == null) throw new IOException("No download URL found for build " + build);
            String downloadUrl = downloads.get("server:default").get("url").asText();
            HttpRequest downloadReq = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build();
            HTTP.send(downloadReq, HttpResponse.BodyHandlers.ofFile(jarPath));
            LOGGER.info("Downloaded paper.jar to " + jarPath);
        } else {
            LOGGER.severe("Paper .jar already exists " + jarPath);
        }
        return jarPath;
    }

    private static void copyFolderRecursive(Path source, Path target) throws IOException {
        if (!Files.exists(source)) return;
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
                try {
                    Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to copy file " + sourcePath + " to " + target, e);
                }
            });
        }
    }

    @SuppressWarnings("HttpUrlsUsage")
    private static void downloadAndAddPlugin(@NonNull PluginInfo pluginInfo, String pluginFileName, @NonNull Path pluginDir) throws IOException, InterruptedException {
        String pluginUrl = pluginInfo.url();
        String configFolderPath = pluginInfo.config();
        Path pluginPath = pluginDir.resolve(pluginFileName);
        if (pluginUrl.startsWith("http://")) LOGGER.severe("Plugin URL must be secure (https)");
        if (pluginUrl.startsWith("https://")) {
            if (!Files.exists(pluginPath)) {
                LOGGER.info("Downloading plugin " + pluginFileName + " from " + pluginUrl);
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(pluginUrl)).build();
                HTTP.send(req, HttpResponse.BodyHandlers.ofFile(pluginPath));
                LOGGER.info("Downloaded " + pluginFileName + " to " + pluginPath);
            } else {
                LOGGER.info(pluginFileName + " already exists in plugins folder");
            }
        } else {
            Path localFile = Paths.get(pluginUrl);
            if (!Files.exists(localFile)) {
                LOGGER.warning("Local plugin file does not exist: " + localFile);
                return;
            }
            if (!Files.exists(pluginPath) || Files.size(pluginPath) != Files.size(localFile)) {
                LOGGER.info("Copying local plugin" + pluginFileName + " from " + localFile);
                Files.copy(localFile, pluginPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                LOGGER.info(pluginFileName + " already exists and matches local file");
            }
            if (configFolderPath != null && !configFolderPath.isEmpty()) {
                Path localPluginConfigFolder = Paths.get(configFolderPath);
                Path serverPluginConfigFolder = pluginDir.resolve(pluginFileName.replaceFirst("\\.jar$", ""));
                if (Files.exists(localPluginConfigFolder) && Files.isDirectory(localPluginConfigFolder)) {
                    LOGGER.info("Copying plugin config folder for " + pluginFileName);
                    copyFolderRecursive(localPluginConfigFolder, serverPluginConfigFolder);
                } else {
                    LOGGER.info("Config folder specified but does not exist " + localPluginConfigFolder);
                }
            }
        }
    }

    private static void deleteDirectoryRecursive(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to delete " + p, e);
                }
            });
        }
    }

    private static void openFolder(Path folder) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            new ProcessBuilder("explorer.exe", folder.toAbsolutePath().toString()).start();
        } else if (os.contains("mac")) {
            new ProcessBuilder("open", folder.toAbsolutePath().toString()).start();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            new ProcessBuilder("xdg-open", folder.toAbsolutePath().toString()).start();
        } else {
            throw new UnsupportedOperationException("Opening folder not supported on this OS");
        }
    }

    private static void setRestrictCreative() throws IOException {
        if (!Files.exists(TestInstanceLauncher.CONFIG_YML)) {
            throw new IOException("Config file not found: " + TestInstanceLauncher.CONFIG_YML);
        }
        var lines = Files.readAllLines(TestInstanceLauncher.CONFIG_YML);
        if (originalConfigSettings == null) {
            originalConfigSettings = Files.readString(TestInstanceLauncher.CONFIG_YML);
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("RestrictCreative:")) {
                lines.set(i, "RestrictCreative: " + false);
                break;
            }
        }
        Files.writeString(TestInstanceLauncher.CONFIG_YML, String.join(System.lineSeparator(), lines));
        LOGGER.info("Set RestrictCreative to " + false + " in " + TestInstanceLauncher.CONFIG_YML);
    }

    private static void addDisabledCategories(List<String> categoriesToAdd) throws IOException {
        if (!Files.exists(TestInstanceLauncher.CONFIG_YML)) {
            throw new IOException("Config file not found: " + TestInstanceLauncher.CONFIG_YML);
        }
        var lines = Files.readAllLines(TestInstanceLauncher.CONFIG_YML);
        if (originalConfigSettings == null) {
            originalConfigSettings = Files.readString(TestInstanceLauncher.CONFIG_YML);
        }
        boolean inDisabledCategories = false;
        int insertIndex = -1;
        Set<String> existingCategories = new HashSet<>();
        String indent = "  ";
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("DisabledCategories:")) {
                inDisabledCategories = true;
                insertIndex = i + 1;
                continue;
            }
            if (inDisabledCategories) {
                if (line.startsWith("- ")) {
                    String item = line.substring(2).trim();
                    existingCategories.add(item);
                } else if (line.startsWith(indent)) break;
            }
        }
        List<String> newLines = new ArrayList<>(lines);
        for (String category : categoriesToAdd) {
            if (!existingCategories.contains(category)) {
                newLines.add(insertIndex++, indent + "- " + category);
            }
        }
        Files.writeString(TestInstanceLauncher.CONFIG_YML, String.join(System.lineSeparator(), newLines));
        LOGGER.info("Added " + categoriesToAdd + " to DisabledCategories in " + TestInstanceLauncher.CONFIG_YML);
    }

    private static int compareVersions(@NonNull String v1, @NonNull String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }
        return 0;
    }

    @Contract(pure = true)
    private static boolean isStableVersion(@NonNull String version) {
        return !version.contains("-");
    }

    public record PluginInfo(String url, String config) {
    }
}