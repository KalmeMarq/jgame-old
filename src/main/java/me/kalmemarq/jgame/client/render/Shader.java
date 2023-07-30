package me.kalmemarq.jgame.client.render;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.Util;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shader implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();
    private int id;
    private final Map<String, Uniform> uniforms;
    private final Map<String, Integer> samplers;
    private final List<String> samplerNames;
    
    public final Uniform projectionMatrixUniform;
    public final Uniform modelViewMatrixUniform;
    public final Uniform projectionModelViewMatrixUniform;
    public final Uniform textureMatrixUniform;
    public final Uniform colorUniform;
    
    private final Map<String, Integer> samplersToUse = new HashMap<>();
    
    public Shader(String name, JsonNode data, String vertexSource, String fragmentSource) {
        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertex, vertexSource);
        GL20.glCompileShader(vertex);

        int succ = GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile vertex " + name + ": " + GL20.glGetShaderInfoLog(vertex));
        }

        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, fragmentSource);
        GL20.glCompileShader(fragment);

        succ = GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile fragment " + name + ": " + GL20.glGetShaderInfoLog(fragment));
        }

        this.id = GL20.glCreateProgram();
        GL20.glAttachShader(this.id, vertex);
        GL20.glAttachShader(this.id, fragment);
        GL20.glLinkProgram(this.id);

        succ = GL20.glGetProgrami(this.id, GL20.GL_LINK_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to link program " + name + ": " + GL20.glGetProgramInfoLog(this.id));
        }

        GL20.glValidateProgram(this.id);

        GL20.glDetachShader(this.id, vertex);
        GL20.glDetachShader(this.id, fragment);

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);

        this.uniforms = new HashMap<>();
        
        if (data.hasNonNull("uniforms")) {
            if (data.get("uniforms") instanceof ArrayNode jUnifArr) {
                for (int i = 0; i < jUnifArr.size(); ++i) {
                    if (jUnifArr.get(i) instanceof ObjectNode jUnifObj) {
                        String unifName = jUnifObj.get("name").textValue();
                        Uniform.Type unifType = switch (jUnifObj.get("type").textValue()) {
                            case "int", "int1" -> Uniform.Type.INT1;
                            case "int2" -> Uniform.Type.INT2;
                            case "int3" -> Uniform.Type.INT3;
                            case "int4" -> Uniform.Type.INT4;
                            case "float", "float1" -> Uniform.Type.FLOAT1;
                            case "float2" -> Uniform.Type.FLOAT2;
                            case "float3" -> Uniform.Type.FLOAT3;
                            case "float4" -> Uniform.Type.FLOAT4;
                            case "matrix2" -> Uniform.Type.MATRIX2x2;
                            case "matrix3" -> Uniform.Type.MATRIX3x3;
                            case "matrix4" -> Uniform.Type.MATRIX4x4;
                            default -> throw new RuntimeException("Unknown uniform data type '" + jUnifObj.get("type").textValue() + "'");
                        };
                        boolean hasData = jUnifObj.get("hasData").asBoolean(false);
                        
                        Uniform uniform = new Uniform(unifName, unifType, GL20.glGetUniformLocation(this.id, unifName));
                        
                        LOGGER.debug("Shader '{}' new uniform '{}' at location {}", name, unifName, uniform.getLocation());
                        
                        if (hasData) {
                            JsonNode jData = jUnifObj.get("data");
                            
                            switch (unifType) {
                                case INT1 -> {
                                    if (jData.isNumber()) {
                                        uniform.set(jData.intValue());
                                    }
                                }
                                case FLOAT1 -> {
                                    if (jData.isNumber()) {
                                        uniform.set(jData.floatValue());
                                    }
                                }
                                case INT2, INT3, INT4 -> {
                                    if (jData instanceof ArrayNode jDataArr) {
                                        uniform.set(Util.arrayNodeToIntArray(jDataArr));
                                    }
                                }
                                default -> {
                                    if (jData instanceof ArrayNode jDataArr) {
                                        uniform.set(Util.arrayNodeToFloatArray(jDataArr));
                                    }
                                }
                            }
                        }
                        
                        this.uniforms.put(unifName, uniform);
                    }
                }
            }
        }
        
        this.projectionMatrixUniform = this.uniforms.get("uProjectionMatrix");
        this.modelViewMatrixUniform = this.uniforms.get("uModelViewMatrix");
        this.projectionModelViewMatrixUniform = this.uniforms.get("uProjectionModelViewMatrix");
        this.textureMatrixUniform = this.uniforms.get("uTextureMatrix");
        this.colorUniform = this.uniforms.get("uColor");
        
        this.samplers = new HashMap<>();
        this.samplerNames = new ArrayList<>();
        if (data.hasNonNull("samplers") && data.get("samplers") instanceof ArrayNode jSamplersArr) {
            for (int i = 0; i < jSamplersArr.size(); ++i) {
                if (jSamplersArr.get(i) instanceof ObjectNode jSamplerObj) {
                    this.samplers.put(jSamplerObj.get("name").textValue(), GL20.glGetUniformLocation(this.id, jSamplerObj.get("name").textValue()));
                    this.samplerNames.add(jSamplerObj.get("name").textValue());
                }
            }
        }
    }
    
    public void addSampler(String name, int sampler) {
        this.samplersToUse.put(name, sampler);
    }
    
    public void bind() {
        GL20.glUseProgram(this.id);
        
        for (int i = 0; i < this.samplers.size(); ++i) {
            String name = this.samplerNames.get(i);
            if (this.samplersToUse.get(name) == null) continue;
            GL20.glUniform1i(this.samplers.get(name), i);
            GL20.glActiveTexture(GL20.GL_TEXTURE0 + i);
            GL20.glBindTexture(GL20.GL_TEXTURE_2D, this.samplersToUse.get(name));
        }
        
        GL20.glActiveTexture(GL20.GL_TEXTURE0);
        
        for (Uniform uniform : this.uniforms.values()) {
            uniform.upload();
        }
    }

    public void unbind() {
        GL20.glUseProgram(0);

        for (int i = 0; i < this.samplers.size(); ++i) {
            String name = this.samplerNames.get(i);
            if (this.samplersToUse.get(name) == null) continue;
            GL20.glActiveTexture(GL20.GL_TEXTURE0 + i);
            GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        }

        GL20.glActiveTexture(GL20.GL_TEXTURE0);
    }

    @Override
    public void destroy() {
        for (Uniform uniform : this.uniforms.values()) {
            uniform.destroy();
        }
        
        if (this.id != -1) {
            GL20.glDeleteProgram(this.id);
            this.id = -1;
        }
    }
}
