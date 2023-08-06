package me.kalmemarq.jgame.common;

public class MathHelper {
    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
}
