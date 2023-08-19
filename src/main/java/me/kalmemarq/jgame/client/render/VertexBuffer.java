package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.MemoryUtils;
import me.kalmemarq.jgame.client.render.shader.Shader;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.logger.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class VertexBuffer implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();
    private int vao;
    private int vbo;
    private int ibo;

    private int lastVertexSize;
    private int lastIndexSize;

    private DrawMode mode;
    private VertexFormat format;

    @Nullable
    private ByteBuffer indexBuffer = null;
    private IndexType indexType = IndexType.UBYTE;
    private int indexCount;

    public VertexBuffer() {
        this.vao = GL30.glGenVertexArrays();
        this.vbo = GL15.glGenBuffers();
        this.ibo = GL15.glGenBuffers();
    }

    public void upload(BufferBuilder.BuiltBuffer builtBuffer) {
        this.format = builtBuffer.format();
        this.mode = builtBuffer.mode();
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);

        if (builtBuffer.remaining() > this.lastVertexSize) {
            LOGGER.debug("Had to resize VBO. New: {} Old: {}", builtBuffer.remaining(), this.lastVertexSize);
            this.lastVertexSize = builtBuffer.remaining();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, builtBuffer.buffer(), GL15.GL_DYNAMIC_DRAW);
        } else {
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, builtBuffer.buffer());
        }

        this.indexCount = builtBuffer.indexCount();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo);

        if (this.indexCount > this.lastIndexSize || this.indexBuffer == null) {
            LOGGER.debug("Had to resize IBO. New: {} Old: {}", this.indexCount, this.lastIndexSize);
            this.lastIndexSize = this.indexCount;

            this.indexType = IndexType.getIdeal(this.indexCount);

            int indexTypeSize = this.indexType.byteSize;

            if (this.indexBuffer == null) {
                this.indexBuffer = MemoryUtils.allocateByteBuffer(this.indexCount * indexTypeSize);
            } else {
                this.indexBuffer = MemoryUtils.reallocateByteBuffer(this.indexBuffer, this.indexCount * indexTypeSize);
            }
            
            for (int j = 0, k = 0; j < this.indexCount / 6; ++j, k += 6 * indexTypeSize) {
                if (this.indexType == IndexType.USHORT) {
                    this.indexBuffer.putShort(k, (short) (j * 4));
                    this.indexBuffer.putShort(k + 2, (short) (j * 4 + 1));
                    this.indexBuffer.putShort(k + 4, (short) (j * 4 + 2));
                    this.indexBuffer.putShort(k + 6, (short) (j * 4 + 2));
                    this.indexBuffer.putShort(k + 8, (short) (j * 4 + 3));
                    this.indexBuffer.putShort(k + 10, (short) (j * 4));
                } else if (this.indexType == IndexType.UINT) {
                    this.indexBuffer.putInt(k, j * 4);
                    this.indexBuffer.putInt(k + 4, j * 4 + 1);
                    this.indexBuffer.putInt(k + 8, j * 4 + 2);
                    this.indexBuffer.putInt(k + 12, j * 4 + 2);
                    this.indexBuffer.putInt(k + 16, j * 4 + 3);
                    this.indexBuffer.putInt(k + 20, j * 4);
                } else {
                    this.indexBuffer.put(k, (byte) (j * 4));
                    this.indexBuffer.put((k + 1), (byte) (j * 4 + 1));
                    this.indexBuffer.put((k + 2), (byte) (j * 4 + 2));
                    this.indexBuffer.put((k + 3), (byte) (j * 4 + 2));
                    this.indexBuffer.put((k + 4), (byte) (j * 4 + 3));
                    this.indexBuffer.put((k + 5), (byte) (j * 4));
                }
            }

            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer, GL15.GL_DYNAMIC_DRAW);
        } else {
            GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, this.indexBuffer);
        }

        builtBuffer.release();
    }
    
    public void bind() {
        GL30.glBindVertexArray(this.vao);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void draw() {
        Shader shader = Renderer.getCurrentShader();

        if (shader != null) {
            for (int i = 0; i < 12; ++i) {
                int j = Renderer.getShaderTexture(i);
                shader.addSampler("uSampler" + i, j);
            }
            
            if (shader.projectionMatrixUniform != null) shader.projectionMatrixUniform.set(Renderer.getProjectionMatrix());
            if (shader.modelViewMatrixUniform != null) shader.modelViewMatrixUniform.set(Renderer.getModeViewMatrix());
            if (shader.colorUniform != null) shader.colorUniform.set(Renderer.getShaderColor());

            shader.bind();

            this.format.setup();
            GL20.glDrawElements(this.mode.glType, this.indexCount, this.indexType.glType, 0);
            this.format.clear();
        }
        
        if (shader != null) {
            shader.unbind();
        }
    }

    @Override
    public void destroy() {
        if (this.indexBuffer != null) {
            MemoryUtils.freeByteBuffer(this.indexBuffer);
        }
        
        if (this.vao == -1) {
            GL30.glDeleteVertexArrays(this.vao);
            this.vao = -1;
        }

        if (this.vbo == -1) {
            GL15.glDeleteBuffers(this.vbo);
            this.vbo = -1;
        }

        if (this.ibo == -1) {
            GL15.glDeleteBuffers(this.ibo);
            this.ibo = -1;
        }
    }

    public enum IndexType {
        UBYTE(GL11.GL_UNSIGNED_BYTE, Byte.BYTES),
        USHORT(GL11.GL_UNSIGNED_SHORT, Short.BYTES),
        UINT(GL11.GL_UNSIGNED_INT, Integer.BYTES);

        public final int glType;
        public final int byteSize;

        IndexType(int glType, int byteSize) {
            this.glType = glType;
            this.byteSize = byteSize;
        }

        public static IndexType getIdeal(int indices) {
            if (indices <= 255) {
                return IndexType.UBYTE;
            } else if (indices <= 65335) {
                return IndexType.USHORT;
            }
            return IndexType.UINT;
        }
    }
}
