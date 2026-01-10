package com.hm.achievement.command.executable;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.achievement.utils.StringHelper;
import java.util.Objects;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

/**
 * Class in charge of handling the /aach stats command, which creates and
 * displays a progress bar of the player's
 * achievements
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "stats", permission = "stats", minArgs = 1, maxArgs = 1)
public class StatsCommand extends AbstractCommand {

    // Minecraft font, used to get size information in the progress bar.
    private static final MinecraftFont FONT = MinecraftFont.Font;

    private final CacheManager cacheManager;
    private final AchievementMap achievementMap;
    private final SoundPlayer soundPlayer;

    private NamedTextColor configColor;
    private boolean configAdditionalEffects;
    private boolean configSound;
    private String configIcon;
    private String configSoundStats;
    private Component langNumberAchievements;

    @Inject
    public StatsCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager, AchievementMap achievementMap, SoundPlayer soundPlayer) {
        super(mainConfig, langConfig, pluginHeader);
        this.cacheManager = cacheManager;
        this.achievementMap = achievementMap;
        this.soundPlayer = soundPlayer;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        // Load configuration parameters.
        configColor = ColorHelper.parseColor(mainConfig.getString("Color"));
        configIcon = StringHelper.unescapeJava(mainConfig.getString("Icon"));
        configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects");
        configSound = mainConfig.getBoolean("Sound");
        configSoundStats = Objects.requireNonNull(mainConfig.getString("SoundStats")).toUpperCase();
        langNumberAchievements = Component.text(String.valueOf(pluginHeader)).append(Component.text(Objects.requireNonNull(langConfig.getString("number-achievements")))).append(Component.text(" "));
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        int playerAchievements = cacheManager.getPlayerAchievements(player.getUniqueId()).size();
        int totalAchievements = achievementMap.getAll().size();

        Component percentMessage = langNumberAchievements.append(Component.text(String.format("%.1f", 100 * (double) playerAchievements / totalAchievements) + "%", configColor));

        player.sendMessage(percentMessage);

        String middleText = " " + playerAchievements + "/" + totalAchievements + " ";
        int verticalBarsToDisplay = 150 - configIcon.length() - FONT.getWidth(middleText);
        boolean hasDisplayedMiddleText = false;
        Component barDisplay = Component.empty();
        int i = 1;
        while (i < verticalBarsToDisplay) {
            if (!hasDisplayedMiddleText && i >= verticalBarsToDisplay / 2) {
                // Middle reached: append number of achievements information.
                barDisplay = barDisplay.append(Component.text(middleText, NamedTextColor.GRAY));
                // Do not display middleText again.
                hasDisplayedMiddleText = true;
                // Iterate a number of times equal to the number of iterations so far to have
                // the same number of
                // vertical bars left and right from the middle text.
                i = verticalBarsToDisplay - i;
            } else if (i < ((verticalBarsToDisplay - 1) * playerAchievements) / totalAchievements) {
                // Color: progress by user.
                barDisplay = barDisplay.append(Component.text("|", configColor));
                i++;
            } else {
                // Grey: amount not yet reached by user.
                barDisplay = barDisplay.append(Component.text("|", NamedTextColor.DARK_GRAY));
                i++;
            }
        }
        // Display enriched progress bar.
        Component message = Component.text(String.valueOf(pluginHeader)).append(Component.text("[")).append(barDisplay).append(Component.text("]", NamedTextColor.DARK_GRAY));
        player.sendMessage(message);

        // Player has received all achievement; play special effect and sound.
        if (playerAchievements >= totalAchievements) {
            if (configAdditionalEffects) {
                player.spawnParticle(Particle.WITCH, player.getLocation(), 400, 0, 1, 0, 0.5f);
            }

            if (configSound) {
                soundPlayer.play(player, configSoundStats, "ENTITY_FIREWORK_ROCKET_BLAST");
            }
        }
    }
}
