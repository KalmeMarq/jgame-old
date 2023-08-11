package me.kalmemarq.jgame.client.render;

import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

public class MatrixStack {
    private final Deque<Matrix4f> stack = new ArrayDeque<>();
    private final Deque<Matrix4f> cache = new ArrayDeque<>();
    
    public MatrixStack() {
        this.stack.add(new Matrix4f());
    }
    
    public void push() {
        Matrix4f previous = this.stack.getLast();
        Matrix4f matrix;
        
        if (!this.cache.isEmpty()) {
            matrix = this.cache.removeLast().set(previous);
        } else {
            matrix = new Matrix4f(previous);
        }
        this.stack.add(matrix);
    }
    
    public void pop() {
        this.cache.add(this.stack.removeLast());
    }

    public void scale(float x, float y, float z) {
        this.stack.getLast().scale(x, y, z);
    }

    public void translate(float x, float y, float z) {
        this.stack.getLast().translate(x, y, z);
    }

    public Matrix4f peek() {
        return this.stack.getLast();
    }
    
    public boolean isEmpty() {
        return this.stack.size() == 1;
    }
}
