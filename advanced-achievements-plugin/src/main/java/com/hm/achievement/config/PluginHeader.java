package com.hm.achievement.config;

import com.hm.achievement.utils.ColorHelper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.Component;

@Singleton
public class PluginHeader {

    private final StringBuilder legacyHeader;

    @Inject
    public PluginHeader(StringBuilder legacyHeader) {
        this.legacyHeader = legacyHeader;
    }

    public Component get() {
        return ColorHelper.convertAmpersandToComponent(legacyHeader.toString());
    }
}
