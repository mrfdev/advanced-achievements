package com.hm.achievement.command.executable;

import com.hm.achievement.utils.FancyMessageSender;
import com.hm.achievement.utils.StringHelper;
import java.util.Objects;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.ChatColor;
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

    private ChatColor configColor;
    private String configIcon;

    private String langCommandList;
    private String langCommandListHover;
    private String langCommandTop;
    private String langCommandTopHover;
    private String langCommandInfo;
    private String langCommandInfoHover;
    private String langCommandBook;
    private String langCommandBookHover;
    private String langCommandWeek;
    private String langCommandWeekHover;
    private String langCommandStats;
    private String langCommandStatsHover;
    private String langCommandMonth;
    private String langCommandMonthHover;
    private String langCommandToggleHover;
    private String langCommandToggle;
    private String langCommandReload;
    private String langCommandReloadHover;
    private String langCommandGenerate;
    private String langCommandGenerateHover;
    private String langCommandInspect;
    private String langCommandInspectHover;
    private String langCommandGive;
    private String langCommandGiveHover;
    private String langCommandAdd;
    private String langCommandAddHover;
    private String langCommandReset;
    private String langCommandResetHover;
    private String langCommandCheck;
    private String langCommandCheckHover;
    private String langCommandDelete;
    private String langCommandDeleteHover;
    private String langCommandGrant;
    private String langCommandGrantHover;
    private String langTip;

    @Inject
    public HelpCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, StringBuilder pluginHeader, FancyMessageSender fancyMessageSender) {
        super(mainConfig, langConfig, pluginHeader);
        this.fancyMessageSender = fancyMessageSender;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();

        configColor = ChatColor.getByChar(Objects.requireNonNull(mainConfig.getString("Color")));
        configIcon = StringHelper.unescapeJava(mainConfig.getString("Icon"));

        langCommandList = header("/aach list") + langConfig.getString("aach-command-list");
        langCommandListHover = langConfig.getString("aach-command-list-hover");
        langCommandTop = header("/aach top") + langConfig.getString("aach-command-top");
        langCommandTopHover = langConfig.getString("aach-command-top-hover");
        langCommandInfo = header("/aach info") + langConfig.getString("aach-command-info");
        langCommandInfoHover = langConfig.getString("aach-command-info-hover");
        langCommandBook = header("/aach book") + langConfig.getString("aach-command-book");
        langCommandBookHover = langConfig.getString("aach-command-book-hover");
        langCommandWeek = header("/aach week") + langConfig.getString("aach-command-week");
        langCommandWeekHover = langConfig.getString("aach-command-week-hover");
        langCommandStats = header("/aach stats") + langConfig.getString("aach-command-stats");
        langCommandStatsHover = langConfig.getString("aach-command-stats-hover");
        langCommandMonth = header("/aach month") + langConfig.getString("aach-command-month");
        langCommandMonthHover = langConfig.getString("aach-command-month-hover");
        langCommandToggle = header("/aach toggle") + langConfig.getString("aach-command-toggle");
        langCommandToggleHover = langConfig.getString("aach-command-toggle-hover");
        langCommandReload = header("/aach reload") + langConfig.getString("aach-command-reload");
        langCommandReloadHover = langConfig.getString("aach-command-reload-hover");
        langCommandGenerate = header("/aach generate") + langConfig.getString("aach-command-generate");
        langCommandGenerateHover = langConfig.getString("aach-command-generate-hover");
        langCommandGive = header("/aach give player") + langConfig.getString("aach-command-give");
        langCommandInspect = header("/aach inspect") + langConfig.getString("aach-command-inspect");
        langCommandInspectHover = langConfig.getString("aach-command-inspect-hover");
        langCommandGiveHover = langConfig.getString("aach-command-give-hover");
        langCommandAdd = header("/aach add 1 cat player") + langConfig.getString("aach-command-add");
        langCommandAddHover = langConfig.getString("aach-command-add-hover");
        langCommandReset = header("/aach reset cat player") + langConfig.getString("aach-command-reset");
        langCommandResetHover = langConfig.getString("aach-command-reset-hover");
        langCommandCheck = header("/aach check cat player") + langConfig.getString("aach-command-check");
        langCommandCheckHover = langConfig.getString("aach-command-check-hover");
        langCommandDelete = header("/aach delete cat player") + langConfig.getString("aach-command-delete");
        langCommandDeleteHover = langConfig.getString("aach-command-delete-hover");
        langCommandGrant = header("/aach grant cat player") + langConfig.getString("aach-command-grant");
        langCommandGrantHover = langConfig.getString("aach-command-grant-hover");

        langTip = ChatColor.GRAY + translateColorCodes(langConfig.getString("aach-tip"));
    }

    private @NonNull String header(String command) {
        return pluginHeader.toString() + configColor + command + ChatColor.GRAY + " > ";
    }

    @Override
    void onExecute(@NonNull CommandSender sender, String[] args) {
        // Header.
        sender.sendMessage(configColor + "------------ " + configIcon + translateColorCodes(" &lAdvanced Achievements ") + configColor + configIcon + configColor + " ------------");

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

        // Empty line.
        sender.sendMessage(configColor + " ");

        sender.sendMessage(langTip);
    }

    /**
     * Sends a packet message to the server in order to display a clickable and hoverable message. A suggested command
     * is displayed in the chat when clicked on, and an additional help message appears when a command is hovered.
     *
     * @param sender
     * @param message
     * @param command
     * @param hover
     */
    private void sendJsonClickableHoverableMessage(CommandSender sender, String message, String command, String hover) {
        // Send clickable and hoverable message if sender is a player.
        if (sender instanceof Player) {
            fancyMessageSender.sendHoverableCommandMessage((Player) sender, message, command, hover, configColor.name().toLowerCase());
        } else {
            sender.sendMessage(message);
        }
    }
}
