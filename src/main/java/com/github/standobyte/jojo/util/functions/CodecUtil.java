package com.github.standobyte.jojo.util.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
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
	
	public static <E> Codec<List<E>> listOrSingleCodec(final Codec<E> elementCodec) {
		return listOrSingleCodec(elementCodec, 0, Integer.MAX_VALUE);
	}
	
	public static <E> Codec<List<E>> listOrSingleCodec(final Codec<E> elementCodec, final int minSize, final int maxSize) {
		return new ListOrSingleCodec<>(elementCodec, minSize, maxSize);
	}
	
	
	/** See {@link com.mojang.serialization.codecs.ListCodec} */
	public record ListOrSingleCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements Codec<List<E>> {
		private <R> DataResult<R> createTooShortError(final int size) {
			return DataResult.error(() -> "List is too short: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
		}

		private <R> DataResult<R> createTooLongError(final int size) {
			return DataResult.error(() -> "List is too long: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
		}

		@Override
		public <T> DataResult<T> encode(final List<E> input, final DynamicOps<T> ops, final T prefix) {
			if (input.size() < minSize) {
				return createTooShortError(input.size());
			}
			if (input.size() > maxSize) {
				return createTooLongError(input.size());
			}
			final ListBuilder<T> builder = ops.listBuilder();
			for (final E element : input) {
				builder.add(elementCodec.encodeStart(ops, element));
			}
			return builder.build(prefix);
		}

		@Override
		public <T> DataResult<Pair<List<E>, T>> decode(final DynamicOps<T> ops, final T input) {
			DataResult<Consumer<Consumer<T>>> listStream = ops.getList(input);
			if (listStream.isSuccess()) {
				return listStream.setLifecycle(Lifecycle.stable()).flatMap(stream -> {
					final DecoderState<T> decoder = new DecoderState<>(ops);
					stream.accept(decoder::accept);
					return decoder.build();
				});
			}
			else {
				return elementCodec.decode(ops, input).map(result -> result.mapFirst(success -> {
					ArrayList<E> list = new ArrayList<>();
					list.add(success);
					return list;
				}));
			}
		}

		@Override
		public String toString() {
			return "ListOrSingleCodec[" + elementCodec + ']';
		}

		private class DecoderState<T> {
			private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

			private final DynamicOps<T> ops;
			private final List<E> elements = new ArrayList<>();
			private final Stream.Builder<T> failed = Stream.builder();
			private DataResult<Unit> result = INITIAL_RESULT;
			private int totalCount;

			private DecoderState(final DynamicOps<T> ops) {
				this.ops = ops;
			}

			public void accept(final T value) {
				totalCount++;
				if (elements.size() >= maxSize) {
					failed.add(value);
					return;
				}
				final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
				elementResult.error().ifPresent(error -> failed.add(value));
				elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
				result = result.apply2stable((result, element) -> result, elementResult);
			}

			public DataResult<Pair<List<E>, T>> build() {
				if (elements.size() < minSize) {
					return createTooShortError(elements.size());
				}
				final T errors = ops.createList(failed.build());
				final Pair<List<E>, T> pair = Pair.of(List.copyOf(elements), errors);
				if (totalCount > maxSize) {
					result = createTooLongError(totalCount);
				}
				return result.map(ignored -> pair).setPartial(pair);
			}
		}
	}
}
