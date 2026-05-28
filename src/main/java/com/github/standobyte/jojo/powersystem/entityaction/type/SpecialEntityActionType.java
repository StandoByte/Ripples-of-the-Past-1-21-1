package com.github.standobyte.jojo.powersystem.entityaction.type;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * An EntityActionType, that plays an action over multiple ticks and can have its entity animation,
 * but is not registered as an ability in a power's moveset, instead using its own registry.
 * Can be used for stuff like special weapon attacks or item abilities.
 */
public class SpecialEntityActionType implements EntityActionType {
	public final ResourceLocation id;
	protected Function<EntityActionType, ? extends EntityActionInstance> createActionObj;
	@Nullable protected ResourceLocation animSet;
	@Nullable protected ActionAnimIdentifier anim;
	protected AbilityId abilityId;
	
	@Deprecated
	public SpecialEntityActionType(@Nullable String animFileName, ResourceLocation id) {
		this(animFileName, id, EntityActionInstance::new);
	}
	
	public SpecialEntityActionType(@Nullable String animFileName, ResourceLocation id, 
			Function<EntityActionType, ? extends EntityActionInstance> createActionObj) {
		this.id = id;
		if (animFileName != null) {
			this.animSet = id.withPath(animFileName);
			this.anim = ActionAnimIdentifier.getOrCreate(id.getPath());
		}
		else {
			this.animSet = null;
			this.anim = null;
		}
		this.abilityId = new AbilityId(null, JojoMod.resLoc("special"), id.toString());
		this.createActionObj = createActionObj;
	}
	
	@Override
	public AbilityId getAbilityId() {
		return abilityId;
	}
	
	@Override
	public AbilityUsageGroup getAbilityUsageCategory() {
		return AbilityUsageGroup.SPECIAL;
	}


	@Override
	public void encodeAbility(LivingEntity user, FriendlyByteBuf buffer) {
		buffer.writeBoolean(false);
		buffer.writeResourceLocation(id);
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}
	
	@Override
	public ResourceLocation getEntityAnimSet(LivingEntity user) {
		return animSet;
	}


	@Override
	public EntityActionInstance createActionObj() {
		return createActionObj.apply(this);
	}

}
