package com.hm.achievement.command.executable;

import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.FancyMessageSender;
import com.hm.achievement.utils.StringHelper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of displaying the plugin's help (/aach help).
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "help", permission = "", minArgs = 0, maxArgs = Integer.MAX_VALUE)
public class HelpCommand extends AbstractCommand {

    private final FancyMessageSender fancyMessageSender;

    private NamedTextColor configColor;
    private String configIcon;

    private Component langCommandList;
    private Component langCommandListHover;
    private Component langCommandTop;
    private Component langCommandTopHover;
    private Component langCommandInfo;
    private Component langCommandInfoHover;
    private Component langCommandBook;
    private Component langCommandBookHover;
    private Component langCommandWeek;
    private Component langCommandWeekHover;
    private Component langCommandStats;
    private Component langCommandStatsHover;
    private Component langCommandMonth;
    private Component langCommandMonthHover;
    private Component langCommandToggleHover;
    private Component langCommandToggle;
    private Component langCommandReload;
    private Component langCommandReloadHover;
    private Component langCommandGenerate;
    private Component langCommandGenerateHover;
    private Component langCommandInspect;
    private Component langCommandInspectHover;
    private Component langCommandGive;
    private Component langCommandGiveHover;
    private Component langCommandAdd;
    private Component langCommandAddHover;
    private Component langCommandReset;
    private Component langCommandResetHover;
    private Component langCommandCheck;
    private Component langCommandCheckHover;
    private Component langCommandDelete;
    private Component langCommandDeleteHover;
    private Component langCommandGrant;
    private Component langCommandGrantHover;
    private Component langTip;

