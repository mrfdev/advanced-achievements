package com.hm.achievement.command.executable;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.PluginHeader;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.SoundPlayer;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of handling the /aach book command, which creates and gives a
 * book containing the player's
 * achievements.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "book", permission = "book", minArgs = 1, maxArgs = 1)
public class BookCommand extends AbstractCommand implements Cleanable {

    // Corresponds to times at which players have received their books. Cooldown
    // structure.
    private final HashMap<UUID, Long> playersBookTime = new HashMap<>();
    private final AbstractDatabaseManager databaseManager;
    private final SoundPlayer soundPlayer;
    private final AchievementMap achievementMap;

    private int configTimeBook;
    private String configBookSeparator;
    private boolean configAdditionalEffects;
    private boolean configSound;
    private String configSoundBook;
    private Component langBookDelay;
    private Component langBookNotReceived;
    private Component langBookDate;
    private String langBookName;
    private Component langBookReceived;
    private DateFormat dateFormat;

    @Inject
    public BookCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, PluginHeader pluginHeader, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer, AchievementMap achievementMap) {
        super(mainConfig, langConfig, pluginHeader);
        this.databaseManager = databaseManager;
        this.soundPlayer = soundPlayer;
        this.achievementMap = achievementMap;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        configTimeBook = mainConfig.getInt("TimeBook") * 1000;
        configBookSeparator = "\n&r" + mainConfig.getString("BookSeparator") + "\n&r";
        configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects");
        configSound = mainConfig.getBoolean("Sound");
        configSoundBook = Objects.requireNonNull(mainConfig.getString("SoundBook")).toUpperCase();
        langBookDelay = replace(Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("book-delay")))).build(), "TIME", Integer.toString(configTimeBook / 1000));
        langBookNotReceived = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("book-not-received")))).build();
        langBookDate = ColorHelper.convertAmpersandToComponent(langConfig.getString("book-date"));
        langBookName = langConfig.getString("book-name");
        langBookReceived = Component.text().append(pluginHeader.get()).append(Component.text(Objects.requireNonNull(langConfig.getString("book-received")))).build();


        String localeString = mainConfig.getString("DateLocale");
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag(Objects.requireNonNull(localeString)));
    }

    @Override
    public void cleanPlayerData() {
        long currentTime = System.currentTimeMillis();
        playersBookTime.values().removeIf(bookTime -> currentTime > bookTime + configTimeBook);
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        if (!isInCooldownPeriod(player)) {
            List<AwardedDBAchievement> playerAchievementsList = databaseManager.getPlayerAchievementsList(player.getUniqueId());
            if (playerAchievementsList.isEmpty()) {
                player.sendMessage(langBookNotReceived);
                return;
            }
            // Play special particle effect when receiving the book.
            if (configAdditionalEffects) {
                player.spawnParticle(Particle.ENCHANT, player.getLocation(), 1000, 0, 2, 0, 1);
            }

            // Play special sound when receiving the book.
            if (configSound) {
                soundPlayer.play(player, configSoundBook, "ENTITY_PLAYER_LEVELUP");
            }

            fillBook(playerAchievementsList, player);
        } else {
            player.sendMessage(langBookDelay);
        }
    }

    /**
     * Constructs the pages of a book.
     *
     * @param achievements achievements
     * @param player       player
     */
    private void fillBook(@NonNull List<AwardedDBAchievement> achievements, Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        List<Component> bookPages = new ArrayList<>(achievements.size());
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        for (AwardedDBAchievement awardedAchievement : achievements) {
            Achievement achievement = achievementMap.getForName(awardedAchievement.name());
            if (achievement != null) {
                String currentAchievement = "&0" + achievement.getDisplayName() + configBookSeparator + achievement.getMessage() + configBookSeparator + awardedAchievement.formattedDate();
                bookPages.add(ColorHelper.convertAmpersandToComponent(currentAchievement));
            }
        }

        // Set the pages and other elements of the book (author, title and date of
        // reception).
        bookMeta.addPages(bookPages.toArray(new Component[0]));
        bookMeta.setAuthor(player.getName());
        bookMeta.setTitle(langBookName);
        bookMeta.lore(Collections.singletonList(langBookDate.replaceText(TextReplacementConfig.builder().matchLiteral("DATE").replacement(dateFormat.format(System.currentTimeMillis())).build())));
        book.setItemMeta(bookMeta);

        // Check whether player has room in his inventory, else drop book on the ground.
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(book);
        } else {
            player.getWorld().dropItem(player.getLocation(), book);
        }
        player.sendMessage(langBookReceived);
    }

    /**
     * Checks if player hasn't done a command too recently (with "too recently"
     * being defined in configuration file).
     *
     * @param player player
     * @return whether a player is authorised to perform the list command
     */
    private boolean isInCooldownPeriod(@NonNull Player player) {
        // Player bypasses cooldown if he has full plugin permissions.
        if (player.hasPermission("achievement.*") || configTimeBook == 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        Long lastListTime = playersBookTime.get(player.getUniqueId());
        if (lastListTime == null || currentTime - lastListTime > configTimeBook) {
            playersBookTime.put(player.getUniqueId(), currentTime);
            return false;
        }
        return true;
    }
}