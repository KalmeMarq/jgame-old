package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.client.resource.SyncResourceReloader;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureManager extends SyncResourceReloader implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();

    private final HashMap<String, Texture> textures = new HashMap<>();
    private final ResourceManager resourceManager;
    
    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void bindTexture(String path) {
        Texture txr = this.textures.get(path);

        if (txr == null) {
            txr = new Texture(path);
            txr.load(this.resourceManager);
            this.textures.put(path, txr);
        }

        txr.bind();
    }

    public Set<Map.Entry<String, Texture>> getTextures() {
        return this.textures.entrySet();
    }
    
    public Texture getTexture(String path) {
        Texture txr = this.textures.get(path);

        if (txr == null) {
            txr = new Texture(path);
            txr.load(this.resourceManager);
            this.textures.put(path, txr);
        }

        return txr;
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying textures");

        for (Texture txr : this.textures.values()) {
            txr.destroy();
        }
    }

    @Override
    protected void reload(ResourceManager resourceManager) {
        for (Texture txr : this.textures.values()) {
            txr.load(resourceManager);
        }
    }
}