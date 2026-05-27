package com.github.standobyte.jojo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.github.standobyte.jojo.client.shader.ColorShiftEffect;
import com.github.standobyte.jojo.client.shader.ColorShiftShader;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmPlayer;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackLoader;
import com.github.standobyte.jojo.client.sound.bgmloop.DebugBgm;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.network.c2s.ClDebugCommandPacket;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.util.OOPMoment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class DebugItem extends Item {

	public DebugItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (level.isClientSide()) {
        	DebugFunctionsScreen.onDebugItemUsed();
        }
		return InteractionResultHolder.consume(item);
	}

	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		ctx.registries().lookup(JojoRegistries.STORY_PARTS_REG_KEY).ifPresent(registry -> {
			registry.listElements().forEach(holder -> {
				tooltip.add(holder.value().getPartName());
			});
		});
	}
	
	
	public static String[] OPTIONS = new String[] {
			"cycle_bgm",
			"color_shift",
			"__blank1",
			"__blank2",
			"__blank3",
			"__blank4",
			"serialize_stands",
			"track_offhand",
			"drop_tracked"
	};
	public static String[] getOptions() {
		return OPTIONS;
	}
	
	/**
	 * @return true if the option should be sent to the server side for handling
	 */
	public static boolean onClientClick(String option, int mouseButton) {
		return switch (option) {
			case "cycle_bgm" -> {
				switch (mouseButton) {
					case 0 -> {
						StandSkin standSkin = StandSkinsLoader.getCurSkin();
						if (standSkin != null) {
							Weighted<BgmTrackInfo> track = DebugBgm.cycleVariation(DebugBgm.BgmTrackType.STAND_SKINS, standSkin.skinId);
							if (track != null) {
								BgmPlayer player = new BgmPlayer(track);
								player.start();
							}
						}
					}
					case 1 -> {
						BgmPlayer curPlaying = BgmTrackLoader.getInstance().bgmPlaying;
						if (curPlaying != null) {
							curPlaying.finishWithOutro();
						}
					}
				}
				yield false;
			}
			case "color_shift" -> {
				ColorShiftShader colorShift = ModShaders.getInstance().colorShift;
				if (colorShift != null) {
					switch (mouseButton) {
						case 0 -> colorShift.parameters = ColorShiftEffect.Parameters.createRandom(OOPMoment.RANDOM);
						case 1 -> colorShift.parameters = null;
					}
				}
				yield false;
			}
			default -> {
				yield true;
			}
		};
	}
	
	public static void handleServer(String option, ServerPlayer player, int mouseButton) {
		if (!player.hasPermissions(2)) return;
		switch (option) {
			case "track_offhand" -> {
				ItemStack item = player.getOffhandItem();
				if (!item.isEmpty()) {
					ServerLevel level = (ServerLevel) player.level();
					ItemTracking trackingSystem = ItemTracking.getItemTracking(level);
					ItemTracker tracker = trackingSystem.startTracking(item, level);
					tracker.context = "debug";
					tracker.setTrackedByPlayer(player);
				}
			}
			case "drop_tracked" -> {
				ServerLevel level = (ServerLevel) player.level();
				ItemTracking trackingSystem = ItemTracking.getItemTracking(level);
				for (ItemTracker tracker : trackingSystem.values()) {
					ItemStack item = tracker.getItem();
					if (item != null && !item.isEmpty()) {
						Vec3 pos = tracker.getPos(level, 1);
						if (pos != null) {
							ItemStack itemToDrop = tracker.clearAndCopyItem(level);
							ItemEntity dropItem = new ItemEntity(level, pos.x, pos.y, pos.z, itemToDrop);
							level.addFreshEntity(dropItem);
						}
					}
				}
			}
			case "serialize_stands" -> {
				MinecraftServer server = ((ServerLevel) player.level()).getServer();
				Path dir = server.getWorldPath(LevelResource.GENERATED_DIR).normalize();
				for (var standEntry : JojoRegistries.DEFAULT_STANDS_REG.entrySet()) {
					ResourceLocation standId = standEntry.getKey().location();
					Path modIdDir = dir.resolve(standId.getNamespace()).resolve("stands");
					Path file = FileUtil.createPathToResource(modIdDir, standId.getPath(), ".json");
					StandType stand = standEntry.getValue();
					JsonElement standJson = stand.makeConfigTemplate();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					try {
						Files.createDirectories(Files.exists(modIdDir) ? modIdDir.toRealPath() : modIdDir);
						try (Writer writer = new FileWriter(file.toFile())) {
							gson.toJson(standJson, writer);
						}
					} catch (Exception e) {
						JojoMod.getLogger().error("womp womp", e);
					}
				}
			}
			default -> {}
		};
	}
	
	
	
	public static class DebugFunctionsScreen extends Screen {

		public DebugFunctionsScreen() {
			super(CommonComponents.EMPTY);
		}

		@Override
		public void init() {
			super.init();
			String[] commands = DebugItem.getOptions();
			for (int i = 0; i < commands.length; ++i) {
				String command = commands[i];
				Button button = new DebugButton(5, 5 + i * 25, 150, 20, command);
				addRenderableWidget(button);
			}
		}

		public static void onDebugItemUsed() {
			Minecraft.getInstance().setScreen(new DebugFunctionsScreen());
		}

		public static class DebugButton extends Button {
			public String command;

			// this is why the builder approach FUCKING SUCKS DICK, i need to know the mouse button
			// thank fuck the constructor is protected and not private
			public DebugButton(int x, int y, int width, int height, String command) {
				super(x, y, width, height, Component.literal(command), b -> {}, Button.DEFAULT_NARRATION);
				this.command = command;
			}

			@Override
			protected boolean isValidClickButton(int button) {
				return true;
			}

			@Override
			public void onClick(double mouseX, double mouseY, int button) {
				boolean sendPacket = DebugItem.onClientClick(command, button);
				if (sendPacket) {
					PacketDistributor.sendToServer(new ClDebugCommandPacket(command, button));
				}
			}

		}
	}
}
