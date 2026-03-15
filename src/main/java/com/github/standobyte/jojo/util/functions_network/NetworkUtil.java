package com.github.standobyte.jojo.util.functions_network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.connection.ConnectionType;

public class NetworkUtil {
	
	public static Stream<ServerPlayerConnection> getTrackingPlayers(Entity entity) {
		if (entity.level().isClientSide()) {
			throw new IllegalStateException("Cannot get tracking players on the client");
		}
		else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
			ChunkMap.TrackedEntity trackedEntity = chunkCache.chunkMap.entityMap.get(entity.getId());
			if (trackedEntity != null) {
				return trackedEntity.seenBy.stream();
			}
		}
		return Stream.empty();
	}
	
	/**
	 * For when you have a bunch of packet data that you can only decode in the packet handler,
	 * once you resolve some context-dependent stuff (e.g. an entity from entity id).
	 * Splits the remaining data into a separate buffer, 
	 * because if you don't read all the bytes from the buffer in the packet decoder the game will kick you.
	 */
	@Nullable
	public static FriendlyByteBuf extraPacketData(FriendlyByteBuf remainingData) {
		int extraInputBytes = remainingData.readableBytes();
		return extraInputBytes > 0 ? new FriendlyByteBuf(remainingData.readBytes(extraInputBytes)) : null;
	}
	
	@Nullable
	public static RegistryFriendlyByteBuf extraPacketData(FriendlyByteBuf remainingData, RegistryAccess registryAccess) {
		int extraInputBytes = remainingData.readableBytes();
		return extraInputBytes > 0 ? new RegistryFriendlyByteBuf(remainingData.readBytes(extraInputBytes), registryAccess, ConnectionType.NEOFORGE) : null;
	}
	
	// StreamCodec stuff below
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> nullableCodec(StreamCodec<? super B, T> codec) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {
				ByteBufCodecs.BOOL.encode(buffer, value != null);
				if (value != null) {
					codec.encode(buffer, value);
				}
			}

			@Override
			public T decode(B buffer) {
				boolean isPresent = ByteBufCodecs.BOOL.decode(buffer);
				return isPresent ? codec.decode(buffer) : null;
			}
		};
	}
	
	public static final StreamCodec<ByteBuf, JsonObject> JSON_OBJECT_CODEC = ByteBufCodecs.STRING_UTF8
			.map(JSONUtil::parse, JsonObject::toString);
	
	public static <B extends FriendlyByteBuf, T> StreamCodec<B, Collection<T>> collectionCodec(StreamCodec<? super B, T> elementCodec) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, Collection<T> collection) {
				writeCollection(buffer, collection, elementCodec);
			}

			@Override
			public List<T> decode(B buffer) {
				return readCollection(buffer, elementCodec);
			}
		};
	}
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> emptyObjectCodec(Supplier<T> constructor) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {}

			@Override
			public T decode(B buffer) {
				return constructor.get();
			}
			
		};
	}
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> extraDynamicData(StreamCodec<B, T> codec, BiConsumer<T, B> encodeExtra, BiConsumer<B, T> decodeExtra) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {
				codec.encode(buffer, value);
				encodeExtra.accept(value, buffer);
			}

			@Override
			public T decode(B buffer) {
				T value = codec.decode(buffer);
				decodeExtra.accept(buffer, value);
				return value;
			}
			
		};
	}
	
	private static final Map<ResourceKey<? extends Registry<?>>, StreamCodec<RegistryFriendlyByteBuf, ?>> CODECS_CACHE = new HashMap<>();
	@SuppressWarnings("unchecked")
	public static <T> StreamCodec<RegistryFriendlyByteBuf, T> registryCodec(ResourceKey<? extends Registry<T>> registryKey) {
		// this shit doesn't compile because of generics bs
//		return (StreamCodec<RegistryFriendlyByteBuf, T>) CODECS_CACHE.computeIfAbsent(registryKey, ByteBufCodecs::registry);
		if (CODECS_CACHE.containsKey(registryKey)) {
			return (StreamCodec<RegistryFriendlyByteBuf, T>) CODECS_CACHE.get(registryKey);
		}
		else {
			StreamCodec<RegistryFriendlyByteBuf, T> codec = ByteBufCodecs.registry(registryKey);
			CODECS_CACHE.put(registryKey, codec);
			return codec;
		}
	}
	
	// FriendlyByteBuf stuff
	
	public static <T, B extends FriendlyByteBuf> void writeOptionally(@Nullable T value, B buf, StreamEncoder<? super B, T> writer) {
		if (value != null) {
			buf.writeBoolean(true);
			writer.encode(buf, value);
		} else {
			buf.writeBoolean(false);
		}
	}
	
	public static <T, B extends FriendlyByteBuf> void writeOptional(@Nullable Optional<T> value, B buf, StreamEncoder<? super B, T> writer) {
		if (value.isPresent()) {
			buf.writeBoolean(true);
			writer.encode(buf, value.get());
		} else {
			buf.writeBoolean(false);
		}
	}
	
	public static <T, B extends FriendlyByteBuf> Optional<T> readOptional(B buf, StreamDecoder<? super B, T> reader) {
		return buf.readBoolean() ? Optional.of(reader.decode(buf)) : Optional.empty();
	}

	public static void writeOptionalInt(FriendlyByteBuf buf, OptionalInt optional, boolean varInt) {
		buf.writeBoolean(optional.isPresent());
		optional.ifPresent(value -> {
			if (varInt) {
				buf.writeVarInt(value);
			}
			else {
				buf.writeInt(value);
			}
		});
	}

	public static OptionalInt readOptionalInt(FriendlyByteBuf buf, boolean varInt) {
		if (!buf.readBoolean()) {
			return OptionalInt.empty();
		}
		int value = varInt ? buf.readVarInt() : buf.readInt();
		return OptionalInt.of(value);
	}

	
	public static <T, B extends FriendlyByteBuf> void writeAsSingletonCollection(B buf, T obj, StreamEncoder<? super B, T> writer) {
		buf.writeInt(1);
		writer.encode(buf, obj);
	}

	public static <T, B extends FriendlyByteBuf> int writeCollection(B buf, Collection<T> collection, StreamEncoder<? super B, T> writer) {
		int i = 0;
		int initialWriterIndex = buf.writerIndex();
		buf.writeInt(0);

		int lastWriterIndex = initialWriterIndex;
		int maxElemSize = 0;
		Iterator<T> iter = collection.iterator();
		while (iter.hasNext()) {
			if (buf.capacity() < maxElemSize) break;
			T element = iter.next();
			writer.encode(buf, element);
			i++;
			int writerIndex = buf.writerIndex();
			maxElemSize = Math.max(maxElemSize, writerIndex - lastWriterIndex);
			lastWriterIndex = writerIndex;
		}

		buf.setInt(initialWriterIndex, i);
		return i;
	}

	public static <T, B extends FriendlyByteBuf, C extends Collection<T>> C readCollection(Supplier<C> createCollection, B buf, StreamDecoder<? super B, T> readElement) {
		C collection = createCollection.get();
		int size = buf.readInt();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				collection.add(readElement.decode(buf));
			}
		}
		return collection;
	}

	public static <T, B extends FriendlyByteBuf> List<T> readCollection(B buf, StreamDecoder<? super B, T> readElement) {
		return readCollection(ArrayList::new, buf, readElement);
	}

}
