package com.github.standobyte.jojo.mixin.container.open_as_non_player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_opencontainer.ContainerUtil;
import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer.ContainerOpenedAsNonPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

// this shit so ass
@Mixin(BaseContainerBlockEntity.class)
public abstract class ValidCheckChestInventory extends BlockEntity implements Container {

	public ValidCheckChestInventory(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
	private void checkActualEntityRangeInstead(Player player, CallbackInfoReturnable<Boolean> ci) {
		if (player.containerMenu != null) {
			boolean thisIsTheOpenedContainer = ContainerUtil.isSameContainer(this, ContainerUtil.possiblyGetContainerInventory(player.containerMenu));
			if (thisIsTheOpenedContainer) {
				Entity actualEntity = ((ContainerOpenedAsNonPlayer) player.containerMenu).jojo_ripples$getActualEntity();
				if (actualEntity != null) {
					boolean entityIsInRange;
					Level level = this.getLevel();
					BlockPos blockPos = this.getBlockPos();
					if (level == null || level.getBlockEntity(blockPos) != this) {
						entityIsInRange = false;
					}
					else {
						double range = Attributes.BLOCK_INTERACTION_RANGE.value().getDefaultValue() + 4;
						entityIsInRange = new AABB(blockPos).distanceToSqr(actualEntity.getEyePosition()) < range * range;
					}
					ci.setReturnValue(entityIsInRange);
				}
			}
		}
	}
	
}
