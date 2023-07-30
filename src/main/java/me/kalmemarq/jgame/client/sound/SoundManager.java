package me.kalmemarq.jgame.client.sound;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.Util;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SoundManager implements Destroyable {
    protected static final Logger LOGGER = Logger.getLogger();

    private long device = -1L;
    private long context = -1L;
    private boolean initialized;

    private final Map<String, SoundBuffer> buffers = new HashMap<>();
    private final Map<SoundInstance, SoundSource> sources = new HashMap<>();

    public void init() {
        LOGGER.info("Initializing OpenAL");

        this.device = ALC10.alcOpenDevice((ByteBuffer) null);

        if (this.device == 0L) {
            this.device = -1;
            LOGGER.error("Failed to open default OpenAL device");
            return;
        }

        ALCCapabilities capabilities = ALC.createCapabilities(this.device);
        this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);

        if (this.context == 0L) {
            this.context = -1;
            LOGGER.error("Failed to create OpenAL context");
            return;
        }

        ALC10.alcMakeContextCurrent(this.context);
        AL.createCapabilities(capabilities);

        this.initialized = true;

        LOGGER.info("OpenAL {} initialized!", AL10.alGetString(AL10.AL_VERSION));

        LOGGER.info("Using OpenAL: {} by {}", AL10.alGetString(AL10.AL_RENDERER), AL10.alGetString(AL10.AL_VENDOR));
        LOGGER.info("Current device: {}", this.getCurrentDeviceName());
        LOGGER.info("Available devices: {}", this.getDevices().stream().reduce("", (res, vl) -> {
            return res + (res.isEmpty() ? "" :  ", ") + vl;
        }));
    }

    public String getCurrentDeviceName() {
        String name = ALC10.alcGetString(this.device, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        if (name == null) {
            name = ALC10.alcGetString(this.device, ALC10.ALC_DEVICE_SPECIFIER);
        }
        if (name == null) {
            name = "Unknown";
        }
        return name;
    }

    public List<String> getDevices() {
        return Objects.requireNonNullElse(ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER), Collections.emptyList());
    }

    public void play(SoundInstance instance) {
        if (!this.initialized) return;
        if (!Client.getInstance().options.sound.getValue()) return;

        float volume = Util.clamp(instance.getVolume(), 0.0f, 1.0f);

        if (volume == 0.0f) {
            return;
        }

        float pitch = Util.clamp(instance.getVolume(), 0.5f, 2.0f);

        SoundSource source = new SoundSource();
        source.setVolume(volume);
        source.setVolume(pitch);
        source.setPosition(instance.getX(), instance.getY(), instance.getZ());

        SoundBuffer buffer = this.buffers.get(instance.getPath());
        if (buffer == null) {
            buffer = new SoundBuffer(instance.getPath());
            this.buffers.put(instance.getPath(), buffer);
        }

        source.setBuffer(buffer);
        source.play();

        this.sources.put(instance, source);
    }

    public void tick() {
        Iterator<Map.Entry<SoundInstance, SoundSource>> iterator = this.sources.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<SoundInstance, SoundSource> entry = iterator.next();
            SoundSource source = entry.getValue();

            if (source.isStopped()) {
                iterator.remove();
            }
        }
    }

    public void stopAll() {
        if (!this.initialized) return;

        this.sources.values().forEach(SoundSource::stop);
        this.sources.clear();
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying OpenAL contexts and devices");

        LOGGER.debug("Remaining sources: {}", this.sources.size());

        this.sources.values().forEach(SoundSource::destroy);
        this.sources.clear();
        this.buffers.values().forEach(SoundBuffer::destroy);
        this.buffers.clear();

        if (this.context != -1) {
            ALC10.alcMakeContextCurrent(0L);
            ALC10.alcDestroyContext(this.context);

            this.context = -1;
        }

        if (this.device != -1) {
            boolean succ = ALC10.alcCloseDevice(this.device);

            if (!succ) {
                LOGGER.error("Failed to close device. Not everything was destroyed properly.");
            }

            this.device = -1;
        }
    }
}
