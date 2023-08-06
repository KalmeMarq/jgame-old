package me.kalmemarq.jgame.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import me.kalmemarq.jgame.common.JacksonHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameOptions {
    public final Option<Boolean> vsync = new Option<>("vsync", true);
    public final Option<Boolean> sound = new Option<>("sound", true);
    private final Path optionsFile;

    public GameOptions(Path gameDir) {
        this.optionsFile = Paths.get(gameDir.toString(), "settings.json");
    }

    public void load() {
        if (!Files.exists(this.optionsFile)) return;

        try {
            ObjectNode root = JacksonHelper.OBJECT_MAPPER.readValue(Files.readString(this.optionsFile), ObjectNode.class);

            if (root.hasNonNull(this.vsync.getKey())) {
                this.vsync.setValue(root.get(this.vsync.getKey()).booleanValue());
            }
            
            if (root.hasNonNull(this.sound.getKey())) {
                this.sound.setValue(root.get(this.sound.getKey()).booleanValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        ObjectNode root = JacksonHelper.OBJECT_MAPPER.createObjectNode();
        root.put(this.vsync.getKey(), this.vsync.getValue());
        root.put(this.sound.getKey(), this.sound.getValue());

        try {
            Files.writeString(this.optionsFile, root.toPrettyString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Option<T> {
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
