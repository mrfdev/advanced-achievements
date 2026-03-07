package com.hm.achievement.exception;

import java.io.Serial;

/**
 * Checked exception thrown if the plugin encounters a non-recoverable error during load time.
 *
 * @author Pyves
 */
public class PluginLoadError extends Exception {

    @Serial
    private static final long serialVersionUID = -2223221493185030224L;

    public PluginLoadError(String message, Exception e) {
        super(message, e);
    }

    public PluginLoadError(String message) {
        super(message);
    }
}
