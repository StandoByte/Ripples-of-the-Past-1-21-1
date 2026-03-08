package com.github.standobyte.jojo.core.command;

import java.util.Collection;

import com.github.standobyte.jojo.client.sound.bgmloop.BgmPlayer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/* 
 * XXX (bgm) suggestions based on the local client's resources
 * (the suggestions in vanilla are server-side, i might have to somehow hack into ClientPacketListener#suggestionsProvider or something, 
 * i still want to keep at least some of my brain cells so not gonna bother with this shit any time soon)
 */
public class PlayBgmCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(
		Commands.literal(JojoMod.MOD_ID).then(
			Commands.literal("bgm")
				.requires(src -> src.hasPermission(2))
				
				.then(
				Commands.literal("play")
					.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
						Commands.argument("track", ResourceLocationArgument.id())
							.executes(
							src -> playTrack(
								src.getSource(),
								EntityArgument.getPlayers(src, "targets"),
								ResourceLocationArgument.getId(src, "track")
								)
							)
						)
					)
				)
				
				.then(
				Commands.literal("preload")
					.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
						Commands.argument("track", ResourceLocationArgument.id())
							.executes(
							src -> preloadTrack(
								src.getSource(),
								EntityArgument.getPlayers(src, "targets"),
								ResourceLocationArgument.getId(src, "track")
								)
							)
						)
					)
				)
				
				.then(
				Commands.literal("finish")
					.executes(src -> finishTrack(src.getSource(), ImmutableList.of(src.getSource().getPlayerOrException())))
					.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(src -> finishTrack(src.getSource(), EntityArgument.getPlayers(src, "targets")))
					)
				)
				
				.then(
				Commands.literal("stop")
					.executes(src -> stopTrack(src.getSource(), ImmutableList.of(src.getSource().getPlayerOrException())))
					.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(src -> stopTrack(src.getSource(), EntityArgument.getPlayers(src, "targets")))
					)
				)
			)
		);
		JojoCommandsCommand.addCommand("bgm");
	}


	public static int playTrack(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation track) throws CommandSyntaxException {
        int i = 0;

		for (ServerPlayer player : targets) {
			player.connection.send(new PlayBgmCommandPacket(PlayBgmCommandPacket.PacketType.PLAY, track));
			i++;
		}

		if (source != null) {
			Component trackName = Component.translatable("track." + track.getNamespace() + "." + track.getPath());
			if (targets.size() == 1) {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.play.success.single", 
						trackName, 
						targets.iterator().next().getDisplayName()),
						false);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.play.success.multiple", 
						trackName, 
						targets.size()), 
						false);
			}
		}

		return i;
	}

	public static int preloadTrack(CommandSourceStack source, Collection<ServerPlayer> targets, ResourceLocation track) throws CommandSyntaxException {
        int i = 0;

		for (ServerPlayer player : targets) {
			player.connection.send(new PlayBgmCommandPacket(PlayBgmCommandPacket.PacketType.PRELOAD, track));
			i++;
		}

		// TODO (bgm) a function to preload sounds
//		if (source != null) {
//			Component trackName = Component.translatable("track." + track.getNamespace() + "." + track.getPath());
//			if (targets.size() == 1) {
//				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.preload.success.single", 
//						trackName, 
//						targets.iterator().next().getDisplayName()),
//						true);
//			} else {
//				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.preload.success.multiple", 
//						trackName, 
//						targets.size()), 
//						true);
//			}
//		}

		return i;
	}

	public static int finishTrack(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int i = 0;

		for (ServerPlayer player : targets) {
			player.connection.send(new PlayBgmCommandPacket(PlayBgmCommandPacket.PacketType.FINISH, null));
			i++;
		}

		if (source != null) {
			if (targets.size() == 1) {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.finish.success.single", 
						targets.iterator().next().getDisplayName()),
						false);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.finish.success.multiple", 
						targets.size()), 
						false);
			}
		}

		return i;
	}

	public static int stopTrack(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        int i = 0;

		for (ServerPlayer player : targets) {
			player.connection.send(new PlayBgmCommandPacket(PlayBgmCommandPacket.PacketType.ABRUPT_STOP, null));
			i++;
		}

		if (source != null) {
			if (targets.size() == 1) {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.finish.success.single", 
						targets.iterator().next().getDisplayName()),
						false);
			} else {
				source.sendSuccess(() -> Component.translatable("commands.jojo_ripples.bgm.finish.success.multiple", 
						targets.size()), 
						false);
			}
		}

		return i;
	}
	
	
	public static record PlayBgmCommandPacket(PacketType packetType, ResourceLocation trackId) implements CustomPacketPayload {
		
		private static CustomPacketPayload.Type<PlayBgmCommandPacket> type;
		
		public enum PacketType {
			PRELOAD,
			PLAY,
			FINISH,
			ABRUPT_STOP
		}
		
		public static class Handler implements PacketsRegister.PacketOGHandler<PlayBgmCommandPacket> {
			
			public Handler(ResourceLocation packetId) { 
				type = new CustomPacketPayload.Type<>(packetId);
			}

			@Override
			public Type<PlayBgmCommandPacket> type() {
				return type;
			}
			
			@Override
			public void encode(PlayBgmCommandPacket packet, RegistryFriendlyByteBuf buf) {
				buf.writeEnum(packet.packetType);
				switch (packet.packetType) {
					case PLAY, PRELOAD -> {
						buf.writeResourceLocation(packet.trackId);
					}
					default -> {}
				}
			}
			
			@Override
			public PlayBgmCommandPacket decode(RegistryFriendlyByteBuf buf) {
				PacketType packetType = buf.readEnum(PacketType.class);
				return switch (packetType) {
					case PLAY, PRELOAD -> {
						ResourceLocation trackId = buf.readResourceLocation();
						yield new PlayBgmCommandPacket(packetType, trackId);
					}
					default -> new PlayBgmCommandPacket(packetType, null);
				};
			}

			@Override
			public void handle(PlayBgmCommandPacket payload, IPayloadContext context) {
				switch (payload.packetType) {
					// TODO (bgm) a function to preload sounds
					case PRELOAD -> {
					}
					case PLAY -> {
		        		BgmPlayer track = BgmPlayer.track(payload.trackId);
		        		BgmPlayer.start(track);
					}
					case FINISH -> {
						BgmPlayer curBgm = BgmPlayer.getCurTrackPlaying();
						if (curBgm != null) {
							curBgm.finishWithOutro();
						}
					}
					case ABRUPT_STOP -> {
						BgmPlayer curBgm = BgmPlayer.getCurTrackPlaying();
						if (curBgm != null) {
							curBgm.stopSound();
						}
					}
				}
			}
			
		}
		
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return type;
		}

	}

}
