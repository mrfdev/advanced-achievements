package com.hm.achievement.command.executable;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.StringHelper;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandSpec(name = "grant", permission = "grant", minArgs = 3, maxArgs = 3)
public class GrantCommand extends AbstractParsableCommand {
    private final CacheManager cacheManager;
    private final AchievementMap achievementMap;
    private boolean configMultiCommand;
    private String langAchievementAlreadyReceived;
    private String langAchievementGranted;
    private String langAchievementNotFound;
    private String langAchievementNoPermission;
    private String langPlayerNotFound;
    private String langAchievementGrantedAll;

    @Inject
    public GrantCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        configMultiCommand = mainConfig.getBoolean("MultiCommand");
        langAchievementAlreadyReceived = pluginHeader + langConfig.getString("achievement-already-received");
        langAchievementGranted = pluginHeader + langConfig.getString("achievement-granted");
        langAchievementGrantedAll = pluginHeader + langConfig.getString("achievement-granted-all");
        langAchievementNotFound = pluginHeader + langConfig.getString("achievement-not-found");
        langAchievementNoPermission = pluginHeader + langConfig.getString("achievement-no-permission");
        langPlayerNotFound = pluginHeader + langConfig.getString("player-not-found", "Player not found.");
    }

    @Override
    public void onExecuteForPlayer(CommandSender sender, String @NotNull [] args, Player ignored) {
        String achName = args[1];
        String playerName = args[2];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(langPlayerNotFound);
            return;
        }

        String WILDCARD = "*";
        if (WILDCARD.equals(achName)) {
            boolean grantedAny = false;
            for (Achievement ach : achievementMap.getAll()) {
                if (!configMultiCommand && cacheManager.hasPlayerAchievement(targetPlayer.getUniqueId(), ach.getName())) {
                    continue;
                }
                if (!sender.hasPermission("achievement." + ach.getName())) {
                    continue;
                }
                Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(targetPlayer, ach));
                grantedAny = true;
            }
            if (grantedAny) {
                sender.sendMessage(StringUtils.replaceEach(langAchievementGrantedAll, new String[]{"PLAYER"}, new String[]{playerName}));
            } else {
                sender.sendMessage(StringUtils.replaceEach(langAchievementAlreadyReceived, new String[]{"PLAYER"}, new String[]{playerName}));
            }
            return;
        }

        Optional<Achievement> achievement = achievementMap.getAll().stream().filter(ach -> ach.getName().equalsIgnoreCase(achName)).findAny();
        if (achievement.isPresent()) {
            Achievement ach = achievement.get();
            if (!configMultiCommand && cacheManager.hasPlayerAchievement(targetPlayer.getUniqueId(), ach.getName())) {
                sender.sendMessage(StringUtils.replaceEach(langAchievementAlreadyReceived, new String[]{"PLAYER"}, new String[]{playerName}));
                return;
            }
            if (!sender.hasPermission("achievement." + ach.getName())) {
                sender.sendMessage(StringUtils.replaceEach(langAchievementNoPermission, new String[]{"PLAYER"}, new String[]{playerName}));
                return;
            }
            Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(targetPlayer, ach));
            sender.sendMessage(StringUtils.replaceEach(langAchievementGranted, new String[]{"PLAYER"}, new String[]{playerName}));
        } else {
            Set<String> names = achievementMap.getForCategory(CommandAchievements.COMMANDS).stream().map(Achievement::getName).collect(Collectors.toSet());
            sender.sendMessage(StringUtils.replaceEach(langAchievementNotFound, new String[]{"CLOSEST_MATCH"}, new String[]{StringHelper.getClosestMatch(achName, names)}));
        }
    }
}
