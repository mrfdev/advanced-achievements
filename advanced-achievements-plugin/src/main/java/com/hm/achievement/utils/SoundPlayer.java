package com.hm.achievement.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.logging.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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
        NamespacedKey soundToPlay = parseSound(providedSound, fallbackSound);
        if (soundToPlay != null) {
            player.playSound(player.getLocation(), String.valueOf(soundToPlay), 1, 0.7f);
        } else {
            logger.warning("soundToPlay is null");
        }
    }

    /**
     * Parses a sound string and returns the corresponding Sound enum value.
     *
     * @param soundName     The name of the sound to parse.
     * @param fallbackSound The fallback sound to use if parsing fails.
     * @return The resolved Sound enum value.
     */
    NamespacedKey parseSound(@NonNull String soundName, @NonNull String fallbackSound) {
        NamespacedKey key = NamespacedKey.minecraft(soundName.toLowerCase());
        if (Registry.SOUNDS.get(key) != null) return key;
        logger.warning("Sound " + soundName + " is invalid, falling back to " + fallbackSound);
        return NamespacedKey.minecraft(fallbackSound.toLowerCase());
    }
}