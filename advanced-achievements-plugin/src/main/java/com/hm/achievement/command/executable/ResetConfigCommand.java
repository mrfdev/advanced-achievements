package com.hm.achievement.command.executable;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.FancyMessageSender;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandSpec(name = "resetconfig", permission = "achievement.resetconfig", minArgs = 1, maxArgs = 1)
public class ResetConfigCommand extends AbstractCommand {
    private final AdvancedAchievements plugin;
    private final FancyMessageSender fancyMessageSender;
    private final Logger LOGGER = Logger.getLogger(ResetConfigCommand.class.getName());

    @Inject
    public ResetConfigCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, AdvancedAchievements plugin, FancyMessageSender fancyMessageSender) {
        super(mainConfig, langConfig, pluginHeader);
        this.plugin = plugin;
        this.fancyMessageSender = fancyMessageSender;
    }

    protected void backupConfig(@NotNull File source, @NotNull File target) throws IOException {
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    void onExecute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        boolean forceReset = args.length > 0 && args[0].equalsIgnoreCase("confirm");
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backupFile = new File(plugin.getDataFolder(), "config_old.yml");
        if (configFile.exists()) {
            if (!forceReset) {
                try {
                    backupConfig(configFile, backupFile);
                } catch (IOException e) {
                    sendMessage(sender, "Failed to backup current config.yml.");
                    sendMessage(sender, "Run '/resetconfig confirm' to reset without backup.");
                    LOGGER.warning("Failed to backup config: " + e.getMessage());
                    return;
                }
            } else {
                sendMessage(sender, "Warning: Skipping backup as 'confirm' flag was used.");
            }

            if (!configFile.delete()) {
                sendMessage(sender, "Failed to delete config.yml");
                return;
            }
        }
        InputStream is = plugin.getResource("config.yml");
        if (is == null) {
            sendMessage(sender, "Default config.yml not found");
            return;
        }
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            plugin.reloadConfig();
            sendMessage(sender, "Config.yml has been reset to default. Backup saved as config_old.yml");
        } catch (IOException e) {
            sendMessage(sender, "Error occurred whilst resetting config.yml");
            e.printStackTrace();
        }
    }

    public void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            fancyMessageSender.sendHoverableMessage(player, message, "AdvancedAchievements", "GREEN");
        } else {
            sender.sendMessage(message);
        }
    }
}