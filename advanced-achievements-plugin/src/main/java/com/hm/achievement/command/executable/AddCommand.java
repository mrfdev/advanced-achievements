package com.hm.achievement.command.executable;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.PluginHeader;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import com.hm.achievement.utils.StringHelper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of increase a statistic of an achievement by command.
 *
 * @author Phoetrix
 */
@Singleton
@CommandSpec(name = "add", permission = "add", minArgs = 4, maxArgs = 4)
public class AddCommand extends AbstractParsableCommand {

    private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

    private final AbstractDatabaseManager databaseManager;
    private final CacheManager cacheManager;
    private final StatisticIncreaseHandler statisticIncreaseHandler;
    private final AchievementMap achievementMap;
    private Component langErrorValue;
    private Component langStatisticIncreased;
    private Component langCategoryDoesNotExist;

    @Inject
    public AddCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, PluginHeader pluginHeader, AbstractDatabaseManager databaseManager, CacheManager cacheManager, StatisticIncreaseHandler statisticIncreaseHandler, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.databaseManager = databaseManager;
        this.cacheManager = cacheManager;
        this.statisticIncreaseHandler = statisticIncreaseHandler;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        langErrorValue = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("error-value")))).build();
        langStatisticIncreased = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("statistic-increased")))).build();
        langCategoryDoesNotExist = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("category-does-not-exist")))).build();
    }

    @Override
    void onExecuteForPlayer(CommandSender sender, String @NonNull [] args, Player player) {
        if (!NumberUtils.isCreatable(args[1])) {
            sender.sendMessage(replace(langErrorValue, "VALUE", args[1]));
            return;
        }

        int valueToAdd = Integer.parseInt(args[1]);
        Set<String> categorySubcategories = achievementMap.getCategorySubcategories();
        if (categorySubcategories.contains(args[2])) {
            UUID uuid = player.getUniqueId();
            if (args[2].contains(".")) {
                MultipleAchievements category = MultipleAchievements.getByName(StringUtils.substringBefore(args[2], "."));
                String subcategory = StringUtils.substringAfter(args[2], ".");
                long amount = cacheManager.getAndIncrementStatisticAmount(category, subcategory, uuid, valueToAdd);
                statisticIncreaseHandler.checkThresholdsAndAchievements(player, category, subcategory, amount);
            } else {
                NormalAchievements category = NormalAchievements.getByName(args[2]);
                long amount;
                if (category == NormalAchievements.PLAYEDTIME) {
                    // Thresholds in the configuration are in hours, underlying statistics are millis.
                    valueToAdd = (int) (valueToAdd * MILLIS_PER_HOUR);
                    amount = cacheManager.getAndIncrementStatisticAmount(category, uuid, valueToAdd) / MILLIS_PER_HOUR;
                } else if (category == NormalAchievements.CONNECTIONS) {
                    amount = databaseManager.getNormalAchievementAmount(uuid, NormalAchievements.CONNECTIONS) + valueToAdd;
                    databaseManager.updateConnectionInformation(uuid, amount);
                } else {
                    amount = cacheManager.getAndIncrementStatisticAmount(category, uuid, valueToAdd);
                }
                statisticIncreaseHandler.checkThresholdsAndAchievements(player, category, amount);
            }
            sender.sendMessage(replace(langStatisticIncreased, new String[]{"ACH", "AMOUNT", "PLAYER"}, new String[]{args[2], args[1], args[3]}));
        } else {
            sender.sendMessage(replace(langCategoryDoesNotExist, new String[]{"CAT", "CLOSEST_MATCH"}, new String[]{args[2], StringHelper.getClosestMatch(args[2], categorySubcategories)}));
        }
    }
}
