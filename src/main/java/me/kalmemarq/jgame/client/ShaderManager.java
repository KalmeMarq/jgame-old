package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.client.resource.PreparationResourceReloader;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.Util;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager extends PreparationResourceReloader<Map<String, String[]>> implements Destroyable {
    // ^#import[ \t]+(\"|<)([^>\"]+)(?:\"|>)

    private static Shader POSITION_COLOR_SHADER;
    private static Shader POSITION_COLOR_TEXTURE_SHADER;
    private static final Map<String, Shader> SHADERS = new HashMap<>();

    @Override
    protected Map<String, String[]> prepare() {
        Map<String, String[]> shaders = new HashMap<>();
        shaders.put("position_color", new String[]{ Util.readString(ShaderManager.class.getResourceAsStream("/position_color.vsh")), Util.readString(ShaderManager.class.getResourceAsStream("/position_color.fsh")) });
        shaders.put("position_texture_color", new String[]{ Util.readString(ShaderManager.class.getResourceAsStream("/position_color.vsh")), Util.readString(ShaderManager.class.getResourceAsStream("/position_color.fsh")) });
        return shaders;
    }

    @Override
    protected void apply(Map<String, String[]> prepared) {
        for (Map.Entry<String, String[]> entry : prepared.entrySet()) {
            String name = entry.getKey();
            String vertexSource = entry.getValue()[0];
            String fragmentSource = entry.getValue()[1];

            if (name.equals("position_color")) {
                POSITION_COLOR_SHADER.destroy();
                POSITION_COLOR_SHADER = new Shader(name, vertexSource, fragmentSource);
            } else if (name.equals("position_texture_color")) {
                POSITION_COLOR_TEXTURE_SHADER.destroy();
                POSITION_COLOR_TEXTURE_SHADER = new Shader(name, vertexSource, fragmentSource);
            } else {
                if (SHADERS.containsKey(name)) SHADERS.get(name).destroy();
                SHADERS.put(name, new Shader(name, vertexSource, fragmentSource));
            }
        }
    }

    public static Shader getPositionColorShader() {
        return POSITION_COLOR_SHADER;
    }

    public static Shader getPositionTextureColorShader() {
        return POSITION_COLOR_TEXTURE_SHADER;
    }

    public static Shader getShader(String name) {
        return SHADERS.get(name);
    }

    @Override
    public void destroy() {
        POSITION_COLOR_SHADER.destroy();
        POSITION_COLOR_TEXTURE_SHADER.destroy();
    }
}
