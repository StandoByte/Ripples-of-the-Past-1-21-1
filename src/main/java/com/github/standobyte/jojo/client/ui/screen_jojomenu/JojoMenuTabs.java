package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.client_screens.StandInfoScreen;
import com.github.standobyte.jojo.powersystem.standpower.client_screens.StandSkillsScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class JojoMenuTabs {
	public static Map<TabCategory, Tab> curTabs = new IdentityHashMap<>();
	public static TabCategory curCategory;
	
	public static Tab getTabToOpenOnMenuKey() {
		TabCategory category = curCategory;
		if (category == null || !category.isActive()) {
			List<TabCategory> active = TabCategory.getActiveCategories();
			if (!active.isEmpty()) {
				category = active.get(0);
			}
		}
		return category != null ? getTabToOpen(category) : null;
	}
	
	public static Tab getTabToOpen(TabCategory category) {
		Tab tab = curTabs.get(category);
		if (tab == null || !tab.isActive()) {
			List<Tab> active = category.getActiveTabs();
			if (!active.isEmpty()) {
				return active.get(0);
			}
		}
		return tab;
	}
	
	public static boolean isSameTabOpened(Tab tab) {
		TabCategory category = tab.getCategory();
		return category == curCategory && curTabs.get(category) == tab;
	}
	
	public static void onTabOpened(Tab tab) {
		if (tab != null) {
			curCategory = tab.getCategory();
			curTabs.put(curCategory, tab);
		}
	}
	
	public static void initDefaults() {}
	
	// Story tabs
	
	public static final TabCategory CATEGORY_STORY = new TabCategory() {}
			.withName(Component.translatable("jojo_ripples.ui.story"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/story.png"), 16, 16));
	
	static {
		if (JojoMod.disableDevStuff()) {
			TabCategory.ALL_CATEGORIES.remove(CATEGORY_STORY);
		}
	}
	
	public static final Tab PLAYER_PROFILE = new Tab(CATEGORY_STORY) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			ClientUtil.renderPlayerFace(guiGraphics.pose(), x, y, Minecraft.getInstance().player);
		}
		
		@Override
		public Component getName() {
			Entity curCharacter = Minecraft.getInstance().player;
			Component curCharacterName = curCharacter != null ? curCharacter.getDisplayName() : CommonComponents.EMPTY;
			return Component.translatable(JojoMod.MOD_ID + ".menu.player.profile", curCharacterName);
		}
	};
	
	public static final Tab GROUP = new Tab(CATEGORY_STORY)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.group"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/group.png"), 16, 16));
	
	public static final Tab STORY_ARCS = new Tab(CATEGORY_STORY)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.story_arcs"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/arcs.png"), 16, 16));
	
	public static final Tab STORYTELLING = new Tab(CATEGORY_STORY) {
		@Override
		public boolean isActive() {
			if (super.isActive()) {
				Minecraft mc = Minecraft.getInstance();
				Player player = mc.player;
				if (player != null && player.hasPermissions(2)) {
					GameType gameMode = mc.gameMode.getPlayerMode();
					return gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR;
				}
			}
			return false;
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.player.storytelling"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/storytelling.png"), 16, 16));
	
	// Stand
	
	public static final TabCategory CATEGORY_STAND = new TabCategory(PowerClass.STAND, null) {
		@Override
		public Component getName() {
			return Component.translatable("jojo_ripples.class.stand", ClientPowerCache.getPower(PowerClass.STAND).getName());
		}
		
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			StandSkin skin = StandSkinsLoader.getCurSkin();
			if (skin != null) {
				this.icon = skin.getStandIcon();
				if (icon != null) {
					super.renderIcon(guiGraphics, x, y);
				}
			}
		}
	};
	
	public static final Tab STAND_INFO = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			PowerHud.renderClientStandIcon(guiGraphics.pose(), x, y);
		}
	}
			.withScreen(tab -> new StandInfoScreen(tab.category, tab))
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.info"));
	
	public static final Tab STAND_SKILLS = new Tab(CATEGORY_STAND)
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skills"))
			.withScreen(tab -> new StandSkillsScreen(Component.empty(), tab.category, tab))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/stand_skills.png"), 16, 16));
	
	public static final Tab STAND_SKINS = new Tab(CATEGORY_STAND) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
			icon = null;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && standPower.hasPower()) {
				ResourceLocation standId = standPower.getPowerType().getId();
				List<StandSkin> allSkins = StandSkinsLoader.getInstance().getStandSkinsView(standId);
				if (!allSkins.isEmpty()) {
					float ticks = ClientTickHandler.tickCount + ClientUtil.partialTick(Minecraft.getInstance().getTimer(), true);
					StandSkin cycledSkin = allSkins.get((int) (ticks / 20) % allSkins.size());
					icon = cycledSkin.getStandIcon();
				}
			}
			super.renderIcon(guiGraphics, x, y);
		}
	}
			.withName(Component.translatable(JojoMod.MOD_ID + ".menu.stand.skins"))
			.withScreen(tab -> {
				return PowerClass.STAND.getOptional(ClientProxy.getClientPlayer()).map(playerStand -> {
					return playerStand.hasPower() ? new StandSkinsScreen(playerStand) : null;
				}).orElse(null);
			});
	
	// Hamon
	
	public static final TabCategory CATEGORY_HAMON = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.HAMON)
			.withName(Component.translatable("power." + JojoMod.MOD_ID + ".hamon"))
			.withIcon(PowerHud.getPowerIcon(ModPlayerPowers.HAMON));
	
	public static final Tab HAMON_INTRO = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.intro.tab"))
			.withIcon(PowerHud.getPowerIcon(ModPlayerPowers.HAMON));
	
	public static final Tab HAMON_STATS = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonStatsIcon();
		}
	}
			.withName(Component.translatable("hamon.stats.tab"));
	
	public static final Tab HAMON_STRENGTH_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.strength_skills.tab"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/hamon/skills_combat.png"), 16, 16));
	
	public static final Tab HAMON_CONTROL_SKILLS = new Tab(CATEGORY_HAMON)
			.withName(Component.translatable("hamon.control_skills.tab"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/hamon/skills_support.png"), 16, 16));
	
	public static final Tab HAMON_TECHNIQUES = new Tab(CATEGORY_HAMON) {
		@Override
		public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
//			renderHamonTechniqueIcon();
		}
	}
			.withName(Component.translatable("hamon.techniques.tab"));
	
	// Vampirism

	public static final TabCategory CATEGORY_VAMPIRISM = new TabCategory(PowerClass.PLAYER_POWER, ModPlayerPowers.VAMPIRISM)
			.withName(Component.translatable("power." + JojoMod.MOD_ID + ".vampirism"))
			.withIcon(PowerHud.getPowerIcon(ModPlayerPowers.VAMPIRISM));

	public static final Tab VAMPIRISM_SKILLS = new Tab(CATEGORY_VAMPIRISM)
			.withName(Component.translatable(JojoMod.MOD_ID + ".vampirism.skills"));
	
	// Controls
	
	public static final TabCategory CATEGORY_CONTROLS = new TabCategory()
			.withName(Component.translatable("jojo_ripples.screen.edit_hud_layout"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/controls.png"), 16, 16));
	
	public static final Tab EDIT_CONTROL_SCHEMES = new Tab(CATEGORY_CONTROLS, null, null) {
		
		@Override
		public boolean isActive() {
			if (isDisabled) return false;
			for (PowerClass<?> powerClass : PowerClass.values()) {
				Power<?> power = ClientPowerCache.getPower(powerClass);
				if (power != null && power.hasPower()) {
					return true;
				}
			}
			return false;
		}
	}		
			.withScreen(tab -> new ControlSchemeScreen(CommonComponents.EMPTY, tab.getCategory(), tab))
			.withName(Component.translatable("jojo_ripples.screen.edit_hud_layout"))
			.withIcon(new GuiIcon(JojoMod.resLoc("textures/gui/controls.png"), 16, 16));
	

}
