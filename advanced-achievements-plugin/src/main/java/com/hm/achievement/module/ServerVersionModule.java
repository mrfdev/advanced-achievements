package com.hm.achievement.module;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.bukkit.Bukkit;

@Module
public class ServerVersionModule {

    @Provides
    @Singleton
    int provideServerVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String versionIdent = bukkitVersion.split("-")[0];
        String[] parts = versionIdent.split("\\.");
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse server version: " + bukkitVersion);
            }
        }
        throw new IllegalArgumentException("Unexpected Bukkit version format: " + bukkitVersion);
    }
}