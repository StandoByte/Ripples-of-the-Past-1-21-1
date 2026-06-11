package com.github.standobyte.jojo.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class SerializeKeybind {
	
	public static final Codec<KeyWithModifier> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<T> encode(KeyWithModifier input, DynamicOps<T> ops, T prefix) {
			String key = input.key.getName() + (input.modifier != KeyModifier.NONE ? ":" + input.modifier : "");
			return Codec.STRING.encode(key, ops, prefix);
		}

		@Override
		public <T> DataResult<Pair<KeyWithModifier, T>> decode(DynamicOps<T> ops, T input) {
			return Codec.STRING.decode(ops, input).map(result -> result.mapFirst(str -> {
				try {
					if (str.indexOf(':') != -1) {
						String[] pts = str.split(":");
						Key key = InputConstants.getKey(pts[0]);
						KeyModifier modifier = KeyModifier.valueFromString(pts[1]);
						return new KeyWithModifier(key, modifier);
					}
					else {
						Key key = InputConstants.getKey(str);
						return new KeyWithModifier(key);
					}
				}
				catch (IllegalArgumentException keyNotFound) {
					return new KeyWithModifier(InputConstants.UNKNOWN);
				}
			}));
		}
		
	};

	public static record KeyWithModifier(Key key, KeyModifier modifier) {
		
		public KeyWithModifier(Key key) {
			this(key, KeyModifier.NONE);
		}
	}
	
}
