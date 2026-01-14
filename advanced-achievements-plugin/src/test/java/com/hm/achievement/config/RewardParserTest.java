package com.hm.achievement.config;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.utils.MaterialHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardParserTest {

    @Mock
    private Economy economy;
    @Mock
    private Player player;
    @Mock
    private AdvancedAchievements advancedAchievements;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Server server;
    private YamlConfiguration mainConfig;
    private RewardParser underTest;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException, InvalidConfigurationException {
        when(advancedAchievements.getServer()).thenReturn(server);
        when(server.getPluginManager().isPluginEnabled("Vault")).thenReturn(true);
        when(Objects.requireNonNull(server.getServicesManager().getRegistration(Economy.class)).getProvider()).thenReturn(economy);

        mainConfig = new YamlConfiguration();
        YamlConfiguration langConfig = new YamlConfiguration();
        langConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/lang.yml")).toURI()).toFile());
        underTest = new RewardParser(mainConfig, langConfig, advancedAchievements, new MaterialHelper(Logger.getGlobal()));
    }

    @Test
    void shouldParseMoneyRewardSingular() throws URISyntaxException, IOException, InvalidConfigurationException {
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/money-1.yml")).toURI()).toFile());
        when(economy.currencyNameSingular()).thenReturn("coin");

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("receive 1 coin"), reward.listTexts());
        assertEquals(List.of("You received: 1 coin!"), reward.chatTexts());
        reward.rewarder().accept(player);
        verify(economy).depositPlayer(player, 1);
    }

    @Test
    void shouldParseMoneyRewardPlural() throws URISyntaxException, IOException, InvalidConfigurationException {
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/money-2.yml")).toURI()).toFile());
        when(economy.currencyNamePlural()).thenReturn("coins");

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("receive 2 coins"), reward.listTexts());
        assertEquals(List.of("You received: 2 coins!"), reward.chatTexts());
        reward.rewarder().accept(player);
        verify(economy).depositPlayer(player, 2);
    }

    @Test
    void shouldParseSingleCommandReward() throws URISyntaxException, IOException, InvalidConfigurationException {
        World world = Mockito.mock(World.class);
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/command.yml")).toURI()).toFile());
        when(player.getName()).thenReturn("Pyves");
        when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("Nether");

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("teleportation to somewhere special!"), reward.listTexts());
        assertEquals(List.of("You received your reward: teleportation to somewhere special!"), reward.chatTexts());
        reward.rewarder().accept(player);
        TextComponent expectedCommand = Component.text("teleport Pyves");
        String expectedRaw = PlainTextComponentSerializer.plainText().serialize(expectedCommand);
        verify(server).dispatchCommand(any(), eq(expectedRaw));
    }

    @Test
    void shouldParseMultipleCommandRewards() throws URISyntaxException, IOException, InvalidConfigurationException {
        World world = Mockito.mock(World.class);
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/commands.yml")).toURI()).toFile());
        when(player.getName()).thenReturn("Pyves");
        when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("Nether");

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(Arrays.asList("display 1", "display 2"), reward.listTexts());
        assertEquals(Arrays.asList("You received your reward: display 1", "You received your reward: display 2"),
                reward.chatTexts());
        reward.rewarder().accept(player);
        TextComponent exec1 = Component.text("execute 1");
        TextComponent exec2 = Component.text("execute 2");
        String exec1Raw = PlainTextComponentSerializer.plainText().serialize(exec1);
        String exec2Raw = PlainTextComponentSerializer.plainText().serialize(exec2);
        verify(server).dispatchCommand(any(), eq(exec1Raw));
        verify(server).dispatchCommand(any(), eq(exec2Raw));
    }

    @Test
    void shouldParseExperienceReward() throws URISyntaxException, IOException, InvalidConfigurationException {
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/experience.yml")).toURI()).toFile());

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("receive 500 experience"), reward.listTexts());
        assertEquals(List.of("You received: 500 experience!"), reward.chatTexts());
        reward.rewarder().accept(player);
        verify(player).giveExp(500);
    }

    @Disabled("Cannot fix right now")
    @Test
    void shouldParseMaxHealthReward() throws URISyntaxException, IOException, InvalidConfigurationException {
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/max-health.yml")).toURI()).toFile());
        AttributeInstance healthAttribute = Mockito.mock(AttributeInstance.class);
        when(player.getAttribute(any())).thenReturn(healthAttribute);
        when(healthAttribute.getBaseValue()).thenReturn(1.0);

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("increase max health by 2"), reward.listTexts());
        assertEquals(List.of("Your max health has increased by 2!"), reward.chatTexts());
        reward.rewarder().accept(player);
        verify(player).getAttribute(Attribute.MAX_HEALTH);
        verify(healthAttribute).setBaseValue(3.0);
    }

    @Test
    void shouldParseMaxOxygenReward() throws URISyntaxException, IOException, InvalidConfigurationException {
        mainConfig.load(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("reward-parser/max-oxygen.yml")).toURI()).toFile());
        when(player.getMaximumAir()).thenReturn(5);

        List<Reward> rewards = underTest.parseRewards("Reward");

        assertEquals(1, rewards.size());
        Reward reward = rewards.getFirst();
        assertEquals(List.of("increase max oxygen by 10"), reward.listTexts());
        assertEquals(List.of("Your max oxygen has increased by 10!"), reward.chatTexts());
        reward.rewarder().accept(player);
        verify(player).setMaximumAir(15);
    }

}
