package com.hm.achievement.command.executable;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.PluginHeader;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "delete", permission = "delete", minArgs = 3, maxArgs = Integer.MAX_VALUE)
public class DeleteCommand extends AbstractParsableCommand {

    public static final String WILDCARD = "*";

    private final CacheManager cacheManager;
    private final AbstractDatabaseManager databaseManager;
    private final AchievementMap achievementMap;

    private Component langCheckAchievementFalse;
    private Component langDeleteAchievements;
    private Component langAllDeleteAchievements;

    @Inject
    public DeleteCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, PluginHeader pluginHeader, CacheManager cacheManager, AbstractDatabaseManager databaseManager, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.databaseManager = databaseManager;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        langCheckAchievementFalse = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("check-achievements-false")))).build();
        langDeleteAchievements = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("delete-achievements")))).build();
        langAllDeleteAchievements = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("delete-all-achievements")))).build();
    }

    @Override
    void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
        String achievementName = parseAchievementName(args);

        if (WILDCARD.equals(achievementName)) {
            cacheManager.removePreviouslyReceivedAchievements(player.getUniqueId(), achievementMap.getAllNames());
            databaseManager.deleteAllPlayerAchievements(player.getUniqueId());
            sender.sendMessage(replace(langAllDeleteAchievements, "PLAYER", args[args.length - 1]));
        } else if (cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
            cacheManager.removePreviouslyReceivedAchievements(player.getUniqueId(), Collections.singletonList(achievementName));
            databaseManager.deletePlayerAchievement(player.getUniqueId(), achievementName);
            sender.sendMessage(replace(langDeleteAchievements, new String[]{"PLAYER", "ACH"}, new String[]{args[args.length - 1], achievementName}));
        } else sender.sendMessage(replace(langCheckAchievementFalse, new String[]{"PLAYER", "ACH"}, new String[]{args[args.length - 1], achievementName}));
    }
}
