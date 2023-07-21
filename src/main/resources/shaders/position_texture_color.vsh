#version 330

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

layout(location = 0) vec3 aPosition;
layout(location = 1) vec2 aUV0;
layout(location = 2) vec4 aColor;

out vec2 vUV0;
out vec4 vColor;

void main() {
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
    vUV0 = aUV0;
    vColor = aColor;
}