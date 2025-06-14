package com.hm.achievement.command.executable;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.FancyMessageSender;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("DataFlowIssue")
public class ResetConfigCommandTest {
    private AdvancedAchievements plugin;
    private CommandSender sender;
    private ResetConfigCommand command;
    private FancyMessageSender fancyMessageSender;
    private File tempFolder;

    @BeforeEach
    public void setUp() throws IOException {
        Logger logger = Logger.getLogger(ResetConfigCommand.class.getName());
        logger.setLevel(Level.OFF);
        plugin = mock(AdvancedAchievements.class);
        sender = mock(CommandSender.class);
        YamlConfiguration mainConfig = mock(YamlConfiguration.class);
        YamlConfiguration langConfig = mock(YamlConfiguration.class);
        StringBuilder pluginHeader = new StringBuilder();
        fancyMessageSender = mock(FancyMessageSender.class);
        tempFolder = Files.createTempDirectory("pluginData").toFile();
        command = new ResetConfigCommand(mainConfig, langConfig, pluginHeader, plugin, fancyMessageSender);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (tempFolder != null && tempFolder.exists()) {
            Path path = tempFolder.toPath();
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Test
    public void testResetConfigWithBackup() throws Exception {
        when(plugin.getDataFolder()).thenReturn(tempFolder);
        File configFile = new File(tempFolder, "config.yml");
        Files.writeString(configFile.toPath(), "dummy config content");
        InputStream defaultConfigStream = new ByteArrayInputStream("default config content".getBytes());
        when(plugin.getResource("config.yml")).thenReturn(defaultConfigStream);
        doNothing().when(plugin).reloadConfig();
        command.onExecute(sender, new String[]{"reset"});
        File backupFile = new File(tempFolder, "config_old.yml");
        assertTrue(backupFile.exists());
        verify(sender).sendMessage("Config.yml has been reset to default. Backup saved as config_old.yml");
        assertTrue(backupFile.delete(), "Failed to delete backupFile during cleanup");
        assertTrue(configFile.delete(), "Failed to delete configFile during cleanup");
        assertTrue(tempFolder.delete(), "Failed to delete tempFolder during cleanup");
    }

    @Test
    public void testResetConfigForceNoBackup() throws Exception {
        when(plugin.getDataFolder()).thenReturn(tempFolder);
        File configFile = new File(tempFolder, "config.yml");
        Files.writeString(configFile.toPath(), "dummy config content");
        InputStream defaultConfigStream = new ByteArrayInputStream("default config content".getBytes());
        when(plugin.getResource("config.yml")).thenReturn(defaultConfigStream);
        doNothing().when(plugin).reloadConfig();
        command.onExecute(sender, new String[]{"confirm"});
        File backupFile = new File(tempFolder, "config_old.yml");
        assertFalse(backupFile.exists());
        verify(sender).sendMessage("Warning: Skipping backup as 'confirm' flag was used.");
        verify(sender).sendMessage("Config.yml has been reset to default. Backup saved as config_old.yml");
        assertTrue(configFile.delete(), "Failed to delete configFile during cleanup");
        assertTrue(tempFolder.delete(), "Failed to delete tempFolder during cleanup");
    }

    @Test
    public void testResetConfigBackupFailurePromptsUserToConfirm() throws Exception {
        when(plugin.getDataFolder()).thenReturn(tempFolder);
        File configFile = new File(tempFolder, "config.yml");
        Files.writeString(configFile.toPath(), "dummy config content");
        File backupDir = new File(tempFolder, "config_old.yml");
        assertTrue(backupDir.mkdir());
        InputStream defaultConfigStream = new ByteArrayInputStream("default config content".getBytes());
        when(plugin.getResource("config.yml")).thenReturn(defaultConfigStream);
        doNothing().when(plugin).reloadConfig();
        ResetConfigCommand failingBackupCommand = new ResetConfigCommand(mock(YamlConfiguration.class), mock(YamlConfiguration.class), new StringBuilder(), plugin, fancyMessageSender) {
            @Override
            protected void backupConfig(@NotNull File source, @NotNull File target) throws IOException {
                throw new IOException("test");
            }
        };
        failingBackupCommand.onExecute(sender, new String[]{"reset"});
        verify(sender).sendMessage("Failed to backup current config.yml.");
        verify(sender).sendMessage("Run '/resetconfig confirm' to reset without backup.");
        assertTrue(configFile.exists());
    }
}