package com.hm.achievement.command.executable;

import com.hm.achievement.advancement.AdvancementManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class in charge of handling the /aach generate command, which creates advancements for the achievements defined in
 * the plugin's configuration.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "generate", permission = "generate", minArgs = 1, maxArgs = 1)
public class GenerateCommand extends AbstractCommand {

    private final AdvancementManager advancementManager;

    private Component langAdvancementsGenerated;

    @Inject
    public GenerateCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig, Component pluginHeader, AdvancementManager advancementManager) {
        super(mainConfig, langConfig, pluginHeader);
        this.advancementManager = advancementManager;
    }

    @Override
    public void extractConfigurationParameters() {
        super.extractConfigurationParameters();
        langAdvancementsGenerated = Component.text().append(pluginHeader).append(Component.text(Objects.requireNonNull(langConfig.getString("advancements-generated")))).build();
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        advancementManager.registerAdvancements();
        sender.sendMessage(langAdvancementsGenerated);
    }
}