    @Inject
    public HelpCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Component pluginHeader, FancyMessageSender fancyMessageSender) {
        super(mainConfig, langConfig, pluginHeader);
        this.fancyMessageSender = fancyMessageSender;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        configColor = ColorHelper.configColor(mainConfig);
        configIcon = StringHelper.unescapeJava(mainConfig.getString("Icon"));

        langCommandList = header("/aach list").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-list"))));
        langCommandListHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-list-hover")));
        langCommandTop = header("/aach top").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-top"))));
        langCommandTopHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-top-hover")));
        langCommandInfo = header("/aach info").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-info"))));
        langCommandInfoHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-info-hover")));
        langCommandBook = header("/aach book").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-book"))));
        langCommandBookHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-book-hover")));
        langCommandWeek = header("/aach week").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-week"))));
        langCommandWeekHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-week-hover")));
        langCommandStats = header("/aach stats").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-stats"))));
        langCommandStatsHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-stats-hover")));
        langCommandMonth = header("/aach month").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-month"))));
        langCommandMonthHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-month-hover")));
        langCommandToggle = header("/aach toggle").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-toggle"))));
        langCommandToggleHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-toggle-hover")));
        langCommandReload = header("/aach reload").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-reload"))));
        langCommandReloadHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-reload-hover")));
        langCommandGenerate = header("/aach generate").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-generate"))));
        langCommandGenerateHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-generate-hover")));
        langCommandInspect = header("/aach inspect").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-inspect"))));
        langCommandInspectHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-inspect-hover")));
        langCommandGive = header("/aach give player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-give"))));
        langCommandGiveHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-give-hover")));
        langCommandAdd = header("/aach add 1 cat player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-add"))));
        langCommandAddHover = Component.text(Objects.requireNonNull(Objects.requireNonNull(langConfig.getString("aach-command-add-hover"))));
        langCommandReset = header("/aach reset cat player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-reset"))));
        langCommandResetHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-reset-hover")));
        langCommandCheck = header("/aach check cat player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-check"))));
        langCommandCheckHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-check-hover")));
        langCommandDelete = header("/aach delete cat player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-delete"))));
        langCommandDeleteHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-delete-hover")));
        langCommandGrant = header("/aach grant cat player").append(Component.text(Objects.requireNonNull(langConfig.getString("aach-command-grant"))));
        langCommandGrantHover = Component.text(Objects.requireNonNull(langConfig.getString("aach-command-grant-hover")));

        langTip = ColorHelper.convertAmpersandToComponent(langConfig.getString("aach-tip"));
    }

    private @NonNull TextComponent header(String command) {
        return Component.text().append(pluginHeader).append(Component.text(command).color(configColor)).append(Component.text(" > ").color(NamedTextColor.GRAY)).build();
    }

    @Override
    void onExecute(@NonNull CommandSender sender, String[] args) {
        sender.sendMessage(Component.text().append(Component.text("------------ " + configIcon + " ", configColor)).append(Component.text("Advanced Achievements", configColor).decorate(TextDecoration.BOLD)).append(Component.text(" " + configIcon + " ------------", configColor)).build());

        if (sender.hasPermission("achievement.list")) {
            sendJsonClickableHoverableMessage(sender, langCommandList, "/aach list", langCommandListHover);
        }
        if (sender.hasPermission("achievement.top")) {
            sendJsonClickableHoverableMessage(sender, langCommandTop, "/aach top", langCommandTopHover);
        }
        sendJsonClickableHoverableMessage(sender, langCommandInfo, "/aach info", langCommandInfoHover);
        if (sender.hasPermission("achievement.book")) {
            sendJsonClickableHoverableMessage(sender, langCommandBook, "/aach book", langCommandBookHover);
        }
        if (sender.hasPermission("achievement.week")) {
            sendJsonClickableHoverableMessage(sender, langCommandWeek, "/aach week", langCommandWeekHover);
        }
        if (sender.hasPermission("achievement.stats")) {
            sendJsonClickableHoverableMessage(sender, langCommandStats, "/aach stats", langCommandStatsHover);
        }
        if (sender.hasPermission("achievement.month")) {
            sendJsonClickableHoverableMessage(sender, langCommandMonth, "/aach month", langCommandMonthHover);
        }
        if (sender.hasPermission("achievement.toggle")) {
            sendJsonClickableHoverableMessage(sender, langCommandToggle, "/aach toggle", langCommandToggleHover);
        }
        if (sender.hasPermission("achievement.reload")) {
            sendJsonClickableHoverableMessage(sender, langCommandReload, "/aach reload", langCommandReloadHover);
        }
        if (sender.hasPermission("achievement.generate")) {
            sendJsonClickableHoverableMessage(sender, langCommandGenerate, "/aach generate", langCommandGenerateHover);
        }
        if (sender.hasPermission("achievement.inspect")) {
            sendJsonClickableHoverableMessage(sender, langCommandInspect, "/aach inspect ach", langCommandInspectHover);
        }
        if (sender.hasPermission("achievement.give")) {
            sendJsonClickableHoverableMessage(sender, langCommandGive, "/aach give ach name", langCommandGiveHover);
        }
        if (sender.hasPermission("achievement.add")) {
            sendJsonClickableHoverableMessage(sender, langCommandAdd, "/aach add x cat name", langCommandAddHover);
        }
        if (sender.hasPermission("achievement.reset")) {
            sendJsonClickableHoverableMessage(sender, langCommandReset, "/aach reset cat name", langCommandResetHover);
        }
        if (sender.hasPermission("achievement.check")) {
            sendJsonClickableHoverableMessage(sender, langCommandCheck, "/aach check ach name", langCommandCheckHover);
        }
        if (sender.hasPermission("achievement.delete")) {
            sendJsonClickableHoverableMessage(sender, langCommandDelete, "/aach delete ach name", langCommandDeleteHover);
        }
        if (sender.hasPermission("achievement.grant")) {
            sendJsonClickableHoverableMessage(sender, langCommandGrant, "/aach grant ach name", langCommandGrantHover);
        }

        sender.sendMessage(Component.text(" "));
        sender.sendMessage(langTip);
    }

    /**
     * Sends a packet message to the server in order to display a clickable and hoverable message. A suggested command
     * is displayed in the chat when clicked on, and an additional help message appears when a command is hovered.
     *
     * @param sender  sender
     * @param message message
     * @param command command
     * @param hover   hover
     */
    private void sendJsonClickableHoverableMessage(CommandSender sender, Component message, String command, Component hover) {
        if (sender instanceof Player player) fancyMessageSender.sendHoverableCommandMessage(player, message, command, hover);
        else sender.sendMessage(message);
    }
}