package me.kalmemarq.client;

import me.kalmemarq.common.Destroyable;
import me.kalmemarq.common.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureManager implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();

    private final HashMap<String, Texture> textures = new HashMap<>();

    public void bindTexture(String path) {
        Texture txr = this.textures.get(path);

        if (txr == null) {
            txr = new Texture(path);
            txr.load();
            this.textures.put(path, txr);
        }

        txr.bind();
    }

    public Set<Map.Entry<String, Texture>> getTextures() {
        return this.textures.entrySet();
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying textures");

        for (Texture txr : this.textures.values()) {
            txr.destroy();
        }
    }
}