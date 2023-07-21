package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.Util;
import org.lwjgl.opengl.GL20;

public class Shader implements Destroyable {
    private int id;

    public Shader(String name, String vertexSource, String fragmentSource) {
        int vertex = GL20.glCreateShader(GL20.GL_VERTEX_ARRAY);
        GL20.glShaderSource(vertex, vertexSource);
        GL20.glCompileShader(vertex);

        int succ = GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile vertex " + name + ": " + GL20.glGetShaderInfoLog(vertex));
        }

        int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, fragmentSource);
        GL20.glCompileShader(fragment);

        succ = GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to compile fragment " + name + ": " + GL20.glGetShaderInfoLog(fragment));
        }

        this.id = GL20.glCreateProgram();
        GL20.glAttachShader(this.id, vertex);
        GL20.glAttachShader(this.id, fragment);
        GL20.glLinkProgram(this.id);

        succ = GL20.glGetProgrami(this.id, GL20.GL_LINK_STATUS);
        if (succ == 0) {
            throw new RuntimeException("Failed to link program " + name + ": " + GL20.glGetProgramInfoLog(this.id));
        }

        GL20.glValidateProgram(this.id);

        GL20.glDetachShader(this.id, vertex);
        GL20.glDetachShader(this.id, fragment);

        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
    }

    @Override
    public void destroy() {
        if (this.id != -1) {
            GL20.glDeleteProgram(this.id);
            this.id = -1;
        }
    }
}
