package me.kalmemarq.jgame.client.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class Resource {
    private final Supplier<InputStream> supplier;
    
    public Resource(Supplier<InputStream> supplier) {
        this.supplier = supplier;
    }

    public InputStream getAsInputStream() {
        return this.supplier.get();
    }

    public BufferedReader getAsReader() {
        return new BufferedReader(new InputStreamReader(this.supplier.get(), StandardCharsets.UTF_8));
    }

    public String getAsString() throws IOException {
        try (BufferedReader reader = this.getAsReader()) {
            return String.join("\n", this.getAsReader().lines().toArray(String[]::new));
        }
    }
}
