package com.github.standobyte.jojo.mixin.stand.crazyd;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

@Mixin(LootTable.class)
public class LootTableMixin {

	@Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
	public void jojo_ripples$rememberBlockLoot(LootContext context, CallbackInfoReturnable<List<ItemStack>> ci) {
		boolean wasBlockBroken = LootContextParamSets.BLOCK.getRequired().stream().allMatch(context::hasParam);
		if (wasBlockBroken) {
			Level level = context.getLevel();
			if (level != null) {
				BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
				Optional<BlockEntity> tileEntity = Optional.ofNullable(context.getParamOrNull(LootContextParams.BLOCK_ENTITY));
				Vec3 posCenter = context.getParamOrNull(LootContextParams.ORIGIN);
				BlockPos blockPos = BlockPos.containing(posCenter);
				boolean blockLootGamerule = level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
				List<ItemStack> loot = blockLootGamerule ? ci.getReturnValue() : Collections.emptyList();
				CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, blockState, tileEntity, loot);
			}
		}
	}
}
