package com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.stand;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.ElementTransparency;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.mechanics.externalcontainer._stand.input.ClientStandItemInputs;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientStandController extends ClientEntityController {
	
	public ClientStandController(LivingEntity entity) {
		super(entity);
	}


	@Override
	public void onSet() {
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void onUnset() {
		NeoForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInputUpdate(MovementInputUpdateEvent event) {
		Input input = event.getInput();
		moveStandManually(entityAsLiving, input.leftImpulse, input.forwardImpulse, 
//				input.keyPresses.jump(), input.keyPresses.shift());
				input.jumping, input.shiftKeyDown);
		// FIXME (1.16) (stand manual control) do not reset deltaMovement in manual control
		PacketDistributor.sendToServer(new ClStandManualMovementPacket(
				entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), prevTickInput));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearInput(MovementInputUpdateEvent event) {
		Input input = event.getInput();
		clearInput(input);
	}
	
	@Override
	public void tickPre() {
		super.tickPre();
		ClientStandItemInputs.handleInManualControl(mc);
	}


	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		manualMovementSpeed = Mth.clamp(manualMovementSpeed + 0.025f * (float) event.getScrollDeltaY(), 0, 1);
		movementSpeedBarTranslucency.reset();
		event.setCanceled(true);
	}

	static float manualMovementSpeed = 1;
	private boolean prevTickInput = false;
	public void moveStandManually(LivingEntity standEntity, float strafe, float forward, boolean jumping, boolean sneaking) {
		boolean canStandMoveManually = true;
		Vec3 motion = Vec3.ZERO;
		if (canStandMoveManually) {
			boolean input = jumping || sneaking || forward != 0 || strafe != 0;
			if (input) {
				double speed = standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
				double y = jumping ? speed : 0;
				if (sneaking) {
					y -= speed;
					strafe *= 0.5;
					forward *= 0.5;
				}
				if (!prevTickInput) {
//					standEntity.setDeltaMovement(Vec3.ZERO);
				}
				else {
					float actionWalkSpeed = 1;
					EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(standEntity);
					if (curAction != null) {
						actionWalkSpeed = curAction.userWalkSpeed;
					}
					motion = getAbsoluteMotion(new Vec3((double)strafe, y, (double)forward), speed, standEntity.getYRot())
							.scale(actionWalkSpeed * manualMovementSpeed);
//					standEntity.setDeltaMovement(motion);
				}
			}
			else if (prevTickInput) {
//				standEntity.setDeltaMovement(Vec3.ZERO);
			}
			prevTickInput = input;
		}
		StandEntity stand = (StandEntity) standEntity;
		stand.manualControlInput(motion);
	}

	private static Vec3 getAbsoluteMotion(Vec3 relative, double speed, float facingYRot) {
		double d0 = relative.lengthSqr();
		if (d0 < 1.0E-7D) {
			return Vec3.ZERO;
		} else {
			Vec3 vec3d = relative.normalize().scale(speed);
			float yRotSin = Mth.sin(facingYRot * ((float)Math.PI / 180F));
			float yRotCos = Mth.cos(facingYRot * ((float)Math.PI / 180F));
			return new Vec3(vec3d.x * (double)yRotCos - vec3d.z * (double)yRotSin, vec3d.y, vec3d.z * (double)yRotCos + vec3d.x * (double)yRotSin);
		}
	}


	@Override
	public boolean shouldRenderBlockOutline() {
		if (!mc.player.mayBuild()) { // either adventure mode or spectator
			ItemStack heldItem = ((LivingEntity) entity).getMainHandItem();
			HitResult vanillaAim = mc.hitResult;
			if (vanillaAim != null && vanillaAim.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult) vanillaAim).getBlockPos();
				BlockState blockState = mc.level.getBlockState(blockPos);
				if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
					return blockState.getMenuProvider(mc.level, blockPos) != null;
				} else {
					BlockInWorld blockInWorld = new BlockInWorld(mc.level, blockPos, false);
					return !heldItem.isEmpty() && (heldItem.canBreakBlockInAdventureMode(blockInWorld) || heldItem.canPlaceOnBlockInAdventureMode(blockInWorld));
				}
			}
		}

		return true;
	}

	public static ElementTransparency movementSpeedBarTranslucency = new ElementTransparency();
	public static final ResourceLocation SPEED_BAR_EMPTY = JojoMod.resLoc("textures/hud/stand_movement_speed_0.png");
	public static final ResourceLocation SPEED_BAR_FULL = JojoMod.resLoc("textures/hud/stand_movement_speed_1.png");
	
	@Override
	public void renderExtraHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (movementSpeedBarTranslucency.shouldRender()) {
			float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
			float alpha = movementSpeedBarTranslucency.getAlpha(partialTick);
			int color = ARGB32.colorFromFloat(alpha, 1, 1, 1);
			int x = guiGraphics.guiWidth() / 2 + 4;
			int y = guiGraphics.guiHeight() / 2 - 8;
			float SPRITE_WIDTH = 16;
			float SPRITE_HEIGHT = 16;

			RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
					GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
					GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			BlitFloat.blit(guiGraphics.pose(), mc, SPEED_BAR_EMPTY, 
					x, y, SPRITE_WIDTH, SPRITE_HEIGHT, 0, 
					BlitFloat.NO_TINT);
            RenderSystem.defaultBlendFunc();

			float speed = ClientStandController.manualMovementSpeed;
			if (speed > 1E-4) {
				float height = speed == 1 ? 1 : Math.min(speed, 1f - 1f / (16 * mc.options.guiScale().get()));
				float fillY = SPRITE_HEIGHT * (1 - height);
				BlitFloat.blit(guiGraphics.pose(), mc, SPEED_BAR_FULL, 
						x, y + fillY, SPRITE_WIDTH, SPRITE_HEIGHT - fillY, 0, 
						0, fillY,     SPRITE_WIDTH, SPRITE_HEIGHT - fillY, SPRITE_WIDTH, SPRITE_HEIGHT, 
						color);
			}
		}
	}

}
