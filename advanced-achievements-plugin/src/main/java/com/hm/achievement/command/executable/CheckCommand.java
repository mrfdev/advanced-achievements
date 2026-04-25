package com.hm.achievement.command.executable;

import com.hm.achievement.config.PluginHeader;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "check", permission = "check", minArgs = 3, maxArgs = Integer.MAX_VALUE)
public class CheckCommand extends AbstractParsableCommand {

    private final CacheManager cacheManager;

    private Component langCheckAchievementTrue;
    private Component langCheckAchievementFalse;

    @Inject
    public CheckCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, PluginHeader pluginHeader, CacheManager cacheManager) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        langCheckAchievementTrue = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("check-achievement-true")))).build();
        langCheckAchievementFalse = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("check-achievements-false")))).build();
    }

    @Override
    void onExecuteForPlayer(CommandSender sender, String[] args, @NonNull Player player) {
        String achievementName = parseAchievementName(args);
        // Check if achievement exists in database and display message accordingly.
        if (cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) sender.sendMessage(replace(langCheckAchievementTrue, new String[]{"PLAYER", "ACH"}, new String[]{args[args.length - 1], achievementName}));
        else sender.sendMessage(replace(langCheckAchievementFalse, new String[]{"PLAYER", "ACH"}, new String[]{args[args.length - 1], achievementName}));
    }
}
