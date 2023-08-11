package me.kalmemarq.jgame.common;

public final class MathUtils {
    private MathUtils() {
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
    
    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
}
