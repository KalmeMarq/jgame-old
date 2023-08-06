package me.kalmemarq.jgame.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

public class StringHelper {
    public static String readString(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Exception ignored) {
        }
        return builder.toString();
    }

    public static boolean isValidString(String str, Predicate<Character> predicate) {
        for (int i = 0; i < str.length(); ++i) {
            if (!predicate.test(str.charAt(i))) return false;
        }
        return true;
    }
}
