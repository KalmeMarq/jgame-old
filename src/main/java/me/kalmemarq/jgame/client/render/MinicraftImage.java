package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.File;

public class MinicraftImage implements Destroyable {
    private final long pointer;
    private final int width;
    private final int height;
    
    public MinicraftImage(int width, int height) {
        this.pointer = MemoryUtil.nmemAlloc(width * height * 4L);
        this.width = width;
        this.height = height;
    }
    
    public void setPixel(int x, int y, int color) {
        MemoryUtil.memPutInt(this.pointer + (x + y * this.width * 4L), color);
    }

    public int getPixel(int x, int y) {
        return MemoryUtil.memGetInt(this.pointer + (x + y * this.width * 4L));
    }
    
    public void writeAsPNG(File file) {
    }
    
    @Override
    public void destroy() {
        MemoryUtil.nmemFree(this.pointer);
    }
}
