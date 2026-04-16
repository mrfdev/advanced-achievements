package com.hm.achievement.listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.command.executable.ToggleCommand;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.RewardParser;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.FancyMessageSender;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.StringHelper;
import io.papermc.paper.registry.keys.SoundEventKeys;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

/**
 * Listener class to deal with achievement receptions: rewards, display and
 * database operations.
 *
 * @author Pyves
 */
@Singleton
public class PlayerAdvancedAchievementListener implements Listener, Reloadable {

    private static final Random RANDOM = new Random();
    private static final String ADVANCED_ACHIEVEMENTS_FIREWORK = "advanced_achievements_firework";
    private final YamlConfiguration mainConfig;
    private final YamlConfiguration langConfig;
    private final Logger logger;
    private final Component pluginHeader;
    private final CacheManager cacheManager;
    private final AdvancedAchievements advancedAchievements;
    private final RewardParser rewardParser;
    private final AchievementMap achievementMap;
    private final AbstractDatabaseManager databaseManager;
    private final ToggleCommand toggleCommand;
    private final FancyMessageSender fancyMessageSender;
    private String configFireworkStyle;
    private String langBossBarProgress;
    private Component langAchievementReceived;
    private Component langAchievementNew;
    private Component langAllAchievementsReceived;
    private NamedTextColor configFireworkColor;
    private boolean configFirework;
    private boolean configSimplifiedReception;
    private boolean configTitleScreen;
    private boolean configNotifyOtherPlayers;
    private boolean configActionBarNotify;
    private boolean configHoverableReceiverChatText;
    private boolean configReceiverChatMessages;
    private boolean configBossBarProgress;

