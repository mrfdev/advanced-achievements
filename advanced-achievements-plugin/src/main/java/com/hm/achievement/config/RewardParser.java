package com.hm.achievement.config;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.StringHelper;

import net.milkbowl.vault.economy.Economy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
	public RewardParser(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
						@NotNull AdvancedAchievements advancedAchievements, MaterialHelper materialHelper) {
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

	private @NotNull Reward parseMoneyReward(@NotNull ConfigurationSection configSection) {
		int amount = configSection.getInt("Money");
		String currencyName = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-money"), "AMOUNT",
				amount + " " + currencyName);
		String chatText = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(StringUtils.replaceOnce(langConfig.getString("money-reward-received"), "AMOUNT",
                        amount + " " + currencyName)));
		Consumer<Player> rewarder = player -> economy.depositPlayer(player, amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	@Contract("_ -> new")
	private @NotNull Reward parseItemReward(@NotNull ConfigurationSection configSection) {
		List<String> listTexts = new ArrayList<>();
		List<String> chatTexts = new ArrayList<>();
		List<ItemStack> itemStacks = new ArrayList<>();

		String itemPath = configSection.contains("Item") ? "Item" : "Items";
		for (String item : getOneOrManyConfigStrings(configSection, itemPath)) {
			if (!item.contains(" ")) {
				continue;
			}
			String[] parts = StringUtils.split(item);
			Optional<Material> rewardMaterial = materialHelper.matchMaterial(parts[0],
					"config.yml (" + (configSection.getCurrentPath() + ".Item") + ")");
			if (rewardMaterial.isPresent()) {
				int amount = NumberUtils.toInt(parts[1], 1);
				ItemStack itemStack = new ItemStack(rewardMaterial.get(), amount);
				String name = StringUtils.join(parts, " ", 2, parts.length);
				if (name.isEmpty()) {
					// Convert the item stack material to an item name in a readable format.
					name = WordUtils.capitalizeFully(itemStack.getType().toString().replace('_', ' '));
				} else {
					ItemMeta itemMeta = itemStack.getItemMeta();
					if (itemMeta != null) {
						Component displayName = Component.text(name);
						itemMeta.displayName(displayName);
						itemStack.setItemMeta(itemMeta);
					}
				}
				listTexts.add(StringUtils.replaceEach(langConfig.getString("list-reward-item"),
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				chatTexts.add(StringUtils.replaceEach(langConfig.getString("item-reward-received"),
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				itemStacks.add(itemStack);
			}
		}
		Consumer<Player> rewarder = player -> itemStacks.forEach(item -> {
			ItemStack playerItem = item.clone();
			ItemMeta itemMeta = playerItem.getItemMeta();
			if (itemMeta != null && itemMeta.hasDisplayName()) {
				Component displayName = item.displayName();
				Component newDisplayName = StringHelper.replacePlayerPlaceholders(displayName, player);
				itemMeta.displayName(newDisplayName);
				playerItem.setItemMeta(itemMeta);
			}
			Map<Integer, ItemStack> leftoverItem = player.getInventory().addItem(playerItem);
			for (ItemStack itemToDrop : leftoverItem.values()) {
				player.getWorld().dropItem(player.getLocation(), itemToDrop);
			}
		});
		return new Reward(listTexts, chatTexts, rewarder);
	}

	private @NotNull Reward parseExperienceReward(@NotNull ConfigurationSection configSection) {
		int amount = configSection.getInt("Experience");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-experience"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(StringUtils.replaceOnce(langConfig.getString("experience-reward-received"), "AMOUNT",
                        Integer.toString(amount))));
		Consumer<Player> rewarder = player -> player.giveExp(amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private @NotNull Reward parseIncreaseMaxHealthReward(@NotNull ConfigurationSection configSection) {
		int amount = configSection.getInt("IncreaseMaxHealth");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-increase-max-health"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(StringUtils.replaceOnce(langConfig.getString("increase-max-health-reward-received"), "AMOUNT",
                        Integer.toString(amount))));
		Consumer<Player> rewarder = player -> {
			AttributeInstance playerAttribute = player.getAttribute(Attribute.MAX_HEALTH);
			Objects.requireNonNull(playerAttribute).setBaseValue(playerAttribute.getBaseValue() + amount);
		};
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private @NotNull Reward parseIncreaseMaxOxygenReward(@NotNull ConfigurationSection configSection) {
		int amount = configSection.getInt("IncreaseMaxOxygen");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-increase-max-oxygen"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(StringUtils.replaceOnce(langConfig.getString("increase-max-oxygen-reward-received"), "AMOUNT",
                        Integer.toString(amount))));
		Consumer<Player> rewarder = player -> player.setMaximumAir(player.getMaximumAir() + amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private @NotNull Reward parseCommandReward(@NotNull ConfigurationSection configSection) {
		String displayPath = configSection.contains("Command") ? "Command.Display" : "Commands.Display";
		List<String> listTexts = getOneOrManyConfigStrings(configSection, displayPath);
		List<String> chatTexts = listTexts.stream()
				.map(message -> StringUtils.replace(langConfig.getString("custom-command-reward"), "MESSAGE", message))
				.collect(Collectors.toList());
		String executePath = configSection.contains("Command") ? "Command.Execute" : "Commands.Execute";
		Consumer<Player> rewarder = player -> getOneOrManyConfigStrings(configSection, executePath).stream()
				.map(command -> StringHelper.replacePlayerPlaceholders(command, player))
				.forEach(command -> server.dispatchCommand(server.getConsoleSender(), String.valueOf(command)));
		return new Reward(listTexts, chatTexts, rewarder);
	}

	private List<String> getOneOrManyConfigStrings(@NotNull ConfigurationSection configSection, String path) {
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
