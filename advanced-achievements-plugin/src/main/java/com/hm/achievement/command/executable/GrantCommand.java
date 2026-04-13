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
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

@Singleton
@CommandSpec(name = "grant", permission = "grant", minArgs = 3, maxArgs = 3)
public class GrantCommand extends AbstractParsableCommand {
    private final CacheManager cacheManager;
    private final AchievementMap achievementMap;
    private boolean configMultiCommand;
    private Component langAchievementAlreadyReceived;
    private Component langAchievementGranted;
    private Component langAchievementNotFound;
    private Component langAchievementNoPermission;
    private Component langPlayerNotFound;
    private Component langAchievementGrantedAll;

    @Inject
    public GrantCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Component pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        configMultiCommand = mainConfig.getBoolean("MultiCommand");
        langAchievementAlreadyReceived = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-already-received")))).build();
        langAchievementGranted = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-granted")))).build();
        langAchievementGrantedAll = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-granted-all")))).build();
        langAchievementNotFound = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-not-found")))).build();
        langAchievementNoPermission = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-no-permission")))).build();
        langPlayerNotFound = Component.text().append(pluginHeader).append(Component.text(langConfig.getString("player-not-found", "Player not found."))).build();
    }

    @Override
    public void onExecuteForPlayer(CommandSender sender, String @NonNull [] args, Player ignored) {
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
                sender.sendMessage(replace(langAchievementGrantedAll, "PLAYER", playerName));
            } else {
                sender.sendMessage(replace(langAchievementAlreadyReceived, "PLAYER", playerName));
            }
            return;
        }

        Optional<Achievement> achievement = achievementMap.getAll().stream().filter(ach -> ach.getName().equalsIgnoreCase(achName)).findAny();
        if (achievement.isPresent()) {
            Achievement ach = achievement.get();
            if (!configMultiCommand && cacheManager.hasPlayerAchievement(targetPlayer.getUniqueId(), ach.getName())) {
                sender.sendMessage(replace(langAchievementAlreadyReceived, "PLAYER", playerName));
                return;
            }
            if (!sender.hasPermission("achievement." + ach.getName())) {
                sender.sendMessage(replace(langAchievementNoPermission, "PLAYER", playerName));
                return;
            }
            Bukkit.getPluginManager().callEvent(new PlayerAdvancedAchievementEvent(targetPlayer, ach));
            sender.sendMessage(replace(langAchievementGranted, "PLAYER", playerName));
        } else {
            Set<String> names = achievementMap.getForCategory(CommandAchievements.COMMANDS).stream().map(Achievement::getName).collect(Collectors.toSet());
            sender.sendMessage(replace(langAchievementNotFound, "CLOSEST_MATCH", StringHelper.getClosestMatch(achName, names)));
        }
    }
}
