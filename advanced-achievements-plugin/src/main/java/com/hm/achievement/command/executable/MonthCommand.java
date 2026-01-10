package com.hm.achievement.command.executable;

import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.utils.SoundPlayer;
import java.util.Calendar;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class in charge of handling the /aach month command, which displays monthly rankings.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "month", permission = "month", minArgs = 1, maxArgs = 2)
public class MonthCommand extends AbstractRankingCommand {

    @Inject
    public MonthCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
                        StringBuilder pluginHeader, Logger logger, AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
        super(mainConfig, langConfig, pluginHeader, logger, "month-achievement", databaseManager, soundPlayer);
    }

    @Override
    long getRankingStartTime() {
        Calendar c = Calendar.getInstance();
        // Set calendar to the first day of the month.
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }
}
