package com.hm.achievement.utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Class used to send fancy messages to the player; can be titles, hoverable chat messages or action bar messages. All
 * methods are static and this class cannot be instanciated.
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
     * @param color   The color of the hover text.
     */
    public void sendHoverableMessage(@NonNull Player player, String message, String hover, @NonNull String color) {
        String hexColor = chatColorToHex(ChatColor.valueOf(color.toUpperCase()));
        net.kyori.adventure.text.TextComponent textComponent = Component.text(message).color(TextColor.fromHexString(hexColor));
        net.kyori.adventure.text.event.HoverEvent<Component> hoverEvent = net.kyori.adventure.text.event.HoverEvent.showText(Component.text(hover));
        textComponent = textComponent.hoverEvent(hoverEvent);
        player.sendMessage(textComponent);
    }

    /**
     * Sends a clickable and hoverable message to the player.
     *
     * @param player  Online player to send the message to.
     * @param message The text to display in the chat.
     * @param command The command that is entered when clicking on the message.
     * @param hover   The text to display in the hover.
     * @param color   The color of the hover text.
     */

    public void sendHoverableCommandMessage(@NonNull Player player, String message, String command, String hover, @NonNull String color) {
        String hexColor = chatColorToHex(ChatColor.valueOf(color.toUpperCase()));
        TextComponent textComponent = Component.text(message).color(TextColor.fromHexString(hexColor)).clickEvent(ClickEvent.runCommand(command));
        HoverEvent<Component> hoverEvent = HoverEvent.showText(Component.text(hover));
        textComponent = textComponent.hoverEvent(hoverEvent);
        player.sendMessage(textComponent);

    }

    @Contract(pure = true)
    private @NonNull String chatColorToHex(@NonNull ChatColor chatColor) {
        return switch (chatColor) {
            case DARK_BLUE -> "#0000AA";
            case GREEN -> "#00AA00";
            case AQUA -> "#00AAAA";
            case RED -> "#AA0000";
            case LIGHT_PURPLE -> "#AA00AA";
            case YELLOW -> "#AAAA00";
            case WHITE, GRAY -> "#AAAAAA";
            case DARK_GRAY -> "#555555";
            case DARK_RED -> "#550000";
            case DARK_GREEN -> "#005500";
            case DARK_AQUA -> "#005555";
            case DARK_PURPLE -> "#550055";
            case GOLD -> "#FFAA00";
            case BLUE -> "#5555FF";
            case BLACK -> "#000000";
            default -> "#FFFFFF";
        };
    }
}
