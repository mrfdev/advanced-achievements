package com.hm.achievement;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class TestInstanceLauncher {

    private static final Logger LOGGER = Logger.getLogger("");
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path PLUGIN_PROJECT_DIR = Paths.get("").toAbsolutePath();
    private static final Path PLUGIN_TARGET_JAR = PLUGIN_PROJECT_DIR.resolve("advanced-achievements-plugin").resolve("target");
    private static final String PROJECT = "paper";
    private static final Long UNIX_TIME = System.currentTimeMillis() / 1000L;

    public static void main(String[] args) throws Exception {
        LOGGER.setLevel(Level.ALL);
        Path tempServerDir = Files.createTempDirectory("mc-server-" + UNIX_TIME);
        LOGGER.info("Temp server directory: " + tempServerDir);
        Path pluginDir = tempServerDir.resolve("plugins");
        Files.createDirectories(pluginDir);
        buildPlugin();
        Path pluginJar = findPluginJar(PLUGIN_TARGET_JAR);
        if (pluginJar == null) {
            throw new RuntimeException("Could not find plugin JAR");
        }
        LOGGER.info("Found plugin JAR: " + pluginJar);
        String mcVersion = fetchLatestVersion();
        int build = fetchLatestStableBuild(mcVersion);
        Path paperJar = downloadPaper(mcVersion, build, tempServerDir);
        LOGGER.info("Copying plugin jar to server plugins folder " + pluginDir.toAbsolutePath());
        Files.copy(pluginJar, pluginDir.resolve(pluginJar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        for (Map.Entry<String, String> entry : PLUGINS_TO_DOWNLOAD.entrySet()) {
            String pluginFileName = entry.getKey();
            String pluginUrl = entry.getValue();
            try {
                downloadAndAddPlugin(pluginUrl, pluginFileName, pluginDir);
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Failed to download plugin " + pluginFileName + " from " + pluginUrl, e);
                throw new RuntimeException(e);
            }
        }
        Path eulaFile = tempServerDir.resolve("eula.txt");
        Files.writeString(eulaFile, "eula=true\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        ProcessBuilder serverBuilder = new ProcessBuilder("java", "-jar", paperJar.toString(), "nogui");
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
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading output", e);
            }
        });
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
                        serverWriter.write(inputLine + "\n");
                        serverWriter.flush();
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error forwarding console input to server", e);
            }
        });
        consoleInputThread.setDaemon(true);
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
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error stopping server", e);
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
        LOGGER.info("Server excited with code: " + exitCode);
        System.exit(exitCode);
    }

    public static void buildPlugin() throws Exception {
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

    public static Path findPluginJar(Path targetDir) throws IOException {
        try (Stream<Path> files = Files.list(targetDir)) {
            return files.filter(f -> f.getFileName().toString().endsWith(".jar")).findFirst().orElse(null);
        }
    }

    public static String fetchLatestVersion() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.papermc.io/v2/projects/" + PROJECT)).build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = MAPPER.readTree(res.body());
        JsonNode versions = root.get("versions");
        String latestVersion = versions.get(versions.size() - 1).asText();
        LOGGER.info("Latest Version " + latestVersion);
        return latestVersion;
    }

    public static int fetchLatestStableBuild(String version) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.papermc.io/v2/projects/" + PROJECT + "/versions/" + version + "/builds")).build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode builds = MAPPER.readTree(res.body()).get("builds");
        if (builds == null || !builds.isArray() || builds.isEmpty()) {
            throw new RuntimeException("No builds found for version " + version);
        }
        int maxBuild = -1;
        for (JsonNode build : builds) {
            int buildNum = build.get("build").asInt();
            if (buildNum > maxBuild) maxBuild = buildNum;
        }
        if (maxBuild < 0) {
            throw new RuntimeException("No valid builds found for version " + version);
        }
        LOGGER.info("Highest build for version " + version + " is " + maxBuild);
        return maxBuild;
    }

    public static @NotNull Path downloadPaper(String version, int build, @NotNull Path dir) throws IOException, InterruptedException {
        String fileName = PROJECT + "-" + version + "-" + build + ".jar";
        Path jarPath = dir.resolve(fileName);
        if (!Files.exists(jarPath)) {
            String url = "https://api.papermc.io/v2/projects/" + PROJECT + "/versions/" + version + "/builds/" + build + "/downloads/" + fileName;
            LOGGER.info("Download paper .jar from " + url);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HTTP.send(req, HttpResponse.BodyHandlers.ofFile(jarPath));
            LOGGER.info("Downloaded paper .jar to " + jarPath);
        } else {
            LOGGER.info("Paper .jar already exists " + jarPath);
        }
        return jarPath;
    }

    public static void downloadAndAddPlugin(String pluginUrl, String pluginFileName, @NotNull Path pluginDir) throws IOException, InterruptedException {
        Path pluginPath = pluginDir.resolve(pluginFileName);
        if (!Files.exists(pluginPath)) {
            LOGGER.info("Downloading plugin " + pluginFileName + " from " + pluginUrl);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(pluginUrl)).build();
            HTTP.send(req, HttpResponse.BodyHandlers.ofFile(pluginPath));
            LOGGER.info("Downloaded " + pluginFileName + " to " + pluginPath);
        } else {
            LOGGER.info(pluginFileName + "already exists in plugins folder");
        }
    }

    public static final Map<String, String> PLUGINS_TO_DOWNLOAD = new LinkedHashMap<>() {{
        put("mcMMO.jar", "https://ci.mcmmo.org/job/mcMMO/job/mcMMO/lastSuccessfulBuild/artifact/target/mcMMO.jar");
    }};

    public static void deleteDirectoryRecursive(Path path) throws IOException {
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

    public static void openFolder(Path folder) throws IOException {
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
}