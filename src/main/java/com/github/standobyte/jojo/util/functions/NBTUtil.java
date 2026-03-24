package com.github.standobyte.jojo.util.functions;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class NBTUtil {

	public static Optional<CompoundTag> getCompoundOptional(CompoundTag nbt, String key) {
		if (nbt.contains(key, Tag.TAG_COMPOUND)) {
			return Optional.of(nbt.getCompound(key));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Tag> Optional<T> getElementOptional(CompoundTag nbt, String key, Class<T> nbtClass) {
		if (nbt.contains(key, Types.Tag_ID.get(nbtClass))) {
			return (Optional<T>) Optional.of(nbt.get(key));
		}
		return Optional.empty();
	}

	public static Optional<ResourceLocation> getResLocOptional(CompoundTag nbt, String key) {
		return getElementOptional(nbt, key, StringTag.class)
				.map(Tag::getAsString)
				.map(ResourceLocation::parse);
	}
	
	public static <T> void put(CompoundTag nbt, String key, @Nullable T value, Encoder<T> codec) {
		if (value != null) {
			codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(valueNbt -> nbt.put(key, valueNbt));
		}
	}
	
	public static <T> Optional<T> getOptional(CompoundTag nbt, String key, Decoder<T> codec) {
		Tag valueNbt = nbt.get(key);
		if (valueNbt == null) return Optional.empty();
		
		return codec.decode(NbtOps.INSTANCE, valueNbt).result().map(Pair::getFirst);
	}
	
	public static <T extends Enum<T>> void putEnum(CompoundTag nbt, String key, T enumVal) {
		nbt.putInt(key, enumVal.ordinal());
	}
	
	@Nullable
	public static <T extends Enum<T>> T getEnum(CompoundTag nbt, String key, Class<T> enumClass) {
		if (!nbt.contains(key, Tag.TAG_INT)) {
			return null;
		}
		
		int ordinal = nbt.getInt(key);
		T[] values = enumClass.getEnumConstants();
		if (ordinal >= 0 && ordinal < values.length) {
			return values[ordinal];
		}
		return null;
	}
	
	public static <V> ListTag toList(Iterable<V> collection, Encoder<V> elementCodec) {
		return toList(collection, element -> elementCodec.encodeStart(NbtOps.INSTANCE, element).result().orElse(null));
	}
	
	public static <V> ListTag toList(Iterable<V> collection, Function<V, Tag> encoder) {
		ListTag listNbt = new ListTag();
		for (V value : collection) {
			Tag elementNbt = encoder.apply(value);
			if (elementNbt != null) {
				listNbt.add(elementNbt);
			}
		}
		return listNbt;
	}
	
	public static <V> void fromList(CompoundTag nbt, String key, Collection<V> destination, Decoder<V> elementCodec) {
		fromList(nbt, key, destination::add, elementCodec);
	}
	
	public static <V> void fromList(CompoundTag nbt, String key, Consumer<V> destination, Decoder<V> elementCodec) {
		fromList(nbt, key, destination, elementNbt -> elementCodec.decode(NbtOps.INSTANCE, elementNbt).result().map(Pair::getFirst).orElse(null));
	}
	
	public static <V> void fromList(CompoundTag nbt, String key, Consumer<V> destination, Function<Tag, V> decoder) {
		ListTag listNbt = NBTUtil.getElementOptional(nbt, key, ListTag.class).orElse(null);
		if (listNbt != null) {
			for (Tag elementNbt : listNbt) {
				V element = decoder.apply(elementNbt);
				if (element != null) {
					destination.accept(element);
				}
			}
		}
	}
	
	public static class Types {
		
		static final ImmutableMap<Class<? extends Tag>, Byte> Tag_ID = new ImmutableMap.Builder<Class<? extends Tag>, Byte>()
				.put(EndTag.class, Tag.TAG_END)
				.put(ByteTag.class, Tag.TAG_BYTE)
				.put(ShortTag.class, Tag.TAG_SHORT)
				.put(IntTag.class, Tag.TAG_INT)
				.put(LongTag.class, Tag.TAG_LONG)
				.put(FloatTag.class, Tag.TAG_FLOAT)
				.put(DoubleTag.class, Tag.TAG_DOUBLE)
				.put(ByteArrayTag.class, Tag.TAG_BYTE_ARRAY)
				.put(StringTag.class, Tag.TAG_STRING)
				.put(ListTag.class, Tag.TAG_LIST)
				.put(CompoundTag.class, Tag.TAG_COMPOUND)
				.put(IntArrayTag.class, Tag.TAG_INT_ARRAY)
				.put(LongArrayTag.class, Tag.TAG_LONG_ARRAY)
				.build();
	}
}
