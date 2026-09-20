#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;
uniform vec2 InSize;

void main() {
    vec4 rgb = texture(DiffuseSampler, texCoord);
    float rTmp = rgb.r;
    rgb.r = rgb.g;
    rgb.g = rTmp;
    fragColor = rgb;
}
