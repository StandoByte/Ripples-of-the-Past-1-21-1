package com.github.standobyte.jojoimpl.stands.hierophant;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.event.RipplesAbilityKeyPressEvent;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.mob.ClientMobController;
import com.github.standobyte.jojo.mechanics.possessionv2.LivingComponentPossession;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity.StandFlag;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.entitycomponent.ComponentUtil;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityManualControlToggle;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class HierophantPuppetEffect extends StandEffectInstance {

	public HierophantPuppetEffect(StandEffectType<?> effectType) {
		super(effectType);
		needsTarget = true;
	}

	@Override
	protected void start() {
		if (!level.isClientSide() || user == ClientProxy.getClientPlayer()) {
			LivingEntity targetEntity = getTargetLiving();
			if (targetEntity != null) {
				StandPower standPower = getUserPower();
				StandEntity hierophant = standPower.getSummonedStandEntity();
				if (hierophant != null) {
					if (!level.isClientSide()) {
						LivingComponentPossession.setPossessionTarget(hierophant, targetEntity, "hierophant");
						hierophant.setCanFollowUser(false);
					}
					setMobControl(true);
				}
			}
		}
	}

	@Override
	protected void tick() {
	}

	@Override
	protected void stop() {
		LivingEntity user = getStandUser();
		Level level = user.level();

		if (!level.isClientSide() || user == ClientProxy.getClientPlayer()) {
			StandEntity hierophant = getUserPower().getSummonedStandEntity();
			if (hierophant != null) {
				boolean wasInMobControl = hierophant.getStandFlag(StandFlag.MANUAL_CONTROL);
				setMobControl(false);
				
				LivingComponentPossession possession = ComponentUtil.getExistingDataOrNull(hierophant, ModDataAttachmentTypes.ENTITY_POSSESSION);
				if (possession != null) {
					possession.updatePosition();
					if (!level.isClientSide()) {
						possession.stopPossession();
					}
				}
				
				hierophant.setCanFollowUser(true);
				if (wasInMobControl) {
					StandEntityManualControlToggle.on(level, hierophant);
				}
				else {
					hierophant.retract();
				}
			}
		}
	}
	
	public void setMobControl(boolean control) {
		LivingEntity user = getStandUser();
		
		if (!user.level().isClientSide()) {
			if (control) {
				if (getTargetLiving() instanceof Mob targetMob) {
					EntityComponentController.setControlTarget(user, targetMob, "mob");
				}
			}
			else {
				EntityComponentController component = ComponentUtil.getExistingDataOrNull(user, ModDataAttachmentTypes.CONTROLLER);
				if (component != null) {
					component.stopControlling();
				}
			}
			
			StandEntity hierophant = getUserPower().getSummonedStandEntity();
			if (hierophant != null) {
				hierophant.setManuallyControlled(control);
			}
		}
		else if (user == ClientProxy.getClientPlayer()) {
			if (control) {
				if (getTargetLiving() instanceof Mob targetMob) {
					ClientEntityController.setInstance(new ClientMobController(targetMob));
				}
			}
			else {
				ClientEntityController.setInstance(null);
			}
		}
	}

	
	
	@SubscribeEvent
	public static void onManualControlToggle(RipplesAbilityKeyPressEvent event) {
		Ability ability = event.getAbility();
		AbilityId abilityId = ability.abilityId;
		if (abilityId.powerClass() == PowerClass.STAND && abilityId.nameInMoveset().equals("manual_control")) {
			LivingEntity user = event.getEntity();
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				HierophantPuppetEffect puppeting = standPower.userStandEffects
						.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get())
						.orElse(null);
				if (puppeting != null) {
					event.setCanceled(true);
					
					StandEntity stand = StandUtil.getSummonedStand(user);
					if (stand != null) {
						puppeting.setMobControl(!stand.getStandFlag(StandEntity.StandFlag.MANUAL_CONTROL));
					}
				}
			}
		}
	}
}
