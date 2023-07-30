package me.kalmemarq.jgame.client.sound;

import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;

public class SoundBuffer implements Destroyable {
    public int id;

    public SoundBuffer(String path) {
        this.id = AL10.alGenBuffers();

        MemoryStack.stackPush();
        IntBuffer pChannels = MemoryStack.stackMallocInt(1);
        MemoryStack.stackPush();
        IntBuffer pSampleRate = MemoryStack.stackMallocInt(1);

        URL resource = SoundBuffer.class.getResource(path);
        File p;
        try {
            p = Paths.get(resource.toURI()).toFile();
        } catch (URISyntaxException e) {
            SoundManager.LOGGER.error("Failed to get resource url for  " + path);
            e.printStackTrace();
            MemoryStack.stackPop();
            MemoryStack.stackPop();
            return;
        }

        ShortBuffer b = STBVorbis.stb_vorbis_decode_filename(p.getAbsolutePath(), pChannels, pSampleRate);

        if (b == null) {
            MemoryStack.stackPop();
            MemoryStack.stackPop();
            SoundManager.LOGGER.error("Failed to decode sound buffer " + path);
            return;
        }

        int channels = pChannels.get();
        int sampleRate = pSampleRate.get();

        MemoryStack.stackPop();
        MemoryStack.stackPop();

        int format = -1;
        if (channels == 1) {
            format = AL10.AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL10.AL_FORMAT_STEREO16;
        }

        AL10.alBufferData(this.id, format, b, sampleRate);
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void destroy() {
        if (this.id != -1) {
            AL10.alDeleteBuffers(this.id);
            this.id = -1;
        }
    }
}
