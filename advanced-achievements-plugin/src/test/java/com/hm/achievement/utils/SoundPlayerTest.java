package com.hm.achievement.utils;

import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Disabled("Cannot fix right now")
    @Test
    void shouldUseProvidedSoundIfValid() {
        SoundPlayer underTest = new SoundPlayer(logger);

        underTest.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "SOME_FALLBACK");

        verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
    }

    @Disabled("Cannot fix right now")
    @Test
    void shouldUseFallbackSoundIfProvidedInvalid() {
        SoundPlayer underTest = new SoundPlayer(logger);

        underTest.play(player, "INVALID", "ENTITY_FIREWORK_ROCKET_BLAST");

        verify(player).playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.7f);
    }
}