package me.kalmemarq.jgame.client.resource;

public class ResourceManager {
    public Resource getResource(String path) {
        return new Resource(ResourceManager.class.getResourceAsStream("/" + path));
    }
}
