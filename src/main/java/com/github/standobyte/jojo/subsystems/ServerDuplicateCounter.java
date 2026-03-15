package com.github.standobyte.jojo.subsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.objects_java.ReuseableStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ServerDuplicateCounter {
	public UniquenessPriority _mode;
	public Map<ResourceLocation, MutableInt> _serverWideCounter = new HashMap<>();
	public Map<UUID, Map<ResourceLocation, MutableInt>> _perPlayerCounters = new HashMap<>();

	public ServerDuplicateCounter(UniquenessPriority mode) {
		this._mode = mode;
	}
	
	public static record EntryCounter<T>(T value, ResourceLocation id, int seenByPlayer, int seenOnServer) {}

	public <T> Stream<T> getMostUnique(UUID playerId, Stream<T> eligible, Function<T, ResourceLocation> getId) {
		Map<ResourceLocation, MutableInt> playerCounter = _perPlayerCounters.get(playerId);
		Stream<EntryCounter<T>> counters = eligible.map(entry -> {
			ResourceLocation id = getId.apply(entry);
			return new EntryCounter<>(entry, id, 
					playerCounter != null ? getValue(playerCounter.get(id)) : 0, 
					getValue(_serverWideCounter.get(id)));
		});
		
		Stream<EntryCounter<T>> mostUnique;
		switch (_mode) {
			case FOR_PLAYER -> {
				mostUnique = getAllMin(getAllMin(counters, 
						EntryCounter::seenByPlayer), 
						EntryCounter::seenOnServer);
			}
			case ON_SERVER -> {
				mostUnique = getAllMin(getAllMin(counters, 
						EntryCounter::seenOnServer), 
						EntryCounter::seenByPlayer);
			}
			case FOR_PLAYER_UNIQUE -> {
				mostUnique = getAllMin(getAllMin(counters, 
						EntryCounter::seenByPlayer, 0), 
						EntryCounter::seenOnServer);
			}
			case ON_SERVER_UNIQUE -> {
				mostUnique = getAllMin(getAllMin(counters, 
						EntryCounter::seenOnServer, 0), 
						EntryCounter::seenByPlayer);
			}
			default -> throw new UnsupportedOperationException("Unimplemented case: " + _mode);
		}
		return mostUnique.map(EntryCounter::value);
	}

	public void incrementCounter(UUID playerId, ResourceLocation valueId) {
		_perPlayerCounters.computeIfAbsent(
				playerId, __ -> new HashMap<>()).computeIfAbsent(
						valueId, __ -> new MutableInt(0)).increment();
		_serverWideCounter.computeIfAbsent(valueId, __ -> new MutableInt(0)).increment();
	}
	
	public void decrementCounter(UUID playerId, ResourceLocation valueId) {
		Map<ResourceLocation, MutableInt> perPlayer = this._perPlayerCounters.get(playerId);
		if (perPlayer != null) {
			MutableInt count = perPlayer.get(valueId);
			if (count != null) count.decrement();
		}
		MutableInt count = _serverWideCounter.get(valueId);
		if (count != null) count.decrement();
	}
	
	/*
	 * Possible use case - Stand rerolls
	 * (which won't be a thing in 1.21.1 base ROTP, but idk, maybe someone will want to make an old-style server with them)
	 */
	public void decrementOnlyOnServer(ResourceLocation valueId) {
		MutableInt count = _serverWideCounter.get(valueId);
		if (count != null) count.decrement();
	}
	
	
	public int getSeenOnServer(ResourceLocation entryId) {
		return getValue(_serverWideCounter.get(entryId));
	}
	
	public int getSeenByPlayer(UUID playerId, ResourceLocation entryId) {
		Map<ResourceLocation, MutableInt> playerCounters = _perPlayerCounters.get(playerId);
		return playerCounters != null ? getValue(playerCounters.get(entryId)) : 0;
	}
	
	
	public static int getValue(@Nullable MutableInt counter) {
		return counter != null ? counter.intValue() : 0;
	}
	
	public static <T> Stream<T> getAllMin(Stream<T> stream, ToIntFunction<? super T> mapper) {
		ReuseableStream<T> _stream = new ReuseableStream<>(stream);
		int minValue = _stream.getStream().mapToInt(mapper).min().orElse(0);
		return _stream.getStream().filter(entry -> mapper.applyAsInt(entry) == minValue);
	}
	
	public static <T> Stream<T> getAllMin(Stream<T> stream, ToIntFunction<? super T> mapper, int upperLimit) {
		ReuseableStream<T> _stream = new ReuseableStream<>(stream);
		int minValue = _stream.getStream().mapToInt(mapper).min().orElse(0);
		if (minValue > upperLimit) return Stream.empty();
		return _stream.getStream().filter(entry -> mapper.applyAsInt(entry) == minValue);
	}


	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		CompoundTag onServer = new CompoundTag();
		for (var serverCounterEntry : _serverWideCounter.entrySet()) {
			onServer.putInt(serverCounterEntry.getKey().toString(), serverCounterEntry.getValue().intValue());
		}
		nbt.put("server", onServer);
		
		CompoundTag perPlayer = new CompoundTag();
		for (var perPlayerMap : _perPlayerCounters.entrySet()) {
			CompoundTag forPlayer = new CompoundTag();
			for (var playerCounterEntry : perPlayerMap.getValue().entrySet()) {
				forPlayer.putInt(playerCounterEntry.getKey().toString(), playerCounterEntry.getValue().intValue());
			}
			perPlayer.put(perPlayerMap.getKey().toString(), forPlayer);
		}
		nbt.put("players", perPlayer);
		
		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		_serverWideCounter.clear();
		NBTUtil.getCompoundOptional(nbt, "server").ifPresent(onServer -> {
			for (String entry : onServer.getAllKeys()) {
				_serverWideCounter.put(ResourceLocation.parse(entry), new MutableInt(onServer.getInt(entry)));
			}
		});
		
		_perPlayerCounters.clear();
		NBTUtil.getCompoundOptional(nbt, "players").ifPresent(perPlayer -> {
			for (String playerId : perPlayer.getAllKeys()) {
				var perPlayerMap = _perPlayerCounters.computeIfAbsent(UUID.fromString(playerId), __ -> new HashMap<>());
				for (String entry : perPlayer.getCompound(playerId).getAllKeys()) {
					perPlayerMap.put(ResourceLocation.parse(entry),new MutableInt(perPlayer.getInt(entry)));
				}
			}
		});
	}


	public enum UniquenessPriority {
		/* filter for the entries most unique FOR THE PLAYER, 
		 * then pick the ones most unique ON THE SERVER 
		 * (for PvE-focused content)
		 */
		FOR_PLAYER,
		/* filter for the entries most unique ON THE SERVER, 
		 * then pick the ones most unique FOR THE PLAYER 
		 * (for PvP-focused content)
		 */
		ON_SERVER,
		
		/* an extra condition that the player won't get an entry they've already seen
		 * (for example, special encounters)
		 */
		FOR_PLAYER_UNIQUE,
		/* same for servers (NOT_TAKEN standRandomPoolFilter config setting)
		 */
		ON_SERVER_UNIQUE
	}

	
	
	
	
	public static void test() {
		ServerDuplicateCounter test = new ServerDuplicateCounter(ServerDuplicateCounter.UniquenessPriority.FOR_PLAYER);
		int entriesCount = 5;
		int playersCount = 3;
		
		List<ResourceLocation> entries = new ArrayList<>();
		UUID[] players = new UUID[playersCount];
		
		for (int i = 0; i < entriesCount; i++) {
			entries.add(ResourceLocation.fromNamespaceAndPath("entry", String.valueOf(i)));
		}
		
		for (int i = 0; i < players.length; i++) {
			boolean duplicatePlayer;
			do {
				players[i] = UUID.randomUUID();
				duplicatePlayer = false;
				for (int j = 0; j < i && !duplicatePlayer; j++) {
					if (players[i].equals(players[j])) {
						duplicatePlayer = true;
					}
				}
			} while (duplicatePlayer);
		}
		
		for (int i = 0; i < 3; i++) test.incrementCounter(players[0], entries.get(0));
		for (int i = 0; i < 3; i++) test.incrementCounter(players[0], entries.get(1));
		for (int i = 0; i < 6; i++) test.incrementCounter(players[0], entries.get(2));
		for (int i = 0; i < 1; i++) test.incrementCounter(players[0], entries.get(3));
		for (int i = 0; i < 1; i++) test.incrementCounter(players[0], entries.get(4));
		
		for (int i = 0; i < 10; i++) test.incrementCounter(players[1], entries.get(3));
		for (int i = 0; i < 10; i++) test.incrementCounter(players[1], entries.get(4));
		
		for (int i = 0; i < 20; i++) test.incrementCounter(players[2], entries.get(0));
		for (int i = 0; i < 4; i++) test.incrementCounter(players[2], entries.get(1));
		for (int i = 0; i < 1; i++) test.incrementCounter(players[2], entries.get(2));

		// Note: Add -ea to VM args to enable assert
		test._mode = ServerDuplicateCounter.UniquenessPriority.FOR_PLAYER;
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[0], entries.stream(), Function.identity()), 
				entries.get(3), entries.get(4));
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[1], entries.stream(), Function.identity()), 
				entries.get(1), entries.get(2));
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[2], entries.stream(), Function.identity()), 
				entries.get(3), entries.get(4));
		
		test._mode = ServerDuplicateCounter.UniquenessPriority.ON_SERVER;
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[0], entries.stream(), Function.identity()), 
				entries.get(1));
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[1], entries.stream(), Function.identity()), 
				entries.get(1), entries.get(2));
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[2], entries.stream(), Function.identity()), 
				entries.get(2));
		
		test._mode = ServerDuplicateCounter.UniquenessPriority.FOR_PLAYER_UNIQUE;
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[0], entries.stream(), Function.identity())
				);
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[1], entries.stream(), Function.identity()), 
				entries.get(1), entries.get(2));
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[2], entries.stream(), Function.identity()), 
				entries.get(3), entries.get(4));
		
		test._mode = ServerDuplicateCounter.UniquenessPriority.ON_SERVER_UNIQUE;
		assert ServerDuplicateCounter.checkStream(test.getMostUnique(players[2], entries.stream(), Function.identity())
				);
		
		JojoMod.getLogger().debug("You are not a complete dumbass, congrats!!");
	}
	
	public static <T> boolean checkStream(Stream<T> stream, T... assertValues) {
		Set<T> filtered = stream.collect(Collectors.toSet());
		Set<T> asserted = Arrays.stream(assertValues).collect(Collectors.toSet());
		if (filtered.size() != asserted.size()) return false;
		for (T filteredElem : filtered) {
			if (!asserted.contains(filteredElem)) return false;
		}
		return true;
	}
}
