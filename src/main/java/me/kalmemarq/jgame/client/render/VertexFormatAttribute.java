package me.kalmemarq.jgame.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class VertexFormatAttribute {
    public static VertexFormatAttribute POSITION = new VertexFormatAttribute(ComponentType.FLOAT, 3, (index, components, type, stride, offset) -> {
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, components, type, false, stride, offset);
    }, GL20::glDisableVertexAttribArray);
    public static VertexFormatAttribute TEXTURE = new VertexFormatAttribute(ComponentType.FLOAT, 2, (index, components, type, stride, offset) -> {
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, components, type, false, stride, offset);
    }, GL20::glDisableVertexAttribArray);
    public static VertexFormatAttribute COLOR = new VertexFormatAttribute(ComponentType.UBYTE, 4, (index, components, type, stride, offset) -> {
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, components, type, true, stride, offset);
    }, GL20::glDisableVertexAttribArray);
    
    public final int byteSize;
    public final int components;
    public final ComponentType type;
    private final SetupTask setupTask;
    private final ClearTask clearTask;

    public VertexFormatAttribute(ComponentType type, int components, SetupTask setupTask, ClearTask clearTask) {
        this.type = type;
        this.components = components;
        this.byteSize = type.byteSize * components;
        this.setupTask = setupTask;
        this.clearTask = clearTask;
    }

    public void setup(int index, int stride, int offset) {
        this.setupTask.setup(index, this.components, this.type.glType, stride, offset);
    }

    public void clear(int index) {
        this.clearTask.clear(index);
    }

    public interface SetupTask {
        void setup(int index, int components, int type, int stride, int offset);
    }

    public interface ClearTask {
        void clear(int index);
    }
    
    public enum ComponentType {
        BYTE(GL11.GL_BYTE, Byte.BYTES),
        UBYTE(GL11.GL_UNSIGNED_BYTE, Byte.BYTES),
        SHORT(GL11.GL_SHORT, Short.BYTES),
        USHORT(GL11.GL_UNSIGNED_SHORT, Short.BYTES),
        INT(GL11.GL_INT, Integer.BYTES),
        UINT(GL11.GL_UNSIGNED_INT, Integer.BYTES),
        FLOAT(GL11.GL_FLOAT, Float.BYTES);

        public final int glType;
        public final int byteSize;

        ComponentType(int glType, int byteSize) {
            this.glType = glType;
            this.byteSize = byteSize;
        }
    }
}
