package com.hm.achievement.listener;

import javax.inject.Inject;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import com.hm.achievement.runnable.AchieveDistanceRunnable;

/**
 * Listener class to update the player's location when teleporting, in order to keep the distance achievement statistics
 * correct.
 * 
 * @author Pyves
 *
 */
public class TeleportListener implements Listener {

	private final AchieveDistanceRunnable distanceRunnable;

	@Inject
	public TeleportListener(AchieveDistanceRunnable distanceRunnable) {
		this.distanceRunnable = distanceRunnable;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		// Update location of player if he respawns after dying.
		distanceRunnable.updateLocation(event.getPlayer().getUniqueId(), event.getRespawnLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		// Event fired twice when teleporting with a nether portal: first time to go to nether with the cause
		// NETHER_PORTAL, then later on to change location in nether; we must only consider the second change because
		// the location of the player is not updated during the first event; if the distances are monitored by the
		// plugin between the two events, it would lead to incorrect results.
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			return;
		}

		// Update location of player if they teleport somewhere else.
		distanceRunnable.updateLocation(event.getPlayer().getUniqueId(), event.getTo());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityTeleport(@NotNull EntityTeleportEvent event) {
		for (Entity passenger : event.getEntity().getPassengers()) {
			if (passenger instanceof Player) {
				// Update location of player if they teleport somewhere else.
				distanceRunnable.updateLocation(passenger.getUniqueId(), event.getTo());
			}
		}

	}
}
