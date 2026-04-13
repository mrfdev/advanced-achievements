package com.hm.achievement.command.executable;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StringHelper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "reset", permission = "reset", minArgs = 3, maxArgs = 3)
public class ResetCommand extends AbstractParsableCommand {

    public static final String WILDCARD = "*";

    private final CacheManager cacheManager;
    private final AchievementMap achievementMap;

    private Component langResetSuccessful;
    private Component langResetAllSuccessful;
    private Component langCategoryDoesNotExist;

    @Inject
    public ResetCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Component pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        langResetSuccessful = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("reset-successful")))).build();
        langResetAllSuccessful = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("reset-all-successful")))).build();
        langCategoryDoesNotExist = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("category-does-not-exist")))).build();
    }

    @Override
    void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
        String categoryWithSubcategory = args[1];
        Set<String> categorySubcategories = achievementMap.getCategorySubcategories();
        if (WILDCARD.equals(categoryWithSubcategory)) {
            cacheManager.resetPlayerStatistics(player.getUniqueId(), categorySubcategories);
            sender.sendMessage(replace(langResetAllSuccessful, "PLAYER", player.getName()));
        } else if (categorySubcategories.contains(categoryWithSubcategory)) {
            cacheManager.resetPlayerStatistics(player.getUniqueId(), Collections.singletonList(categoryWithSubcategory));
            sender.sendMessage(replace(langResetSuccessful, new String[]{"CAT", "PLAYER"}, new String[]{categoryWithSubcategory, player.getName()}));
        } else {
            sender.sendMessage(replace(langCategoryDoesNotExist, new String[]{"CAT", "CLOSEST_MATCH"}, new String[]{categoryWithSubcategory, StringHelper.getClosestMatch(categoryWithSubcategory, categorySubcategories)}));
        }
    }
}
