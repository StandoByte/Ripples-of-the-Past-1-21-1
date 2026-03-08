package com.github.standobyte.jojo.client.sound.bgmloop;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.sound.sampled.AudioFormat;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo.BgmLoopPartitioning.BgmPart;
import com.github.standobyte.jojo.client.sound.util.SoundUtil;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.mojang.blaze3d.audio.SoundBuffer;

import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;

public class PartitionedSoundBuffers {
	private final Map<BgmLoopPartitioning, CompletableFuture<Map<BgmPart, SoundBuffer>>> cache = new IdentityHashMap<>();

	public PartitionedSoundBuffers() {}

	public CompletableFuture<Map<BgmPart, SoundBuffer>> getPartitionedBuffers(ResourceLocation soundPath, 
			SoundBufferLibrary fullSoundCache, BgmLoopPartitioning partitioning) {
		return SoundUtil.computeIfKeyAbsent(this.cache, partitioning, 
				_partitioning -> fullSoundCache.getCompleteBuffer(soundPath).thenApply(buffer -> {
					Map<BgmPart, SoundBuffer> partition = new EnumMap<>(BgmPart.class);
					ByteBuffer fullAudio = ClientReflection.getSoundData(buffer);
					int fullSize = fullAudio.limit();
					AudioFormat format = ClientReflection.getAudioFormat(buffer);
					for (var part : _partitioning.partition.entrySet()) {
						ByteBuffer partBuffer = partition(fullAudio, fullSize, format, part.getValue());
						SoundBuffer soundBuffer = new SoundBuffer(partBuffer, format);
						partition.put(part.getKey(), soundBuffer);
					}
					return partition;
				}));
	}

	protected ByteBuffer partition(ByteBuffer fullAudio, int fullSize, AudioFormat format, BgmLoopPartitioning.Partition timing) {
		int start = timeToBytes(timing.start(), format);
		int end = timing.end().isPresent() ? timeToBytes(timing.end().getAsFloat(), format) : fullSize;
		ByteBuffer partition = fullAudio.slice(start, end - start);//.order(ByteOrder.nativeOrder());
		return partition;
	}
	
	protected static int timeToBytes(float time, AudioFormat format) {
		int bytes = (int) (time * format.getSampleSizeInBits() / 8f * format.getChannels() * format.getSampleRate());
		bytes -= bytes % format.getFrameSize();
		return bytes;
	}

	public void clear() {
		this.cache.values().forEach(p_120201_ -> p_120201_.thenAccept(map -> map.values().forEach(SoundBuffer::discardAlBuffer)));
		this.cache.clear();
	}
}
