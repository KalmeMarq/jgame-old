package me.kalmemarq.jgame.client;

import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.HashSet;
import java.util.Set;

public class MemoryUtils {
    private static MemoryUtil.MemoryAllocator ALLOCATOR;
    private static final Set<ByteBuffer> buffers = new HashSet<>(256);

    public static void setupAllocator(boolean trackAllocations) {
        if (ALLOCATOR == null) {
            ALLOCATOR = MemoryUtil.getAllocator(false);
        }
    }

    public static ByteBuffer allocateByteBuffer(int size) {
        long l = ALLOCATOR.malloc(size);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
        ByteBuffer buffer = MemoryUtil.memByteBuffer(l, size);
        buffers.add(buffer);
        return buffer;
    }
    
    public static ByteBuffer reallocateByteBuffer(ByteBuffer source, int size) {
        long l = ALLOCATOR.realloc(MemoryUtil.memAddress0(source), size);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to realloc from " + source.capacity() + " bytes to " + size + " bytes");
        }
        buffers.removeIf(b -> b == source);
        ByteBuffer buffer = MemoryUtil.memByteBuffer(l, size);
        buffers.add(buffer);
        return buffer;
    }
    
    public static void freeByteBuffer(ByteBuffer source) {
        ALLOCATOR.free(MemoryUtil.memAddress0(source));
    }
    
    public static void cleanup() {
    }

    public static ByteBuffer readAsByteBuffer(InputStream inputStream) throws IOException {
        ReadableByteChannel channel = Channels.newChannel(inputStream);
        ByteBuffer buffer = MemoryUtil.memAlloc(channel instanceof SeekableByteChannel ? ((int) ((SeekableByteChannel) channel).size() + 1)  : 8192);

        try {
            while (channel.read(buffer) != -1) {
                if (!buffer.hasRemaining()) {
                    buffer = MemoryUtil.memRealloc(buffer, (int) (buffer.capacity() * 1.5));
                }
            }
            return buffer;
        } catch (IOException e) {
            MemoryUtil.memFree(buffer);
            throw e;
        }
    }
}
