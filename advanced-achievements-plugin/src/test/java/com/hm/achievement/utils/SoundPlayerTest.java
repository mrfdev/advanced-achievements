package com.hm.achievement.utils;

import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoundPlayerTest {

    @Mock
    private Logger logger;

    @Mock
    private Player player;

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location(null, 0, 0, 0);
        when(player.getLocation()).thenReturn(location);
    }

    @Test
    void shouldUseProvidedSoundIfValid() {
        SoundPlayer underTest = spy(new SoundPlayer(logger));
        NamespacedKey key = NamespacedKey.minecraft("entity_firework_rocket_blast");
        doReturn(key).when(underTest).parseSound("ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK");
        underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK");
        verify(player).playSound(location, "minecraft:entity_firework_rocket_blast", 1.0f, 0.7f);
    }

    @Test
    void shouldUseFallbackSoundIfProvidedInvalid() {
        SoundPlayer underTest = spy(new SoundPlayer(logger));
        NamespacedKey key = NamespacedKey.minecraft("entity_firework_rocket_blast");
        doReturn(key).when(underTest).parseSound("INVALID", "ENTITY_FIREWORK_ROCKET_BLAST");
        underTest.play(player, "INVALID", "ENTITY_FIREWORK_ROCKET_BLAST");
        verify(player).playSound(location, "minecraft:entity_firework_rocket_blast", 1.0f, 0.7f);
    }
}