package me.kalmemarq.jgame.client.render;

import org.joml.Matrix4f;

/**
 * An interfaces that consumes vertices in a certain vertex format.
 */
public interface VertexConsumer {
    /**
     * Specifies the position attribute of the current vertex
     * @return this consumer, for chaining
     */
    VertexConsumer vertex(Matrix4f matrix, float x, float y, float z);

    /**
     * Specifies the position attribute of the current vertex
     * @param matrix the matrix that will be applied to the vertex position
     * @return this consumer, for chaining
     */
    VertexConsumer vertex(Matrix4f matrix, double x, double y, double z);

    /**
     * Specifies the position attribute of the current vertex
     * @return this consumer, for chaining
     */
    VertexConsumer vertex(double x, double y, double z);

    /**
     * Specifies the position attribute of the current vertex
     * @return this consumer, for chaining
     */
    VertexConsumer vertex(float x, float y, float z);

    /**
     * Specifies the texture attribute of the current vertex
     * @return this consumer, for chaining
     */
    VertexConsumer texture(float u, float v);

    /**
     * Specifies the colour attribute of the current vertex
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(float r, float g, float b) {
        return this.colour((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @param a alpha
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(float r, float g, float b, float a) {
        return this.colour((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @param colour argb value
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(int colour) {
        return this.colour((colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF, (colour >> 24) & 0xFF);
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @param rgb colour
     * @param alpha alpha
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(int rgb, int alpha) {
        return this.colour((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @param rgb colour
     * @param alpha alpha
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(int rgb, float alpha) {
        return this.colour((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, (int) (alpha * 255));
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(int r, int g, int b) {
        return this.colour(r, g, b, 255);
    }
    
    /**
     * Specifies the colour attribute of the current vertex
     * @param a alpha
     * @return this consumer, for chaining
     */
    default VertexConsumer colour(int r, int g, int b, float a) {
        return this.colour(r, g, b, (int) (a * 255));
    }

    /**
     * Specifies the colour attribute of the current vertex
     * @param a alpha
     * @return this consumer, for chaining
     */
    VertexConsumer colour(int r, int g, int b, int a);

    /**
     * Advances to the next vertex.
     */
    void next();
}
