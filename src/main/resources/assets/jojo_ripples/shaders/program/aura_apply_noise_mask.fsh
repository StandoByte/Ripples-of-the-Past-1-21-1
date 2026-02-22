#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SilhouetteSampler;
uniform sampler2D NoiseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float NoiseVShift;

out vec4 fragColor;

// Noise texture: https://noisegen.bubblebirdstudio.com/ (res - 64, perlin, size - 0.06, 1 octave, seamless)
void main(){
	if (texture(SilhouetteSampler, texCoord).a == 0.0) {
		vec4 outlineColor = texture(DiffuseSampler, texCoord);
		if (outlineColor.a > 0) {
			vec2 noiseCoord = vec2(
				texCoord.x * oneTexel.y / oneTexel.x, 
				1.0 - texCoord.y + NoiseVShift
			);
			vec4 noiseColor = texture(NoiseSampler, noiseCoord);
			if (noiseColor.r < outlineColor.a) {
				fragColor = vec4(outlineColor.rgb, 0.75 * outlineColor.a);
//				fragColor = vec4(outlineColor.rgb, 0.5);
			}
		}
	}
	
}

