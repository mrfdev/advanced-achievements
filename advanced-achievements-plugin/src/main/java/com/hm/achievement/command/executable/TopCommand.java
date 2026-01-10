package com.hm.achievement.command.executable;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.utils.SoundPlayer;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class in charge of handling the /aach top command, which displays global rankings.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "top", permission = "top", minArgs = 1, maxArgs = 2)
public class TopCommand extends AbstractRankingCommand {

    @Inject
    public TopCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
                      StringBuilder pluginHeader, Logger logger, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
        super(mainConfig, langConfig, pluginHeader, logger, "top-achievement", databaseManager, soundPlayer);
    }

    @Override
    long getRankingStartTime() {
        // All time ranking, no time start.
        return 0L;
    }
}
