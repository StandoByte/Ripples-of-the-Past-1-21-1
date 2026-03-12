package com.github.standobyte.jojo.mc.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MeteoriteCoreBlock extends Block {

	public MeteoriteCoreBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, 
			@Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
		return 30;
	}

//	@Override
//	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
//		double x = pos.getX();
//		double y = pos.getY();
//		double z = pos.getZ();
//		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, (new AABB(x, y, z, x, y, z)).inflate(2))) {
//			if (entity.getMobType() != CreatureAttribute.UNDEAD && entity.getHealth() < entity.getMaxHealth() && !isImmuneToMeteoriteStrain(entity)) {
//				entity.hurt(DamageUtil.STAND_VIRUS_METEORITE, 4.0F);
//			}
//		}
//		level.getBlockTicks().scheduleTick(pos, this, 10);
//	}
//
//	@Override
//	public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
//		double d0 = (double)((float)pos.getX() + random.nextFloat() * 4F - 2F);
//		double d1 = (double)((float)pos.getY() + random.nextFloat() * 4F - 2F);
//		double d2 = (double)((float)pos.getZ() + random.nextFloat() * 4F - 2F);
//		level.addParticle(ModParticles.METEORITE_VIRUS.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
//	}
//
//	@Deprecated
//	@Override
//	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, Level level, BlockPos currentPos, BlockPos facingPos) {
//		level.getBlockTicks().scheduleTick(currentPos, this, 10);
//		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
//	}
//
//	@Override
//	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
//		level.getBlockTicks().scheduleTick(pos, this, 10);
//	}
//
//	@Override
//	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tileEntity, ItemStack stack) {
//		super.playerDestroy(level, player, pos, state, tileEntity, stack);
//		if (player.getHealth() < player.getMaxHealth() && !isImmuneToMeteoriteStrain(player)) {
//			player.hurt(DamageUtil.STAND_VIRUS_METEORITE, 10.0F);
//		}
//	}

	public static boolean isImmuneToMeteoriteStrain(LivingEntity entity) {
		StandPower stand = StandPower.get(entity);
		return stand != null && (stand.hasPower() || stand.userStandAwakeningState.hadStandBefore);
	}
}
