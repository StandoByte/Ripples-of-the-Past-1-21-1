package com.github.standobyte.jojo.mixin.stand.theworld.timestop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

	public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
		super(pLevel, pPos, pYRot, pGameProfile);
	}

	@Inject(method = "doTick", at = @At("HEAD"), cancellable = true)
	public void jojoTsCancelPlayerTick(CallbackInfo ci) {
		if (TimeStopEffect.getIsFrozenInTime(this)) {
			ci.cancel();
		}
	}

}
