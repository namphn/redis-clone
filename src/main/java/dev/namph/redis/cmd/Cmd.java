package dev.namph.redis.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Redis commands.
 * This annotation is used to mark classes as Redis commands with a specific name and minimum argument count.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cmd {
    String name();
    int minArgs() default 1;
}
