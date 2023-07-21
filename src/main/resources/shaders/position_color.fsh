#version 330

uniform mat4 uColor;

in vec4 vColor;

out vec4 color;

void main() {
    color = uColor * vColor;
}