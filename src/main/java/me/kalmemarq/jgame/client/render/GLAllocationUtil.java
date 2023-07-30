package me.kalmemarq.jgame.client.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GLAllocationUtil {
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    public static ByteBuffer allocateByteBuffer(int size) {
        long l = ALLOCATOR.malloc(size);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
        return MemoryUtil.memByteBuffer(l, size);
    }

    public static ByteBuffer reallocateByteBuffer(ByteBuffer source, int size) {
        long l = ALLOCATOR.realloc(MemoryUtil.memAddress0(source), size);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to realloc from " + source.capacity() + " bytes to " + size + " bytes");
        }
        return MemoryUtil.memByteBuffer(l, size);
    }
}
