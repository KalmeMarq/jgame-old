package me.kalmemarq.jgame.client.render.shader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.kalmemarq.jgame.client.render.Renderer;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.JacksonHelper;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

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
    
    private ShaderBlend shaderBlend;
    private Renderer.FrontFace cullMode;
    
    public Shader(String name, JsonNode data, String vertexSource, String fragmentSource) {
        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertex, data.get("vertex").textValue());
        GL20.glCompileShader(vertex);

        int succ = GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile vertex " + name + ": " + GL20.glGetShaderInfoLog(vertex));
        }

        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, data.get("fragment").textValue());
        GL20.glCompileShader(fragment);

        succ = GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile fragment " + name + ": " + GL20.glGetShaderInfoLog(fragment));
        }
        
        boolean hasGeometry = data.has("geometry");
        if (!Renderer.supportsGeometryShader) {
            LOGGER.error("Failed to load geometry shader {} because it is not supported!", name);
            hasGeometry = false;
        }

        int geometry = -1;
        if (hasGeometry) {
            geometry = GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER);
            GL20.glShaderSource(geometry, data.get("geometry").textValue());
            GL20.glCompileShader(geometry);

            succ = GL20.glGetShaderi(geometry, GL20.GL_COMPILE_STATUS);
            if (succ == 0) {
                throw new RuntimeException("Failed to compile geometry " + name + ": " + GL20.glGetShaderInfoLog(geometry));
            }
        }

        this.id = GL20.glCreateProgram();
        GL20.glAttachShader(this.id, vertex);
        GL20.glAttachShader(this.id, fragment);
        if (hasGeometry) GL20.glAttachShader(this.id, geometry);
        GL20.glLinkProgram(this.id);

        succ = GL20.glGetProgrami(this.id, GL20.GL_LINK_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to link program " + name + ": " + GL20.glGetProgramInfoLog(this.id));
        }

        GL20.glValidateProgram(this.id);

        GL20.glDetachShader(this.id, vertex);
        GL20.glDetachShader(this.id, fragment);
        if (hasGeometry) GL20.glDetachShader(this.id, geometry);

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        if (hasGeometry) GL20.glDeleteShader(geometry);

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
                                        uniform.set(JacksonHelper.arrayNodeToIntArray(jDataArr));
                                    }
                                }
                                default -> {
                                    if (jData instanceof ArrayNode jDataArr) {
                                        uniform.set(JacksonHelper.arrayNodeToFloatArray(jDataArr));
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
        
        if (data.has("cull") && data.get("cull") instanceof ObjectNode cullObj) {
            if (cullObj.has("mode")) {
                this.cullMode = switch (cullObj.get("mode").textValue()) {
                    case "clockwise", "cw" -> Renderer.FrontFace.CW;
                    case "counterclockwise", "ccw" -> Renderer.FrontFace.CCW;
                    default -> null;
                };
            }
        }
        
        if (data.has("blend") && data.get("blend") instanceof ObjectNode blendObj) {
            String equationRGB = blendObj.hasNonNull("equation_rgb") ? blendObj.get("equation_rgb").textValue() : "add";
            String equationAlpha = blendObj.hasNonNull("equation_alpha") ? blendObj.get("equation_alpha").textValue() : "add";
            String srcColor = blendObj.hasNonNull("src_color") ? blendObj.get("src_color").textValue() : "src_alpha";
            String srcAlpha = blendObj.hasNonNull("src_alpha") ? blendObj.get("src_alpha").textValue() : "1-src_alpha";
            String dstColor = blendObj.hasNonNull("dst_color") ? blendObj.get("dst_color").textValue() : "1";
            String dstAlpha = blendObj.hasNonNull("dst_alpha") ? blendObj.get("dst_alpha").textValue() : "0";
            this.shaderBlend = new ShaderBlend(
                ShaderBlend.parseEquation(equationRGB, Renderer.BlendEquation.FUNC_ADD),
                ShaderBlend.parseEquation(equationAlpha, Renderer.BlendEquation.FUNC_ADD),
                ShaderBlend.parseFactor(srcColor, Renderer.BlendFactor.SRC_ALPHA),
                ShaderBlend.parseFactor(srcAlpha, Renderer.BlendFactor.ONE_MINUS_SRC_ALPHA),
                ShaderBlend.parseFactor(dstColor, Renderer.BlendFactor.ONE),
                ShaderBlend.parseFactor(dstAlpha, Renderer.BlendFactor.ZERO)
            );
        }
    }
    
    public void addSampler(String name, int sampler) {
        this.samplersToUse.put(name, sampler);
    }
    
    public void bind() {
        GL20.glUseProgram(this.id);
        
        if (this.shaderBlend != null) {
            Renderer.blendEquationSeparate(this.shaderBlend.equationRGB, this.shaderBlend.equationAlpha);
            Renderer.blendFunctionSeparate(this.shaderBlend.srcRGBFactor, this.shaderBlend.srcAlphaFactor, this.shaderBlend.dstRGBFactor, this.shaderBlend.dstAlphaFactor);
        }
        
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
    
    public static class ShaderBlend {
        Renderer.BlendEquation equationRGB;
        Renderer.BlendEquation equationAlpha;
        Renderer.BlendFactor srcRGBFactor;
        Renderer.BlendFactor srcAlphaFactor;
        Renderer.BlendFactor dstRGBFactor;
        Renderer.BlendFactor dstAlphaFactor;
        
        public ShaderBlend(Renderer.BlendEquation equationRGB, Renderer.BlendEquation equationAlpha, Renderer.BlendFactor srcRGBFactor, Renderer.BlendFactor srcAlphaFactor, Renderer.BlendFactor dstRGBFactor, Renderer.BlendFactor dstAlphaFactor) {
            this.equationRGB = equationRGB;
            this.equationAlpha = equationAlpha;
            this.srcRGBFactor = srcRGBFactor;
            this.srcAlphaFactor = srcAlphaFactor;
            this.dstRGBFactor = dstRGBFactor;
            this.dstAlphaFactor = dstAlphaFactor;
        }

        static Renderer.BlendEquation parseEquation(String value, Renderer.BlendEquation defaultValue) {
            return switch (value) {
                case "add" -> Renderer.BlendEquation.FUNC_ADD;
                case "subtract", "sub" -> Renderer.BlendEquation.FUNC_SUBTRACT;
                case "reverse_subtract", "reverse_sub" -> Renderer.BlendEquation.FUNC_REVERSE_SUBTRACT;
                case "min" -> Renderer.BlendEquation.MIN;
                case "max" -> Renderer.BlendEquation.MAX;
                default -> defaultValue;
            };
        }
        
        static Renderer.BlendFactor parseFactor(String value, Renderer.BlendFactor defaultValue) {
            return switch (value) {
                case "zero", "0" -> Renderer.BlendFactor.ZERO;
                case "one", "1" -> Renderer.BlendFactor.ONE;
                case "src_color" -> Renderer.BlendFactor.SRC_COLOR;
                case "src_alpha" -> Renderer.BlendFactor.SRC_ALPHA;
                case "one_minus_src_color", "1-src_color" -> Renderer.BlendFactor.ONE_MINUS_SRC_COLOR;
                case "one_minus_src_alpha", "1-src_alpha" -> Renderer.BlendFactor.ONE_MINUS_SRC_ALPHA;
                case "dst_color" -> Renderer.BlendFactor.DST_COLOR;
                case "dst_alpha" -> Renderer.BlendFactor.DST_ALPHA;
                case "one_minus_dst_color", "1-dst_color" -> Renderer.BlendFactor.ONE_MINUS_DST_COLOR;
                case "one_minus_dst_alpha", "1-dst_alpha" -> Renderer.BlendFactor.ONE_MINUS_DST_ALPHA;
                case "const_color" -> Renderer.BlendFactor.CONSTANT_COLOR;
                case "const_alpha" -> Renderer.BlendFactor.CONSTANT_ALPHA;
                case "one_minus_const_color", "1-const_color" -> Renderer.BlendFactor.ONE_MINUS_CONSTANT_COLOR;
                case "one_minus_const_alpha", "1-const_alpha" -> Renderer.BlendFactor.ONE_MINUS_CONSTANT_ALPHA;
                default -> defaultValue;
            };
        }
    }
}
