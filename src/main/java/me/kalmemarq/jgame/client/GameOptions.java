package me.kalmemarq.jgame.client;

import java.io.File;

public class GameOptions {
    public final Option<Boolean> vsync = new Option<>("options.vsync", true);
    private File optionsFile;

    public GameOptions(File gameDir) {
        this.optionsFile = new File(gameDir, "settings.json");
    }

    public void load() {
        if (!this.optionsFile.exists()) return;
    }

    public void save() {
    }

    static class Option<T> {
        private final String key;
        private T value;

        public Option(String key, T value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
