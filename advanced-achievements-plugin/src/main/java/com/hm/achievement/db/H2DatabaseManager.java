package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import jakarta.inject.Named;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

/**
 * Class used to handle an H2 database.
 *
 * @author Pyves
 */
public class H2DatabaseManager extends AbstractFileDatabaseManager {

    public H2DatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger, DatabaseUpdater databaseUpdater, AdvancedAchievements advancedAchievements, ExecutorService writeExecutor) {
        super(mainConfig, logger, databaseUpdater, advancedAchievements, "org.h2.Driver", buildUrl(new File(advancedAchievements.getDataFolder(), "achievements")), "achievements.mv.db", writeExecutor);
        @SuppressWarnings("unused") Class<?>[] classes = new Class<?>[]{org.h2.engine.Engine.class};
    }

    private static @NonNull String buildUrl(@NonNull File dbFile) {
        String path = dbFile.toPath().toAbsolutePath().normalize().toString().replace('\\', '/');
        return "jdbc:h2:file:" + path + ";DATABASE_TO_UPPER=false" + ";MODE=MySQL";
    }
}
