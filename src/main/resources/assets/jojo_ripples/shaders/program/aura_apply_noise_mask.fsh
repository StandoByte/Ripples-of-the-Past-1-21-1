#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SilhouetteSampler;
uniform sampler2D NoiseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main(){
	if (texture(SilhouetteSampler, texCoord).a == 0.0) {
		vec4 outlineColor = texture(DiffuseSampler, texCoord);
		if (outlineColor.a > 0) {
			vec4 noiseColor = texture(NoiseSampler, texCoord);
			if (noiseColor.r < outlineColor.a) {
				fragColor = vec4(outlineColor.rgb, 0.75 * outlineColor.a);
//				fragColor = vec4(outlineColor.rgb, 0.5);
			}
		}
	}
	
}

