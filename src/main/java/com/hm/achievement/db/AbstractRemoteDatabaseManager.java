package com.hm.achievement.db;

import jakarta.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class used to handle a remote (in the sense not managed by the plugin) database.
 *
 * @author Pyves
 */
public class AbstractRemoteDatabaseManager extends AbstractDatabaseManager {

    private final String databaseType;
    volatile String databaseAddress;
    volatile String databaseUser;
    volatile String databasePassword;
    volatile String additionalConnectionOptions;

    public AbstractRemoteDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
                                         DatabaseUpdater databaseUpdater, String driverPath, String databaseType, ExecutorService writeExecutor) {
        super(mainConfig, logger, databaseUpdater, driverPath, writeExecutor);
        this.databaseType = databaseType;
    }

    @Override
    void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
        Class.forName(driverPath);

        databaseAddress = getDatabaseAddress();
        databaseUser = URLEncoder.encode(Objects.requireNonNull(mainConfig.getString("DatabaseUser")), UTF_8);
        databasePassword = URLEncoder.encode(Objects.requireNonNull(mainConfig.getString("DatabasePassword")), UTF_8);
        additionalConnectionOptions = mainConfig.getString("AdditionalConnectionOptions");
    }

    @Override
    Connection createConnection() throws SQLException {
        return DriverManager.getConnection(databaseAddress + "?autoReconnect=true" + additionalConnectionOptions + "&user="
                + databaseUser + "&password=" + databasePassword);
    }

    private String getDatabaseAddress() {
        String databaseAddress = mainConfig.getString("DatabaseAddress");
        // Attempt to deal with common address mistakes where prefixes such as jdbc: or jdbc:mysql:// are omitted.
        if (!Objects.requireNonNull(databaseAddress).startsWith("jdbc:")) {
            if (databaseAddress.startsWith(databaseType + "://")) {
                return "jdbc:" + databaseAddress;
            } else {
                return "jdbc:" + databaseType + "://" + databaseAddress;
            }
        }
        return databaseAddress;
    }
}
