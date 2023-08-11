package me.kalmemarq.jgame.client.render.shader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.kalmemarq.jgame.client.resource.PreparationResourceReloader;
import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.JacksonHelper;
import me.kalmemarq.jgame.common.StringUtils;
import me.kalmemarq.jgame.common.logger.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderManager extends PreparationResourceReloader<Map<String, JsonNode>> implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();
    
    private static final Pattern INCLUDE_PATTERN = Pattern.compile("^#include\\s+((\"([a-zA-Z._]+)\")|(<([a-zA-Z._]+)>))", Pattern.MULTILINE);
    private static final Pattern SINGLELINE_COMMENT_PATTERN = Pattern.compile("//.*");
    private static final Pattern MULTILINE_COMMENT_PATTERN = Pattern.compile("(?<!/)/\\*((?:(?!\\*/).|\\s)*)\\*/");
    
    private static boolean loadedInitial;
    
    private static Shader BLIT_SHADER;
    private static Shader POSITION_COLOR_SHADER;
    private static Shader POSITION_COLOR_TEXTURE_SHADER;
    private static final Map<String, Shader> SHADERS = new HashMap<>();

    @Override
    protected Map<String, JsonNode> prepare(ResourceManager resourceManager) {
        Map<String, JsonNode> shaders = new HashMap<>();
        Map<String, String> cachedIncludes = new HashMap<>();

        try {
            ObjectNode dataPC = (ObjectNode) JacksonHelper.OBJECT_MAPPER.readTree(StringUtils.readString(resourceManager.getResource("assets/minicraft/shaders/blit.json").getAsInputStream()));
            dataPC.put("vertex", readSource(cachedIncludes, dataPC.get("vertex").textValue() + ".vsh", resourceManager));
            dataPC.put("fragment", readSource(cachedIncludes, dataPC.get("fragment").textValue() + ".fsh", resourceManager));
            if (dataPC.has("geometry")) {
                dataPC.put("geometry", readSource(cachedIncludes, dataPC.get("geometry").textValue() + ".gsh", resourceManager));
            }
            shaders.put("blit", dataPC);
        } catch (JsonProcessingException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            ObjectNode dataPC = (ObjectNode) JacksonHelper.OBJECT_MAPPER.readTree(StringUtils.readString(resourceManager.getResource("assets/minicraft/shaders/position_color.json").getAsInputStream()));
            dataPC.put("vertex", readSource(cachedIncludes, dataPC.get("vertex").textValue() + ".vsh", resourceManager));
            dataPC.put("fragment", readSource(cachedIncludes, dataPC.get("fragment").textValue() + ".fsh", resourceManager));
            if (dataPC.has("geometry")) {
                dataPC.put("geometry", readSource(cachedIncludes, dataPC.get("geometry").textValue() + ".gsh", resourceManager));
            }
            shaders.put("position_color", dataPC);
        } catch (JsonProcessingException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            ObjectNode dataPTC = (ObjectNode) JacksonHelper.OBJECT_MAPPER.readTree(StringUtils.readString(resourceManager.getResource("assets/minicraft/shaders/position_texture_color.json").getAsInputStream()));
            dataPTC.put("vertex", readSource(cachedIncludes, dataPTC.get("vertex").textValue() + ".vsh", resourceManager));
            dataPTC.put("fragment", readSource(cachedIncludes, dataPTC.get("fragment").textValue() + ".fsh", resourceManager));
            if (dataPTC.has("geometry")) {
                dataPTC.put("geometry", readSource(cachedIncludes, dataPTC.get("geometry").textValue() + ".gsh", resourceManager));
            }
            shaders.put("position_texture_color", dataPTC);
        } catch (JsonProcessingException | ClassCastException e) {
            e.printStackTrace();
        }

        return shaders;
    }

    @Override
    protected void apply(Map<String, JsonNode> prepared, ResourceManager resourceManager) {
        loadedInitial = true;

        LOGGER.debug("Loaded shaders: {}", prepared.size());
        
        for (Map.Entry<String, JsonNode> entry : prepared.entrySet()) {
            String name = entry.getKey();
            ObjectNode data = (ObjectNode) entry.getValue();
            String vertexSource = data.get("vertex").textValue();
            String fragmentSource = data.get("fragment").textValue();

            LOGGER.debug("Loaded shader '{}'", name);
            
            try {
                if (name.equals("blit")) {
                    if (BLIT_SHADER != null) BLIT_SHADER.destroy();
                    BLIT_SHADER = new Shader(name, data, vertexSource, fragmentSource);
                } else if (name.equals("position_color")) {
                    if (POSITION_COLOR_SHADER != null) POSITION_COLOR_SHADER.destroy();
                    POSITION_COLOR_SHADER = new Shader(name, data, vertexSource, fragmentSource);
                } else if (name.equals("position_texture_color")) {
                    if (POSITION_COLOR_TEXTURE_SHADER != null) POSITION_COLOR_TEXTURE_SHADER.destroy();
                    POSITION_COLOR_TEXTURE_SHADER = new Shader(name, data, vertexSource, fragmentSource);
                } else {
                    if (SHADERS.containsKey(name)) SHADERS.get(name).destroy();
                    SHADERS.put(name, new Shader(name, data, vertexSource, fragmentSource));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to initialize shader '{}': {}", name, e);
            }
        }
    }

    private static String readSource(Map<String, String> cachedIncludes, String name, ResourceManager resourceManager) {
        return readSource(cachedIncludes, "assets/minicraft/shaders/" + name, StringUtils.readString(resourceManager.getResource("assets/minicraft/shaders/" + name).getAsInputStream()), new PreProcessorContext(), resourceManager);
    }

    private static String readSource(Map<String, String> cachedIncludes, String sourcePath, String source, PreProcessorContext context, ResourceManager resourceManager) {
        context.alreadyIncluded.add(sourcePath);
        if (source == null || source.isEmpty()) {
            return "";
        }

        source = stripComments(source);

        Matcher matcher = INCLUDE_PATTERN.matcher(source);

        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (matcher.find()) {
            sb.append(source, i, matcher.start());
            i = matcher.end();

            String path = matcher.group(3);
            if (path == null) path = matcher.group(5);

            if (!context.alreadyIncluded.contains(path)) {
                sb.append("// #include \"").append(path).append("\"\n");
                if (cachedIncludes.containsKey(path)) {
                    sb.append(cachedIncludes.get(path));
                    continue;
                }

                String includSource = readSource(cachedIncludes, path, StringUtils.readString(resourceManager.getResource("assets/minicraft/shaders/include/" + path).getAsInputStream()), context, resourceManager);
                sb.append(includSource);
                cachedIncludes.put(path, includSource);
            }
        }

        if (i == 0) return source;

        if (i < source.length()) {
            sb.append(source, i, source.length());
        }

        return sb.toString();
    }

    private static String stripComments(String source) {
        return SINGLELINE_COMMENT_PATTERN.matcher(MULTILINE_COMMENT_PATTERN.matcher(source).replaceAll("")).replaceAll("");
    }

    private static class PreProcessorContext {
        public Set<String> alreadyIncluded = new HashSet<>();
    }

    public static Shader getBlitShader() {
        return BLIT_SHADER;
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
    
    public static boolean hasLoadedInitial() {
        return loadedInitial;
    }

    @Override
    public void destroy() {
        if (POSITION_COLOR_SHADER != null) POSITION_COLOR_SHADER.destroy();
        if (POSITION_COLOR_TEXTURE_SHADER != null) POSITION_COLOR_TEXTURE_SHADER.destroy();
    
        for (Shader shader : SHADERS.values()) {
            shader.destroy();
        }
    }
}
