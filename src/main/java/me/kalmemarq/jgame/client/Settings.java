package me.kalmemarq.jgame.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import me.kalmemarq.jgame.common.JacksonHelper;
import me.kalmemarq.jgame.common.MathUtils;
import me.kalmemarq.jgame.common.Observable;
import me.kalmemarq.jgame.common.PathUtils;
import me.kalmemarq.jgame.common.Stringifiable;
import me.kalmemarq.jgame.common.logger.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Settings {
    private static final Logger LOGGER = Logger.getLogger();
    
    public final Option<Boolean> vsync = new BooleanOption("vsync", true).subscribe(value -> {
        Client.getInstance().window.setVsync(value);
    });
    public final Option<Boolean> sound = new BooleanOption("sound", true);
    public final Option<Integer> fps = new IntRangeOption("fps", 60, 30, 260);
    
    private final List<Option<?>> options = new ArrayList<>();
    protected boolean debugLWJGL;
    protected boolean debugImGui;
    private final Path settingsPath;

    public Settings(Path gameSavePath) {
        this.settingsPath = PathUtils.join(gameSavePath, "settings.json");
        this.options.add(this.vsync);
        this.options.add(this.sound);
        this.options.add(this.fps);
    }

    public void load() {
        if (!Files.exists(this.settingsPath)) return;

        try {
            ObjectNode root = JacksonHelper.OBJECT_MAPPER.readValue(Files.readString(this.settingsPath), ObjectNode.class);

            for (Option<?> option : this.options) {
                if (root.hasNonNull(option.getKey())) {
                    option.parseValue(root.get(option.getKey()).textValue());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load settings: {}", e);
        }
    }

    public void save() {
        ObjectNode root = JacksonHelper.OBJECT_MAPPER.createObjectNode();
        
        for (Option<?> option : this.options) {
            root.put(option.getKey(), option.getStringifiedValue());
        }

        try {
            Files.writeString(this.settingsPath, root.toPrettyString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Failed to save settings: {}", e);
        }
    }

    public static abstract class Option<T> implements Observable<T> {
        protected final List<Consumer<T>> listeners = new ArrayList<>();
        protected final String key;
        protected T value;

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
        
        abstract void parseValue(String value);
        
        public String getStringifiedValue() {
            return String.valueOf(this.value);
        }

        public void setValue(T value) {
            T oldValue = this.value;
            this.value = value;
            if (this.value != oldValue) {
                this.notifyAllSubscribers();
            }
        }

        @Override
        public Option<T> subscribe(Consumer<T> listener) {
            this.listeners.add(listener);
            return this;
        }

        @Override
        public void unsubscribe(Consumer<T> listener) {
            this.listeners.remove(listener);
        }

        @Override
        public void notifyAllSubscribers() {
            for (Consumer<T> listener : this.listeners) {
                listener.accept(this.value);
            }
        }
    }

    public static class BooleanOption extends Option<Boolean> {
        public BooleanOption(String key, boolean value) {
            super(key, value);
        }

        @Override
        public void parseValue(String value) {
            this.value = "true".equals(value);
        }
    }
    
    public static class EnumOption<E extends Enum<E> & Stringifiable> extends Option<E> {
        private final List<E> values;
        
        public EnumOption(String key, E value, List<E> values) {
            super(key, value);
            this.values = values;
        }

        @Override
        public void parseValue(String value) {
            for (E vl : this.values) {
                if (vl.getName().equals(value)) {
                    this.value = vl;
                }
            }
        }
    }
    
    public static class IntRangeOption extends Option<Integer> {
        private final int min;
        private final int max;
        
        public IntRangeOption(String key, int value, int min, int max) {
            super(key, MathUtils.clamp(value, min, max));
            this.min = min;
            this.max = max;
        }

        @Override
        public void setValue(Integer value) {
            super.setValue(MathUtils.clamp(value, this.min, this.max));
        }

        @Override
        public void parseValue(String value) {
            try {
                int vl = Integer.parseInt(value);
                this.value = MathUtils.clamp(vl, this.min, this.max);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}