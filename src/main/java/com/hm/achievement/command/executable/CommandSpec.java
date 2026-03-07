package com.hm.achievement.command.executable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation providing information about the plugin's commands.
 *
 * @author Pyves
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandSpec {

    String name();

    String permission();

    int minArgs();

    int maxArgs();
}
