package com.hm.achievement.command.executable;

import com.hm.achievement.config.PluginHeader;
import com.hm.achievement.lifecycle.Reloadable;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 *
 * @author Pyves
 */
public abstract class AbstractCommand implements Reloadable {

    final YamlConfiguration mainConfig;
    final YamlConfiguration langConfig;
    final PluginHeader pluginHeader;

    private String langNoPermissions;

    AbstractCommand(YamlConfiguration mainConfig, YamlConfiguration langConfig, PluginHeader pluginHeader) {
        this.mainConfig = mainConfig;
        this.langConfig = langConfig;
        this.pluginHeader = pluginHeader;
    }

    @Override
    public void extractConfigurationParameters() {
        langNoPermissions = pluginHeader.toString() + langConfig.getString("no-permissions");
    }

    /**
     * Executes the command issued by the sender if he has the relevant permissions. If permission null, skip check.
     *
     * @param sender
     * @param args
     */
    public void execute(CommandSender sender, String[] args) {
        String permission = getClass().getAnnotation(CommandSpec.class).permission();
        if (!permission.isEmpty() && !sender.hasPermission("achievement." + permission)) {
            sender.sendMessage(langNoPermissions);
            return;
        }

        onExecute(sender, args);
    }

    /**
     * Executes behaviour specific to the implementing command.
     *
     * @param sender
     * @param args
     */
    abstract void onExecute(CommandSender sender, String[] args);

    protected Component replace(@NonNull Component component, String placeholder, String value) {
        return component.replaceText(b -> b.matchLiteral(placeholder).replacement(value));
    }

    protected Component replace(@NonNull Component component, String @NonNull [] placeholders, String[] values) {
        for (int i = 0; i < placeholders.length; i++) component = replace(component, placeholders[i], values[i]);
        return component;
    }
}
