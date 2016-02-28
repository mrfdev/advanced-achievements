package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveXPListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveXPListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChange(PlayerLevelChangeEvent event) {

		Player player = event.getPlayer();
		
		if (!player.hasPermission("achievement.count.maxlevel")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;
		
		int levels;
		if (!DatabasePools.getXpHashMap().containsKey(player.getUniqueId().toString()))
			levels = plugin.getDb().getNormalAchievementAmount(player, "levels");
		else
			levels = DatabasePools.getXpHashMap().get(player.getUniqueId().toString());

		if (event.getNewLevel() > levels)
			DatabasePools.getXpHashMap().put(player.getUniqueId().toString(), event.getNewLevel());

		String configAchievement = "MaxLevel." + event.getNewLevel();
		if (plugin.getReward().checkAchievement(configAchievement)) {

			if (plugin.getDb().hasPlayerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name")))
				return;
			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}

}
