#version 330

uniform vec4 uColor;

in vec4 vColor;

out vec4 color;

void main() {
    color = uColor * vColor;
}