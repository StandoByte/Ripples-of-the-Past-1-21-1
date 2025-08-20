#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SilhouetteSampler;
uniform sampler2D PixelatedMaskSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float ScreenRatio;

out vec4 fragColor;

void main(){
	if (texture(SilhouetteSampler, texCoord).a == 0.0) {
		vec4 outlineColor = texture(DiffuseSampler, texCoord);
		if (outlineColor.a > 0.0) {
			vec4 maskColor = texture(PixelatedMaskSampler, texCoord * vec2(ScreenRatio));
			fragColor = vec4(outlineColor.rgb, maskColor.a);
		}
	}
	
//	vec4 maskColor = texture(PixelatedMaskSampler, texCoord);
//	if (maskColor.a > 0.0) {
//		fragColor = maskColor;
//	}
}

