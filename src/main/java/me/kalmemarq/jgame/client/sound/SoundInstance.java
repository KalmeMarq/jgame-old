package me.kalmemarq.jgame.client.sound;

public class SoundInstance {
    private final String path;
    private final float volume;
    private final float pitch;
    private final double x;
    private final double y;
    private final double z;
    private final boolean relative;

    public SoundInstance(String path, float volume, float pitch, double x, double y, double z, boolean relative) {
        this.path = path;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.relative = relative;
    }

    public String getPath() {
        return this.path;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public boolean isRelative() {
        return this.relative;
    }

    public void tick() {
    }
}
