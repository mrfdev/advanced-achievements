package com.hm.achievement.command.executable;


import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.FancyMessageSender;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandSpec(name = "regenconfigfolder", permission = "achievement.regenconfigfolder", minArgs = 0, maxArgs = 1)
public class RegenConfigFolderCommand extends AbstractCommand {
    private final AdvancedAchievements plugin;
    private final FancyMessageSender fancyMessageSender;
    private final Logger LOGGER = Logger.getLogger(RegenConfigFolderCommand.class.getName());

    @Inject
    public RegenConfigFolderCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, AdvancedAchievements plugin, FancyMessageSender fancyMessageSender) {
        super(mainConfig, langConfig, pluginHeader);
        this.plugin = plugin;
        this.fancyMessageSender = fancyMessageSender;
    }

    @Override
    public void onExecute(CommandSender sender, String @NotNull [] args) {
        File configFolder = plugin.getDataFolder();
        boolean confirm = args.length > 0 && args[0].equalsIgnoreCase("confirm");
        if (!confirm) {
            sendMessageSevere(sender, "This will delete the entire config folder and cannot be undone");
            sendMessageSevere(sender, "Type /resetconfig confirm to continue");
            return;
        }
        if (!configFolder.exists()) {
            sendMessageSevere(sender, "Config folder not found");
            return;
        }
        try {
            deleteRecursive(configFolder.toPath());
            sendMessage(sender, "Config folder deleted");
        } catch (IOException e) {
            sendMessageSevere(sender, "Failed to delete config folder");
            LOGGER.severe(e.getMessage());
            return;
        }
        try {
            plugin.onDisable();
            plugin.onEnable();
            sendMessage(sender, "Config folder regenerated");
        } catch (Exception e) {
            sendMessageSevere(sender, "Error during plugin reload");
            LOGGER.severe(e.getMessage());
        }
    }

    public void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                if (!file.delete()) {
                    LOGGER.severe("Failed to delete file: " + file.getAbsoluteFile());
                }
            });
        }

    }

    public void sendMessageSevere(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            fancyMessageSender.sendHoverableMessage(player, message, "AdvancedAchievements", "RED");
        } else {
            sender.sendMessage(message);
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