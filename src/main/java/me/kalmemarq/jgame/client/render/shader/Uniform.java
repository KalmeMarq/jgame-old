package me.kalmemarq.jgame.client.render.shader;

import me.kalmemarq.jgame.common.Destroyable;
import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Uniform implements Destroyable {
    private final String name;
    private final Type type;
    private final int location;
    private IntBuffer intBuffer;
    private FloatBuffer floatBuffer;
    
    public Uniform(String name, Type type, int location) {
        this.name = name;
        this.type = type;
        this.location = location;
        if (type.isInt()) {
            this.intBuffer = MemoryUtil.memAllocInt(type.size);
        } else {
            this.floatBuffer = MemoryUtil.memAllocFloat(type.size);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getLocation() {
        return this.location;
    }

    public void set(float[] value) {
        this.floatBuffer.position(0);
        for (int i = 0; i < value.length && i < this.type.size; ++i) {
            this.floatBuffer.put(i, value[i]);
        }
    }

    public void set(float value) {
        this.floatBuffer.position(0);
        this.floatBuffer.put(0, value);
    }

    public void set(float value0, float value1) {
        this.floatBuffer.position(0);
        this.floatBuffer.put(0, value0);
        this.floatBuffer.put(1, value1);
    }

    public void set(float value0, float value1, float value2) {
        this.floatBuffer.position(0);
        this.floatBuffer.put(0, value0);
        this.floatBuffer.put(1, value1);
        this.floatBuffer.put(2, value2);
    }

    public void set(float value0, float value1, float value2, float value3) {
        this.floatBuffer.position(0);
        this.floatBuffer.put(0, value0);
        this.floatBuffer.put(1, value1);
        this.floatBuffer.put(2, value2);
        this.floatBuffer.put(3, value3);
    }

    public void set(int[] value) {
        this.intBuffer.position(0);
        for (int i = 0; i < value.length && i < this.type.size; ++i) {
            this.intBuffer.put(i, value[i]);
        }
    }

    public void set(int value) {
        this.intBuffer.position(0);
        this.intBuffer.put(0, value);
    }

    public void set(int value0, int value1) {
        this.intBuffer.position(0);
        this.intBuffer.put(0, value0);
        this.intBuffer.put(1, value1);
    }

    public void set(int value0, int value1, int value2) {
        this.intBuffer.position(0);
        this.intBuffer.put(0, value0);
        this.intBuffer.put(1, value1);
        this.intBuffer.put(2, value2);
    }

    public void set(int value0, int value1, int value2, int value3) {
        this.intBuffer.position(0);
        this.intBuffer.put(0, value0);
        this.intBuffer.put(1, value1);
        this.intBuffer.put(2, value2);
        this.intBuffer.put(3, value3);
    }

    public void set(Matrix2f value) {
        this.floatBuffer.position(0);
        value.get(this.floatBuffer);
    }

    public void set(Matrix3f value) {
        this.floatBuffer.position(0);
        value.get(this.floatBuffer);
    }
    
    public void set(Matrix4f value) {
        this.floatBuffer.position(0);
        value.get(this.floatBuffer);
    }
    
    public void upload() {
        if (this.type.isInt()) {
            this.intBuffer.rewind();
        } else {
            this.floatBuffer.rewind();
        }

        switch (this.type) {
            case INT1 -> GL20.glUniform1iv(this.location, this.intBuffer);
            case INT2 -> GL20.glUniform2iv(this.location, this.intBuffer);
            case INT3 -> GL20.glUniform3iv(this.location, this.intBuffer);
            case INT4 -> GL20.glUniform4iv(this.location, this.intBuffer);
            case FLOAT1 -> GL20.glUniform1fv(this.location, this.floatBuffer);
            case FLOAT2 -> GL20.glUniform2fv(this.location, this.floatBuffer);
            case FLOAT3 -> GL20.glUniform3fv(this.location, this.floatBuffer);
            case FLOAT4 -> GL20.glUniform4fv(this.location, this.floatBuffer);
            case MATRIX2x2 -> GL20.glUniformMatrix2fv(this.location, false, this.floatBuffer);
            case MATRIX3x3 -> GL20.glUniformMatrix3fv(this.location, false, this.floatBuffer);
            case MATRIX4x4 -> GL20.glUniformMatrix4fv(this.location, false, this.floatBuffer);
        }
    }

    @Override
    public void destroy() {
        if (this.intBuffer != null) {
            MemoryUtil.memFree(this.intBuffer);
        }

        if (this.floatBuffer != null) {
            MemoryUtil.memFree(this.floatBuffer);
        }
    }

    public enum Type {
        INT1(1),
        INT2(2),
        INT3(3),
        INT4(4),
        FLOAT1(1),
        FLOAT2(2),
        FLOAT3(3),
        FLOAT4(4),
        MATRIX2x2(4),
        MATRIX3x3(9),
        MATRIX4x4(16);
        
        public final int size;
        
        Type(final int size) {
            this.size = size;
        }
        
        public boolean isInt() {
            return this == INT1 || this == INT2 || this == INT3 || this == INT4; 
        }
    }
}
