package com.hm.achievement.utils;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
	public void play(@NotNull Player player, String providedSound, String fallbackSound) {
		Sound soundToPlay = parseSound(providedSound, fallbackSound);
		player.playSound(player.getLocation(), soundToPlay, 1, 0.7f);
	}

	/**
	 * Parses a sound string and returns the corresponding Sound enum value.
	 *
	 * @param soundName      The name of the sound to parse.
	 * @param fallbackSound  The fallback sound to use if parsing fails.
	 * @return The resolved Sound enum value.
	 */
	private @NotNull Sound parseSound(String soundName, String fallbackSound) {
		try {
			return Sound.valueOf(soundName);
		} catch (IllegalArgumentException e) {
			logger.warning("Sound " + soundName + " is invalid, using default instead.");
			try {
				return Sound.valueOf(fallbackSound);
			} catch (IllegalArgumentException ex) {
				throw new RuntimeException("Fallback sound " + fallbackSound + " is also invalid!", ex);
			}
		}
	}
}
