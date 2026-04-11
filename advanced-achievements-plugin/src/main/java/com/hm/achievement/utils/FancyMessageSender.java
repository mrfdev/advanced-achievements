package com.hm.achievement.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Class used to send fancy messages to the player; can be titles, hoverable chat messages or action bar messages. All
 * methods are static and this class cannot be instantiated.
 *
 * @author Pyves
 */
@Singleton
public final class FancyMessageSender {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final int serverVersion;

    @Inject
    public FancyMessageSender(int serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * Sends a hoverable message to the player.
     *
     * @param player  Online player to send the message to.
     * @param message The text to display in the chat.
     * @param hover   The text to display in the hover.
     */
    public void sendHoverableMessage(@NonNull Player player, @NonNull TextComponent message, TextComponent hover) {
        player.sendMessage(message.hoverEvent(HoverEvent.showText(hover)));
    }

    /**
     * Sends a clickable and hoverable message to the player.
     *
     * @param player  Online player to send the message to.
     * @param message The text to display in the chat.
     * @param command The command that is entered when clicking on the message.
     * @param hover   The text to display in the hover.
     */

    public void sendHoverableCommandMessage(@NonNull Player player, @NonNull TextComponent message, String command, TextComponent hover) {
        player.sendMessage(message.clickEvent(ClickEvent.runCommand(command)).hoverEvent(HoverEvent.showText(hover)));
    }
}