    @Inject
    public PlayerAdvancedAchievementListener(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Logger logger, Component pluginHeader, CacheManager cacheManager, AdvancedAchievements advancedAchievements, RewardParser rewardParser, AchievementMap achievementMap, AbstractDatabaseManager databaseManager, ToggleCommand toggleCommand, FancyMessageSender fancyMessageSender) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.logger = logger;
        this.pluginHeader = pluginHeader;
        this.cacheManager = cacheManager;
        this.advancedAchievements = advancedAchievements;
        this.rewardParser = rewardParser;
        this.achievementMap = achievementMap;
        this.databaseManager = databaseManager;
        this.toggleCommand = toggleCommand;
        this.fancyMessageSender = fancyMessageSender;
    }

    @Override
    public void extractConfigurationParameters() {
        configFireworkStyle = Objects.requireNonNull(mainConfig.getString("FireworkStyle")).toUpperCase();
        if (!"RANDOM".equals(configFireworkStyle) && !EnumUtils.isValidEnum(Type.class, configFireworkStyle)) {
            configFireworkStyle = Type.BALL_LARGE.name();
            logger.warning("Failed to load FireworkStyle, using ball_large instead. Please use one of the following: " + "ball_large, ball, burst, creeper, star or random.");
        }
        configFirework = mainConfig.getBoolean("Firework");
        configFireworkColor = ColorHelper.configFireworkColor(mainConfig);
        configSimplifiedReception = mainConfig.getBoolean("SimplifiedReception");
        configTitleScreen = mainConfig.getBoolean("TitleScreen");
        configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers");
        configActionBarNotify = mainConfig.getBoolean("ActionBarNotify");
        configHoverableReceiverChatText = mainConfig.getBoolean("HoverableReceiverChatText");
        configBossBarProgress = mainConfig.getBoolean("BossBarProgress");
        configReceiverChatMessages = mainConfig.getBoolean("ReceiverChatMessages");
        langAchievementReceived = Component.text(Objects.requireNonNull(langConfig.getString("achievement-received")));
        langAchievementNew = pluginHeader.append(Component.text(Objects.requireNonNull(langConfig.getString("achievement-new"))));
        langAllAchievementsReceived = pluginHeader.append(Component.text(Objects.requireNonNull(langConfig.getString("all-achievements-received"))));
        langBossBarProgress = langConfig.getString("boss-bar-progress");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NonNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework && firework.getPersistentDataContainer().has(new NamespacedKey(advancedAchievements, ADVANCED_ACHIEVEMENTS_FIREWORK), PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAdvancedAchievementReception(@NonNull PlayerAdvancedAchievementEvent event) {
        Achievement achievement = event.getAchievement();
        Player player = event.getPlayer();
        // Achievement could have already been received if MultiCommand is set to true in the configuration.
        if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.getName())) {
            cacheManager.registerNewlyReceivedAchievement(player.getUniqueId(), achievement.getName());
            // Matching advancement might not exist if user has not called /aach generate.
            Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements, AdvancementManager.getKey(achievement.getName())));
            if (advancement != null) player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
        }
        databaseManager.registerAchievement(player.getUniqueId(), achievement.getName(), System.currentTimeMillis());
        achievement.getRewards().forEach(r -> r.rewarder().accept(player));
        displayAchievement(player, achievement);
        if (cacheManager.getPlayerAchievements(player.getUniqueId()).size() == achievementMap.getAll().size()) handleAllAchievementsReceived(player);
    }

    /**
     * Displays chat messages, screen title and launches a firework when a player
     * receives an achievement.
     *
     * @param player      player
     * @param achievement achievement
     */
    private void displayAchievement(@NonNull Player player, @NonNull Achievement achievement) {
        logger.info("Player " + player.getName() + " received the achievement: " + achievement.getDisplayName());

        Component nameToShowUser = Component.text(achievement.getDisplayName());
        Component messageToShowUser = Component.text(achievement.getMessage());

        if (configReceiverChatMessages || player.hasPermission("achievement.config.receiver.chat.messages")) {
            displayReceiverMessages(player, nameToShowUser, messageToShowUser, achievement.getRewards());
        }

        // Notify other online players that the player has received an achievement.
        // Notify if NotifyOtherPlayers is enabled and player has not used /aach toggle,
        // or if NotifyOtherPlayers is disabled and player has used /aach toggle.
        advancedAchievements.getServer().getOnlinePlayers().stream().filter(p -> !p.getName().equals(player.getName())).filter(p -> configNotifyOtherPlayers ^ toggleCommand.isPlayerToggled(p, achievement.getType())).forEach(p -> displayNotification(player, nameToShowUser, p));
        if (configFirework) displayFirework(player);
        else if (configSimplifiedReception) displaySimplifiedReception(player);
        if (configTitleScreen || player.hasPermission("achievement.config.title.screen")) player.showTitle(Title.title(nameToShowUser, messageToShowUser, Times.times(Duration.ofSeconds(5), Duration.ofSeconds(0), Duration.ofSeconds(5))));
        if (configBossBarProgress) displayBossBarProgress(player);
    }

    /**
     * Displays texts related to the achievement in the receiver's chat. This method
     * can display a single hoverable message or several messages one after the other.
     *
     * @param player            player
     * @param nameToShowUser    name shown to user
     * @param messageToShowUser message shown to user
     * @param rewards           rewards
     */
    private void displayReceiverMessages(Player player, Component nameToShowUser, Component messageToShowUser, @NonNull List<Reward> rewards) {
        List<String> chatMessages = rewards.stream().map(Reward::chatTexts).flatMap(List::stream).map(m -> StringHelper.replacePlayerPlaceholders(m, player)).map(StringHelper::componentToString).toList();
        Component message = langAchievementNew.replaceText(b -> b.matchLiteral("ACH").replacement(nameToShowUser));
        if (configHoverableReceiverChatText) {
            // Build hover component by folding all chat message lines onto the base message
            Component hoverComponent = chatMessages.stream().map(ColorHelper::convertAmpersandToComponent).reduce(messageToShowUser, (acc, t) -> acc.append(Component.newline().append(t)));
            fancyMessageSender.sendHoverableMessage(player, applyPrefix(message), applyPrefix(hoverComponent));
        } else {
            player.sendMessage(applyPrefix(message));
            player.sendMessage(applyPrefix(messageToShowUser));
            chatMessages.stream().map(ColorHelper::convertAmpersandToComponent).forEach(t -> player.sendMessage(applyPrefix(t)));
        }
    }

    /**
     * Displays an action bar message or chat notification to another player.
     *
     * @param receiver       the player who received the achievement
     * @param nameToShowUser the achievement name
     * @param otherPlayer    the player being notified
     */
    private void displayNotification(Player receiver, Component nameToShowUser, Player otherPlayer) {
        Component message = langAchievementReceived.replaceText(b -> b.matchLiteral("PLAYER").replacement(receiver.getName())).replaceText(b -> b.matchLiteral("ACH").replacement(nameToShowUser));
        if (configActionBarNotify) otherPlayer.sendActionBar(message.decorate(TextDecoration.ITALIC));
        else otherPlayer.sendMessage(pluginHeader.append(message).decorate(TextDecoration.ITALIC));
    }

    /**
     * Launches firework when receiving an achievement.
     *
     * @param player
     */
    private void displayFirework(@NonNull Player player) {
        Location location = player.getLocation().subtract(0, 1, 0);
        Firework firework = player.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        Color fireworkColor = Color.fromRGB(configFireworkColor.red(), configFireworkColor.green(), configFireworkColor.blue());
        fireworkMeta.addEffects(FireworkEffect.builder().with(getFireworkType()).withColor(fireworkColor).withFade(fireworkColor).build());
        firework.setFireworkMeta(fireworkMeta);
        firework.getPersistentDataContainer().set(new NamespacedKey(advancedAchievements, ADVANCED_ACHIEVEMENTS_FIREWORK), PersistentDataType.BOOLEAN, true);
        firework.setVelocity(location.getDirection().multiply(0));
    }

    /**
     * Gets the type of the firework, either predefined or random based on config.
     *
     * @return the firework type
     */
    private Type getFireworkType() {
        if ("RANDOM".equals(configFireworkStyle)) {
            Type[] fireworkTypes = Type.values();
            return fireworkTypes[RANDOM.nextInt(fireworkTypes.length)];
        }
        return Type.valueOf(configFireworkStyle);
    }

    /**
     * Displays a simplified particle effect and calm sound when receiving an
     * achievement. Used instead of displayFirework.
     *
     * @param player
     */
    private void displaySimplifiedReception(@NonNull Player player) {
        Sound sound = Registry.SOUNDS.get(SoundEventKeys.ENTITY_PLAYER_LEVELUP);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1, 0.7f);
        }
        player.spawnParticle(Particle.FIREWORK, player.getLocation(), 500, 0, 3, 0, 0.1f);
    }

    /**
     * Displays a boss bar showing the player's achievement progress.
     *
     * @param player
     */
    private void displayBossBarProgress(@NonNull Player player) {
        int receivedAmount = cacheManager.getPlayerAchievements(player.getUniqueId()).size();
        int totalAmount = achievementMap.getAll().size();
        double progress = ((double) receivedAmount) / totalAmount;
        String message = StringUtils.replaceEach(langBossBarProgress, new String[]{"AMOUNT"}, new String[]{receivedAmount + "/" + totalAmount});
        BossBar bossBar = Bukkit.getServer().createBossBar(message, BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(progress);
        Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> bossBar.addPlayer(player), 110);
        Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> bossBar.removePlayer(player), 240);
    }

    /**
     * Handles rewards and displaying messages when a player has received all achievements.
     *
     * @param player
     */
    private void handleAllAchievementsReceived(@NonNull Player player) {
        List<Reward> rewards = rewardParser.parseRewards("AllAchievementsReceivedRewards");
        rewards.forEach(r -> r.rewarder().accept(player));
        player.sendMessage(langAllAchievementsReceived);
        rewards.stream().map(Reward::chatTexts).flatMap(List::stream).map(m -> StringHelper.replacePlayerPlaceholders(m, player)).forEach(t -> player.sendMessage(pluginHeader.append(t)));
    }

    private Component applyPrefix(Component s) {
        if (mainConfig.getBoolean("PrefixEnabled")) return Component.text("[AACH] ").color(NamedTextColor.GRAY).append(pluginHeader).append(s);
        return s;
    }
}