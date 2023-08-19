package me.kalmemarq.jgame.client.sound;

import me.kalmemarq.jgame.client.MemoryUtils;
import me.kalmemarq.jgame.client.resource.Resource;
import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class SoundBuffer implements Destroyable {
    public int id;
    
    public SoundBuffer(String path, ResourceManager resourceManager) {
        this.id = AL10.alGenBuffers();

        MemoryStack.stackPush();
        IntBuffer pChannels = MemoryStack.stackMallocInt(1);
        MemoryStack.stackPush();
        IntBuffer pSampleRate = MemoryStack.stackMallocInt(1);

        Resource resource = resourceManager.getResource(path);

        ByteBuffer buffer;
        try {
            buffer = MemoryUtils.readAsByteBuffer(resource.getAsInputStream());
            buffer.flip();
        } catch (Exception e) {
            SoundManager.LOGGER.error("Failed to read sound: {}", e);
            return;
        }
        
        ShortBuffer b = STBVorbis.stb_vorbis_decode_memory(buffer, pChannels, pSampleRate);
        
        MemoryUtil.memFree(buffer);
        
        if (b == null) {
            MemoryStack.stackPop();
            MemoryStack.stackPop();
            SoundManager.LOGGER.error("Failed to decode sound buffer {}", path);
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
