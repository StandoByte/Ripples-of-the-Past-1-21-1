package com.github.standobyte.jojo.util.functions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

public class CodecUtil {

	public static <V> Codec<V> placeholderCodec(V defaultValue) {
		return new Codec<V>() {
			@Override public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) { return DataResult.success(ops.empty()); }
			@Override public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) { return DataResult.success(Pair.of(defaultValue, input)); }
		};
	}
	
	public static <V extends Enum<V>> Codec<V> enumCodec(Class<V> enumClass) {
		return PrimitiveCodec.STRING.comapFlatMap(
				name -> {
					try {
						V value = Enum.valueOf(enumClass, name);
						return DataResult.success(value);
					}
					catch (NullPointerException | IllegalArgumentException e) {
						return DataResult.error(e::getMessage);
					}
				}, 
				Enum::name);
	}
	
}
