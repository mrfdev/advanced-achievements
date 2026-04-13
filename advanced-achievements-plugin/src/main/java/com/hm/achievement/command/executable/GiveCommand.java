package com.hm.achievement.command.executable;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.StringHelper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "give", permission = "give", minArgs = 3, maxArgs = 3)
public class GiveCommand extends AbstractParsableCommand {

    private final CacheManager cacheManager;
    private final AchievementMap achievementMap;

    private boolean configMultiCommand;
    private Component langAchievementAlreadyReceived;
    private Component langAchievementGiven;
    private Component langAchievementNotFound;
    private Component langAchievementNoPermission;

    @Inject
    public GiveCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Component pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        configMultiCommand = mainConfig.getBoolean("MultiCommand");

        langAchievementAlreadyReceived = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-already-received")))).build();
        langAchievementGiven = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-given")))).build();
        langAchievementNotFound = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-not-found")))).build();
        langAchievementNoPermission = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-no-permission")))).build();
    }

    @Override
    void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
        Optional<Achievement> achievement = achievementMap.getForCategory(CommandAchievements.COMMANDS).stream().filter(ach -> ach.getSubcategory().equals(args[1])).findAny();

        if (achievement.isPresent()) {
            // Check whether player has already received achievement and cannot receive it again.
            if (!configMultiCommand && cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.get().getName())) {
                sender.sendMessage(replace(langAchievementAlreadyReceived, "PLAYER", args[2]));
                return;
            } else if (!player.hasPermission("achievement." + achievement.get().getName())) {
                sender.sendMessage(replace(langAchievementNoPermission, "PLAYER", args[2]));
                return;
            }

            Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(player, achievement.get()));

            sender.sendMessage(langAchievementGiven);
        } else {
            Set<String> commandKeys = achievementMap.getSubcategoriesForCategory(CommandAchievements.COMMANDS);
            sender.sendMessage(replace(langAchievementNotFound, "CLOSEST_MATCH", StringHelper.getClosestMatch(args[1], commandKeys)));
        }
    }
}
