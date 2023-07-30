#version 330

uniform sampler2D uSampler0;

in vec2 vTexCoord;

out vec4 color;

void main() {
  color = texture(uSampler0, vTexCoord);
}