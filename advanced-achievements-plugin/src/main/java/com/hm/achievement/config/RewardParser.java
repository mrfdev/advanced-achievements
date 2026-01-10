package com.hm.achievement.config;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.StringHelper;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of handling the rewards for achievements.
 *
 * @author Pyves
 */
@Singleton
public class RewardParser {

    private static final Pattern MULTIPLE_REWARDS_SPLITTER = Pattern.compile(";\\s*");

    private final YamlConfiguration mainConfig;
    private final YamlConfiguration langConfig;
    private final Server server;
    private final MaterialHelper materialHelper;

    // Used for Vault plugin integration.
    private Economy economy;

    @Inject
    public RewardParser(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, @NonNull AdvancedAchievements advancedAchievements, MaterialHelper materialHelper) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.materialHelper = materialHelper;
        this.server = advancedAchievements.getServer();
        // Try to retrieve an Economy instance from Vault.
        if (server.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public List<Reward> parseRewards(String path) {
        ConfigurationSection configSection = mainConfig.getConfigurationSection(path);
        List<Reward> rewards = new ArrayList<>();
        if (configSection != null) {
            if (economy != null && configSection.contains("Money")) {
                rewards.add(parseMoneyReward(configSection));
            }
            if (configSection.contains("Item") || configSection.contains("Items")) {
                rewards.add(parseItemReward(configSection));
            }
            if (configSection.contains("Experience")) {
                rewards.add(parseExperienceReward(configSection));
            }
            if (configSection.contains("IncreaseMaxHealth")) {
                rewards.add(parseIncreaseMaxHealthReward(configSection));
            }
            if (configSection.contains("IncreaseMaxOxygen")) {
                rewards.add(parseIncreaseMaxOxygenReward(configSection));
            }
            if (configSection.contains("Command") || configSection.contains("Commands")) {
                rewards.add(parseCommandReward(configSection));
            }
        }
        return rewards;
    }

    private @NonNull Reward parseMoneyReward(@NonNull ConfigurationSection configSection) {
        int amount = configSection.getInt("Money");
        String currencyName = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
        String listText = StringUtils.replaceEach(langConfig.getString("list-reward-money"), new String[]{"AMOUNT"}, new String[]{amount + " " + currencyName});
        String chatText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(StringUtils.replaceEach(langConfig.getString("money-reward-received"), new String[]{"AMOUNT"}, new String[]{amount + " " + currencyName})));
        Consumer<Player> rewarder = player -> economy.depositPlayer(player, amount);
        return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
    }

