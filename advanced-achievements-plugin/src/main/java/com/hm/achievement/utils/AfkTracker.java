package com.hm.achievement.utils;

import com.hm.achievement.AdvancedAchievements;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("unused")
public class AfkTracker implements Listener {

    private static final long AFK_TIMEOUT = TimeUnit.MINUTES.toMillis(5);
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    @Inject
    public AfkTracker(final AdvancedAchievements plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) recordActivity(player);
    }

    public boolean isAfk(final @NonNull Player player) {
        final long lastActive = this.lastActivity.computeIfAbsent(player.getUniqueId(), ignored -> System.currentTimeMillis());
        return System.currentTimeMillis() - lastActive >= AFK_TIMEOUT;
    }

    private void recordActivity(final @NonNull Player player) {
        this.lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(final @NonNull PlayerQuitEvent event) {
        this.lastActivity.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(final @NonNull PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            recordActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChat(final @NonNull AsyncPlayerChatEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommand(final @NonNull PlayerCommandPreprocessEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(final @NonNull PlayerInteractEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerAnimation(final @NonNull PlayerAnimationEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerItemHeld(final @NonNull PlayerItemHeldEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(final @NonNull PlayerDropItemEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwapHandItems(final @NonNull PlayerSwapHandItemsEvent event) {
        recordActivity(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(final @NonNull InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            recordActivity(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(final @NonNull InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            recordActivity(player);
        }
    }
}
