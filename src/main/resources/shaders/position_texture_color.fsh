#version 330

uniform sampler2D uSampler0;
uniform mat4 uColor;

in vec2 vUV0;
in vec4 vColor;

out vec4 color;

void main() {
    color = texture(uSampler0, vUV0) * uColor * vColor;
}