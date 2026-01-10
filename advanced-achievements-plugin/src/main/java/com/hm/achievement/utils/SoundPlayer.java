package com.hm.achievement.utils;

import java.util.Locale;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of player sounds when players successfully complete some actions.
 *
 * @author Pyves
 */
@Singleton
public class SoundPlayer {

    private final Logger logger;

    @Inject
    public SoundPlayer(Logger logger) {
        this.logger = logger;
    }

    /**
     * Plays a sound provided via configuration. If the sound is invalid, this method falls back to the provided
     * fallback.
     *
     * @param player
     * @param providedSound
     * @param fallbackSound
     */
    public void play(@NonNull Player player, String providedSound, String fallbackSound) {
        Sound soundToPlay = parseSound(providedSound, fallbackSound);
        if (soundToPlay != null) {
            player.playSound(player.getLocation(), soundToPlay, 1, 0.7f);
        } else {
            logger.warning("soundToPlay is null");
        }
        logger.info("playing sound " + soundToPlay);
    }

    /**
     * Parses a sound string and returns the corresponding Sound enum value.
     *
     * @param soundName     The name of the sound to parse.
     * @param fallbackSound The fallback sound to use if parsing fails.
     * @return The resolved Sound enum value.
     */
    private Sound parseSound(@NonNull String soundName, @NonNull String fallbackSound) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase(Locale.ROOT)));
        Sound fallSound = Registry.SOUNDS.get(NamespacedKey.minecraft(fallbackSound.toLowerCase(Locale.ROOT)));
        if (sound == null) {
            return Sound.ENTITY_FIREWORK_ROCKET_BLAST;
        }
        return fallSound;
    }
}