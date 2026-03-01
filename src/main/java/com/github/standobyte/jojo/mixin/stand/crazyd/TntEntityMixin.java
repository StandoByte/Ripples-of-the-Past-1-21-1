package com.github.standobyte.jojo.mixin.stand.crazyd;

import java.util.Collections;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mixin.EntityMixinSuperclass;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.PrevBlockInfo;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.TntImDynamite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PrimedTnt.class)
public abstract class TntEntityMixin extends EntityMixinSuperclass {
	@Unique private BlockPos originBlockPos;

	@Override
	public void jojo_ripples$onAddedToWorld(CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide()) {
			if (originBlockPos == null) {
				originBlockPos = BlockPos.containing(this.position());
			}
			PrimedTnt thisTntEntity = (PrimedTnt) (Object) this;
			BlockState tntBlockState = thisTntEntity.getBlockState();

			PrevBlockInfo litBlockData = CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, originBlockPos, tntBlockState, 
					Optional.empty(), Collections.emptyList());
			if (litBlockData != null) {
				litBlockData.withEntities(new TntImDynamite(thisTntEntity));
			}
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	@Unique private void saveExtraNbt(CompoundTag nbt, CallbackInfo ci) {
		if (originBlockPos != null) {
			nbt.put("jojo_ripples:origin_pos", NbtUtils.writeBlockPos(originBlockPos));
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	@Unique private void loadExtraNbt(CompoundTag nbt, CallbackInfo ci) {
		NbtUtils.readBlockPos(nbt, "jojo_ripples:origin_pos").ifPresent(pos -> this.originBlockPos = pos);
	}

}
