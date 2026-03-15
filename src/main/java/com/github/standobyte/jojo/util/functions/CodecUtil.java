package com.github.standobyte.jojo.util.functions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class CodecUtil {

	public static <V> Codec<V> placeholderCodec(V defaultValue) {
		return new Codec<V>() {
			@Override public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) { return DataResult.success(ops.empty()); }
			@Override public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) { return DataResult.success(Pair.of(defaultValue, input)); }
		};
	}
}
