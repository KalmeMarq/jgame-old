package me.kalmemarq.jgame.client.render.texture;

import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MinicraftImage implements Destroyable {
    private final long pointer;
    private final int width;
    private final int height;
    private final boolean isSTB;
    
    private MinicraftImage(int width, int height) {
        this.pointer = MemoryUtil.nmemAlloc(width * height * 4L);
        this.width = width;
        this.height = height;
        this.isSTB = false;
    }

    private MinicraftImage(int width, int height, long pointer, boolean isSTB) {
        this.pointer = pointer;
        this.width = width;
        this.height = height;
        this.isSTB = isSTB;
    }
    
    public static MinicraftImage create(int width, int height) {
        if (width <= 0) {
            throw new RuntimeException("Width cannot be 0 or less!");
        }
        if (height <= 0) {
            throw new RuntimeException("Height cannot be 0 or less!");
        }
        return new MinicraftImage(width, height);
    }
    
    public static MinicraftImage create(int width, int height, ColorFunction filler) {
        if (width <= 0) {
            throw new RuntimeException("Width cannot be 0 or less!");
        }
        if (height <= 0) {
            throw new RuntimeException("Height cannot be 0 or less!");
        }
        MinicraftImage image = new MinicraftImage(width, height);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                image.setPixel(x, y, filler.get(x, y));
            }
        }
        return image;
    }

    public static MinicraftImage read(InputStream inputStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ByteBuffer buffer = MemoryUtil.memAlloc(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);
        
        try {
            int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
            bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());

            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int pixel = pixels[y * bufferedImage.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            
            buffer.flip();
            return read(buffer);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    public static MinicraftImage read(ByteBuffer source) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);
            
            ByteBuffer buffer = STBImage.stbi_load_from_memory(source, pWidth, pHeight, pChannels, 4);
            if (buffer == null) {
                throw new IOException("Failed to load image");
            }
            return new MinicraftImage(pWidth.get(0), pHeight.get(0), MemoryUtil.memAddress(buffer), true);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setPixel(int x, int y, int color) {
        MemoryUtil.memPutInt(this.pointer + (x + y * this.width * 4L), color);
    }

    public int getPixel(int x, int y) {
        return MemoryUtil.memGetInt(this.pointer + (x + y * this.width * 4L));
    }
    
    public int[] getPixels() {
        int[] pixels = new int[this.width * this.height];
        MemoryUtil.memIntBuffer(this.pointer, this.width * this.height).get(pixels);
        return pixels;
    }

    @Override
    public void destroy() {
        if (this.isSTB) STBImage.nstbi_image_free(this.pointer);
        else MemoryUtil.nmemFree(this.pointer);
    }

    @FunctionalInterface
    public interface ColorFunction {
        int get(int x, int y);
    }
}
