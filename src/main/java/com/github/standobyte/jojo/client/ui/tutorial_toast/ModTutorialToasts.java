package com.github.standobyte.jojo.client.ui.tutorial_toast;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientTickables;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.stand.StandSkillsScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ModTutorialToasts {
	public static final ToastImpl STAND_SUMMON = new ToastImpl(null, 
			Component.translatable("jojo_ripples.tutorial.summon_stand.tmp", Component.keybind(JojoMod.MOD_ID + ".key.toggle_stand")), 
			null);
//	public static final ToastImpl STAND_SUMMON = new ToastImpl(null, 
//			Component.translatable("jojo_ripples.tutorial.summon_stand.1", Component.keybind(JojoMod.MOD_ID + ".key.toggle_stand")), 
//			Component.translatable("jojo_ripples.tutorial.summon_stand.2", Component.keybind(JojoMod.MOD_ID + ".key.stand_mode")));
	public static final ToastImpl STAND_SKILL = new ToastImpl(null, 
			Component.translatable("jojo_ripples.tutorial.stand_skills.1", Component.keybind(JojoMod.MOD_ID + ".key.jojo_menu")), 
			Component.translatable("jojo_ripples.tutorial.stand_skills.2", Component.keybind(JojoMod.MOD_ID + ".key.jojo_menu")));
	public static final ToastImpl WHEEL_CLICK = new ToastImpl(null, 
			Component.translatable("jojo_ripples.tutorial.wheel_click.1"), 
			Component.translatable("jojo_ripples.tutorial.wheel_click.2"));
	
	@SubscribeEvent
	public static void addToasts(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ToastsHelper toasts = ToastsHelper.getInstance();
			toasts.register("stand_summon", STAND_SUMMON);
			toasts.register("stand_skills", STAND_SKILL);
			toasts.register("wheel_click", WHEEL_CLICK);
		});
		ClientTickables.add(ModTutorialToasts::tick);
	}
	
	public static boolean tick() {
		Minecraft mc = Minecraft.getInstance();
		
		StandPower stand = ClientPowerCache.getPower(PowerClass.STAND);
		if (stand != null && stand.hasPower()) {
			if (!ModTutorialToasts.STAND_SUMMON.isCompleted()) {
				if (stand.isSummoned()) {
					ModTutorialToasts.STAND_SUMMON.setCompleted(true);
				}
			}
			
			if (!ModTutorialToasts.STAND_SKILL.isCompleted()) {
				if (mc.screen instanceof StandSkillsScreen) {
					ModTutorialToasts.STAND_SKILL.setCompleted(true);
				}
			}
			
			
			if (mc.player != null && mc.player.tickCount >= 200) {
				if (ModTutorialToasts.STAND_SUMMON.canShow()) {
					if (!stand.isSummoned() && stand.getPowerType().hasSummonMechanic(stand)) {
						ToastsHelper.showOneAtATimeWithCooldown(ModTutorialToasts.STAND_SUMMON, 
								toast -> toast.showOnceTimed(TimedToast.USUAL_DURATION));
					}
				}
				
				if (ModTutorialToasts.STAND_SKILL.canShow()) {
					boolean isTestingInCreative = StandTypePersistentData.hasInfiniteExp(stand.getUser());
					if (isTestingInCreative) {
						var hudControlScheme = InputHandler.getInstance().getActiveControlScheme();
						boolean hasStandHudOpen = hudControlScheme != null && hudControlScheme.powerClassCosmetic == PowerClass.STAND;
						if (hasStandHudOpen) {
							ToastsHelper.showOneAtATimeWithCooldown(ModTutorialToasts.STAND_SKILL, 
									toast -> toast.showOnceTimed(TimedToast.USUAL_DURATION));
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public static void onStandExpAddedMakingNewSkillsAvailable() {
		if (ModTutorialToasts.STAND_SKILL.canShow()) {
			ToastsHelper.showOneAtATimeWithCooldown(ModTutorialToasts.STAND_SKILL, 
					toast -> toast.showOnceTimed(TimedToast.USUAL_DURATION));
		}
	}

	private static int timesAbilityWheelOpened = 0;
	public static void onAbilityWheelOpened() {
		if (++timesAbilityWheelOpened >= 3 && ModTutorialToasts.WHEEL_CLICK.canShow()) {
			ToastsHelper.showOneAtATimeWithCooldown(ModTutorialToasts.WHEEL_CLICK, 
					toast -> toast.showOnceTimed(TimedToast.USUAL_DURATION));
		}
	}
	
	public static void onAbilityWheelClickedAbility() {
		ModTutorialToasts.WHEEL_CLICK.setCompleted(true);
	}
	
	
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void preRegister(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ToastsHelper.init();
		});
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void postRegister(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ToastsHelper.getInstance().loadFromFileSystem();
		});
	}
}
