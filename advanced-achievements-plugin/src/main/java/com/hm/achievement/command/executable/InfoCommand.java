package com.hm.achievement.command.executable;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.config.RewardParser;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class in charge of displaying the plugin's extra information (/aach info).
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "info", permission = "", minArgs = 1, maxArgs = 1)
public class InfoCommand extends AbstractCommand {

    private final AdvancedAchievements advancedAchievements;
    private final RewardParser rewardParser;

    private String configDatabaseType;
    private String header;

    private String langVersionCommandDescription;
    private String langVersionCommandAuthor;
    private String langVersionCommandVersion;
    private String langVersionCommandWebsite;
    private String langVersionCommandVault;
    private String langVersionCommandPetmaster;
    private String langVersionCommandEssentials;
    private String langVersionCommandPlaceholderAPI;
    private String langVersionCommandDatabase;

    @Inject
    public InfoCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
                       StringBuilder pluginHeader, AdvancedAchievements advancedAchievements, RewardParser rewardParser) {
        super(mainConfig, langConfig, pluginHeader);
        this.advancedAchievements = advancedAchievements;
        this.rewardParser = rewardParser;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        ChatColor configColor = ChatColor.getByChar(Objects.requireNonNull(mainConfig.getString("Color")));
        String configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon"));
        configDatabaseType = mainConfig.getString("DatabaseType");

        header = configColor + "------------ " + configIcon + translateColorCodes(" &lAdvanced Achievements ") + configColor
                + configIcon + configColor + " ------------";

        langVersionCommandDescription = pluginHeader.toString() + configColor
                + langConfig.getString("version-command-description") + " " + ChatColor.GRAY
                + langConfig.getString("version-command-description-details");

        langVersionCommandVersion = pluginHeader.toString() + configColor + langConfig.getString("version-command-version")
                + " " + ChatColor.GRAY + advancedAchievements.getPluginMeta().getVersion();

        langVersionCommandAuthor = pluginHeader.toString() + configColor + langConfig.getString("version-command-author")
                + " " + ChatColor.GRAY + advancedAchievements.getPluginMeta().getAuthors().getFirst();

        langVersionCommandWebsite = pluginHeader.toString() + configColor + langConfig.getString("version-command-website")
                + " " + ChatColor.GRAY + advancedAchievements.getPluginMeta().getWebsite();

        // Display whether Advanced Achievements is linked to Vault.
        String vaultState = rewardParser.getEconomy() != null ? "&a✔" : "&4✘";
        langVersionCommandVault = pluginHeader.toString() + configColor + langConfig.getString("version-command-vault")
                + " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(vaultState));

        // Display whether Advanced Achievements is linked to Pet Master.
        String petMasterState = Bukkit.getPluginManager().isPluginEnabled("PetMaster") ? "&a✔" : "&4✘";
        langVersionCommandPetmaster = pluginHeader.toString() + configColor
                + langConfig.getString("version-command-petmaster") + " " + ChatColor.GRAY
                + translateColorCodes(StringEscapeUtils.unescapeJava(petMasterState));

        // Display whether Advanced Achievements is linked to Essentials.
        boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials")
                && mainConfig.getBoolean("IgnoreAFKPlayedTime");
        String essentialsState = essentialsUsed ? "&a✔" : "&4✘";
        langVersionCommandEssentials = pluginHeader.toString() + configColor
                + langConfig.getString("version-command-essentials") + " " + ChatColor.GRAY
                + translateColorCodes(StringEscapeUtils.unescapeJava(essentialsState));

        // Display whether Advanced Achievements is linked to PlaceholderAPI.
        String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a✔" : "&4✘";
        langVersionCommandPlaceholderAPI = pluginHeader.toString() + configColor
                + langConfig.getString("version-command-placeholderapi") + " " + ChatColor.GRAY
                + translateColorCodes(StringEscapeUtils.unescapeJava(placeholderAPIState));

        // Display database type.
        String databaseType = getDatabaseType();
        langVersionCommandDatabase = pluginHeader.toString() + configColor + langConfig.getString("version-command-database")
                + " " + ChatColor.GRAY + databaseType;
    }

    private String getDatabaseType() {
        if ("mysql".equalsIgnoreCase(configDatabaseType)) {
            return "MySQL";
        } else if ("postgresql".equalsIgnoreCase(configDatabaseType)) {
            return "PostgreSQL";
        } else if ("h2".equalsIgnoreCase(configDatabaseType)) {
            return "H2";
        } else {
            return "SQLite";
        }
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        sender.sendMessage(header);
        sender.sendMessage(langVersionCommandDescription);
        sender.sendMessage(langVersionCommandVersion);
        sender.sendMessage(langVersionCommandAuthor);
        sender.sendMessage(langVersionCommandWebsite);
        if (sender.hasPermission("achievement.*")) {
            sender.sendMessage(langVersionCommandVault);
            sender.sendMessage(langVersionCommandPetmaster);
            sender.sendMessage(langVersionCommandEssentials);
            sender.sendMessage(langVersionCommandPlaceholderAPI);
            sender.sendMessage(langVersionCommandDatabase);
        }
    }
}