    @Contract("_ -> new")
    private @NonNull Reward parseItemReward(@NonNull ConfigurationSection configSection) {
        List<String> listTexts = new ArrayList<>();
        List<String> chatTexts = new ArrayList<>();
        List<ItemStack> itemStacks = new ArrayList<>();
        String itemPath = configSection.contains("Item") ? "Item" : "Items";
        for (String item : getOneOrManyConfigStrings(configSection, itemPath)) {
            if (!item.contains(" ")) {
                continue;
            }
            String[] parts = StringUtils.split(item);
            Optional<Material> rewardMaterial = materialHelper.matchMaterial(parts[0], "config.yml (" + (configSection.getCurrentPath() + ".Item") + ")");
            if (rewardMaterial.isPresent()) {
                int amount = NumberUtils.toInt(parts[1], 1);
                ItemStack itemStack = new ItemStack(rewardMaterial.get(), amount);
                ItemMeta itemMeta = itemStack.getItemMeta();
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.contains(":")) {
                        String[] enchantParts = part.split(":", 2);
                        if (enchantParts.length == 2) {
                            String enchantName = enchantParts[0].toLowerCase();
                            int enchantLevel = NumberUtils.toInt(enchantParts[1], 1);
                            Registry<@NonNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
                            Enchantment enchantment = enchantmentRegistry.get(NamespacedKey.minecraft(enchantName.toLowerCase()));
                            if (enchantment != null) {
                                itemMeta.addEnchant(enchantment, enchantLevel, true);
                                continue;
                            }
                        }
                    }
                    nameBuilder.append(part).append(" ");
                }
                String name = nameBuilder.toString().trim();
                if (name.isEmpty()) name = StringHelper.toReadableName(rewardMaterial.get());
                if (itemMeta != null) {
                    Component displayName = Component.text(name).style(Style.style().decoration(TextDecoration.ITALIC, false));
                    itemMeta.displayName(displayName);
                    itemStack.setItemMeta(itemMeta);
                }
                listTexts.add(StringUtils.replaceEach(langConfig.getString("list-reward-item"), new String[]{"AMOUNT", "ITEM"}, new String[]{Integer.toString(amount), name}));
                chatTexts.add(StringUtils.replaceEach(langConfig.getString("item-reward-received"), new String[]{"AMOUNT", "ITEM"}, new String[]{Integer.toString(amount), name}));
                itemStacks.add(itemStack);
            }
        }
        Consumer<Player> rewarder = player -> itemStacks.forEach(item -> {
            ItemStack playerItem = item.clone();
            ItemMeta itemMeta = playerItem.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                Component displayName = itemMeta.displayName();
                String plainName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(displayName));
                itemMeta.displayName(Component.text(plainName).style(Style.style().decoration(TextDecoration.ITALIC, false)));
                playerItem.setItemMeta(itemMeta);
            }
            Map<Integer, ItemStack> leftoverItem = player.getInventory().addItem(playerItem);
            leftoverItem.values().forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
        });
        return new Reward(listTexts, chatTexts, rewarder);
    }

    private @NonNull Reward parseExperienceReward(@NonNull ConfigurationSection configSection) {
        int amount = configSection.getInt("Experience");
        String listText = StringUtils.replaceEach(langConfig.getString("list-reward-experience"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)});
        String chatText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(StringUtils.replaceEach(langConfig.getString("experience-reward-received"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)})));
        Consumer<Player> rewarder = player -> player.giveExp(amount);
        return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
    }

    private @NonNull Reward parseIncreaseMaxHealthReward(@NonNull ConfigurationSection configSection) {
        int amount = configSection.getInt("IncreaseMaxHealth");
        String listText = StringUtils.replaceEach(langConfig.getString("list-reward-increase-max-health"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)});
        String chatText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(StringUtils.replaceEach(langConfig.getString("increase-max-health-reward-received"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)})));
        Consumer<Player> rewarder = player -> {
            AttributeInstance playerAttribute = player.getAttribute(Attribute.MAX_HEALTH);
            Objects.requireNonNull(playerAttribute).setBaseValue(playerAttribute.getBaseValue() + amount);
        };
        return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
    }

    private @NonNull Reward parseIncreaseMaxOxygenReward(@NonNull ConfigurationSection configSection) {
        int amount = configSection.getInt("IncreaseMaxOxygen");
        String listText = StringUtils.replaceEach(langConfig.getString("list-reward-increase-max-oxygen"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)});
        String chatText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(StringUtils.replaceEach(langConfig.getString("increase-max-oxygen-reward-received"), new String[]{"AMOUNT"}, new String[]{Integer.toString(amount)})));
        Consumer<Player> rewarder = player -> player.setMaximumAir(player.getMaximumAir() + amount);
        return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
    }

    private @NonNull Reward parseCommandReward(@NonNull ConfigurationSection configSection) {
        String displayPath = configSection.contains("Command") ? "Command.Display" : "Commands.Display";
        List<String> listTexts = getOneOrManyConfigStrings(configSection, displayPath);
        List<String> chatTexts = listTexts.stream().map(message -> StringUtils.replaceEach(langConfig.getString("custom-command-reward"), new String[]{"MESSAGE"}, new String[]{message})).collect(Collectors.toList());
        String executePath = configSection.contains("Command") ? "Command.Execute" : "Commands.Execute";
        Consumer<Player> rewarder = player -> getOneOrManyConfigStrings(configSection, executePath).forEach(command -> {
            Component component = StringHelper.replacePlayerPlaceholders(command, player);
            String rawCommand = PlainTextComponentSerializer.plainText().serialize(component);
            server.dispatchCommand(server.getConsoleSender(), rawCommand);
        });
        return new Reward(listTexts, chatTexts, rewarder);
    }

    private List<String> getOneOrManyConfigStrings(@NonNull ConfigurationSection configSection, String path) {
        if (configSection.isList(path)) {
            // Real YAML list.
            return configSection.getStringList(path);
        }
        String configString = configSection.getString(path);
        if (configString != null) {
            // Either a list of strings separate by "; " (old configuration style), or a single string.
            return Arrays.asList(MULTIPLE_REWARDS_SPLITTER.split(StringUtils.normalizeSpace(configString)));
        }
        return Collections.emptyList();
    }

}
