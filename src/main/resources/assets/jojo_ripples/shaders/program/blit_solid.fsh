#version 150

uniform sampler2D DiffuseSampler;

uniform vec4 ColorModulate;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    fragColor = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0) * ColorModulate;
}
