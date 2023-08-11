package me.kalmemarq.jgame.common;

import java.nio.file.Path;

public class PathUtils {
    public static Path join(Path parent, String... children) {
        return Path.of(parent.toString(), children);
    }
}
