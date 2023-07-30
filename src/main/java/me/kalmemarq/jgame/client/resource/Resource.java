package me.kalmemarq.jgame.client.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Resource {
    private final InputStream inputStream;
    
    public Resource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getAsInputStream() {
        return this.inputStream;
    }

    public BufferedReader getAsReader() {
        return new BufferedReader(new InputStreamReader(this.inputStream, StandardCharsets.UTF_8));
    }

    public String getAsString() throws IOException {
        try (BufferedReader reader = this.getAsReader()) {
            return String.join("\n", this.getAsReader().lines().toArray(String[]::new));
        }
    }
}
