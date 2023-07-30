#version 330

uniform sampler2D uSampler0;
uniform vec4 uColor;

in vec2 vTexCoord;
in vec4 vColor;

out vec4 color;

void main() {
    color = texture(uSampler0, vTexCoord) * uColor * vColor;
}