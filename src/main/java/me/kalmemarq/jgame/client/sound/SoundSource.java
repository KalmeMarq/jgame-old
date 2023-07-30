package me.kalmemarq.jgame.client.sound;

import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.openal.AL10;

public class SoundSource implements Destroyable {
    private int id;

    public SoundSource() {
        this.id = AL10.alGenSources();
    }

    public void setBuffer(SoundBuffer buffer) {
        AL10.alSourcei(this.id, AL10.AL_BUFFER, buffer.getId());
    }

    public int getSourceState() {
        return AL10.alGetSourcei(this.id, AL10.AL_SOURCE_STATE);
    }

    public boolean isPlaying() {
        return this.getSourceState() == AL10.AL_PLAYING;
    }

    public boolean isStopped() {
        return this.getSourceState() == AL10.AL_STOPPED;
    }

    public void pause() {
        AL10.alSourcePause(this.id);
    }

    public void play() {
        AL10.alSourcePlay(this.id);
    }

    public void stop() {
        AL10.alSourceStop(this.id);
    }

    public void setPosition(double x, double y, double z) {
        AL10.alSource3f(this.id, AL10.AL_POSITION, (float) x, (float) y, (float) z);
    }

    public void setVelocity(float x, float y, float z) {
        AL10.alSource3f(this.id, AL10.AL_VELOCITY, x, y, z);
    }

    public void setVolume(float volume) {
        AL10.alSourcef(this.id, AL10.AL_GAIN, volume);
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(this.id, AL10.AL_PITCH, pitch);
    }

    public void setLooping(boolean loop) {
        AL10.alSourcei(this.id, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    @Override
    public void destroy() {
        if (this.id != -1) {
            this.stop();
            AL10.alDeleteSources(this.id);
            this.id = -1;
        }
    }
}

