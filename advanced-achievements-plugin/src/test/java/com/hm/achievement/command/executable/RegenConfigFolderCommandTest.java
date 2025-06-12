package com.hm.achievement.command.executable;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.FancyMessageSender;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegenConfigFolderCommandTest {

    @Mock
    YamlConfiguration mainConfig;

    @Mock
    YamlConfiguration langConfig;

    @Mock
    FancyMessageSender fancyMessageSender;

    @Mock
    AdvancedAchievements plugin;

    @Mock
    CommandSender sender;

    RegenConfigFolderCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new RegenConfigFolderCommand(mainConfig, langConfig, new StringBuilder(), plugin, fancyMessageSender);
        Logger logger = Logger.getLogger(RegenConfigFolderCommand.class.getName());
        logger.setLevel(Level.OFF);
    }

    @Test
    void testOnExecuteWithoutConfirm() {
        command.onExecute(sender, new String[0]);
        verify(sender).sendMessage(contains("This will delete the entire config folder"));
        verify(sender).sendMessage(contains("Type /resetconfig confirm"));
    }

    @Test
    void testOnExecuteFolderNotFound() {
        when(plugin.getDataFolder()).thenReturn(new File("nonexistent"));
        command.onExecute(sender, new String[]{"confirm"});
        verify(sender).sendMessage(contains("Config folder not found"));
    }

    @Test
    void testOnExecuteDeleteAndReload() throws Exception {
        Path tempFolder = Files.createTempDirectory("pluginConfigTest");
        File folder = tempFolder.toFile();
        folder.deleteOnExit();
        when(plugin.getDataFolder()).thenReturn(folder);
        command.onExecute(sender, new String[]{"confirm"});
        verify(plugin).onDisable();
        verify(plugin).onEnable();
        verify(sender).sendMessage(contains("Config folder deleted"));
        verify(sender).sendMessage(contains("Config folder regenerated"));
        assertFalse(Files.exists(tempFolder), "Folder should be deleted");
    }

    @Test
    void testOnExecuteReloadFails() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.toPath()).thenReturn(Path.of("mock"));
        when(plugin.getDataFolder()).thenReturn(mockFile);
        doThrow(new RuntimeException()).when(plugin).onDisable();
        command.onExecute(sender, new String[]{"confirm"});
        verify(sender).sendMessage(contains("Config folder deleted"));
        verify(sender).sendMessage(contains("Error during plugin reload"));
    }
}