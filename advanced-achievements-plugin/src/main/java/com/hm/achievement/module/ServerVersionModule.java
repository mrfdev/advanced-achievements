package com.hm.achievement.module;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;

@Module
public class ServerVersionModule {

    @Provides
    @Singleton
    int provideServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        String versionIdentifier = StringUtils.substringBetween(packageName, "v", "_");
        if (versionIdentifier != null && !versionIdentifier.isEmpty()) {
            try {
                return Integer.parseInt(versionIdentifier);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse version number: " + versionIdentifier, e);
            }
        }
        versionIdentifier = StringUtils.substringAfterLast(packageName, "v");
        if (versionIdentifier.matches("\\d+_\\d+_R\\d+")) {
            try {
                return Integer.parseInt(versionIdentifier.split("_")[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse version number from vX_XX_RX format: " + versionIdentifier, e);
            }
        }
        if (packageName.equals("org.bukkit.craftbukkit")) {
            String bukkitVersion = Bukkit.getBukkitVersion();
            versionIdentifier = StringUtils.substringBefore(bukkitVersion, "-");
            try {
                return Integer.parseInt(versionIdentifier.split("\\.")[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse version number from Bukkit version: " + bukkitVersion, e);
            }
        }
        throw new IllegalArgumentException("Unexpected package name format: " + packageName);
    }
}