package com.hm.achievement.command.executable;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.config.RewardParser;
import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.StringHelper;
import java.util.Objects;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

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
    private Component header;

    private Component langVersionCommandDescription;
    private Component langVersionCommandAuthor;
    private Component langVersionCommandVersion;
    private Component langVersionCommandWebsite;
    private Component langVersionCommandVault;
    private Component langVersionCommandPetmaster;
    private Component langVersionCommandEssentials;
    private Component langVersionCommandPlaceholderAPI;
    private Component langVersionCommandDatabase;

    @Inject
    public InfoCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, AdvancedAchievements advancedAchievements, RewardParser rewardParser) {
        super(mainConfig, langConfig, pluginHeader);
        this.advancedAchievements = advancedAchievements;
        this.rewardParser = rewardParser;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        NamedTextColor configColor = ColorHelper.parseColor(Objects.requireNonNull(mainConfig.getString("Color")));
        String configIcon = StringHelper.unescapeJava(mainConfig.getString("Icon"));
        configDatabaseType = mainConfig.getString("DatabaseType");

        header = Component.text("------------ ", configColor).append(Component.text(Objects.requireNonNull(configIcon))).append(Component.text(" Advanced Achievements ", NamedTextColor.WHITE)).append(Component.text(configIcon)).append(Component.text(" ------------", configColor));

        langVersionCommandDescription = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-description")), configColor)).append(Component.text(" ")).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-description-details")), NamedTextColor.GRAY));

        langVersionCommandVersion = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-version")), configColor)).append(Component.text(" ")).append(Component.text(advancedAchievements.getPluginMeta().getVersion(), NamedTextColor.GRAY));

        langVersionCommandAuthor = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-author")), configColor)).append(Component.text(" ")).append(Component.text(advancedAchievements.getPluginMeta().getAuthors().getFirst(), NamedTextColor.GRAY));

        langVersionCommandWebsite = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-website")), configColor)).append(Component.text(" ")).append(Component.text(Objects.requireNonNull(advancedAchievements.getPluginMeta().getWebsite()), NamedTextColor.GRAY));

        // Display whether Advanced Achievements is linked to Vault.
        String vaultState = rewardParser.getEconomy() != null ? "&a✔" : "&4✘";
        langVersionCommandVault = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-vault")), configColor)).append(Component.text(" ")).append(fromLegacy(vaultState).color(NamedTextColor.GRAY));

        // Display whether Advanced Achievements is linked to Pet Master.
        String petMasterState = Bukkit.getPluginManager().isPluginEnabled("PetMaster") ? "&a✔" : "&4✘";
        langVersionCommandPetmaster = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-petmaster")), configColor)).append(Component.text(" ")).append(fromLegacy(petMasterState).color(NamedTextColor.GRAY));

        // Display whether Advanced Achievements is linked to Essentials.
        boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials") && mainConfig.getBoolean("IgnoreAFKPlayedTime");
        String essentialsState = essentialsUsed ? "&a✔" : "&4✘";
        langVersionCommandEssentials = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-essentials")), configColor)).append(Component.text(" ")).append(fromLegacy(essentialsState).color(NamedTextColor.GRAY));

        // Display whether Advanced Achievements is linked to PlaceholderAPI.
        String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a✔" : "&4✘";
        langVersionCommandPlaceholderAPI = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-placeholderapi")), configColor)).append(Component.text(" ")).append(fromLegacy(placeholderAPIState).color(NamedTextColor.GRAY));

        // Display database type.
        String databaseType = getDatabaseType();
        langVersionCommandDatabase = Component.text(pluginHeader.toString()).append(Component.text(Objects.requireNonNull(langConfig.getString("version-command-database")), configColor)).append(Component.text(" ")).append(Component.text(databaseType, NamedTextColor.GRAY));
    }

    private @NonNull String getDatabaseType() {
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
    void onExecute(@NonNull CommandSender sender, String[] args) {
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

    private @NonNull Component fromLegacy(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }
}
