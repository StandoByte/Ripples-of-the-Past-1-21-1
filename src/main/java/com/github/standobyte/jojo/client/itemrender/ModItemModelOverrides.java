package com.github.standobyte.jojo.client.itemrender;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.client.renderer.item.ItemProperties;

public class ModItemModelOverrides {

	public static void register() {
//		CustomIconItem.registerModelOverride();
//		ItemProperties.register(ModItems.KNIFE.get(), JojoMod.resLoc("count"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null ? itemStack.getCount() : 1;
//		});
//		ItemProperties.register(ModItems.STONE_MASK.get(), JojoMod.resLoc("stone_mask_activated"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return itemStack.getTag().getByte(StoneMaskItem.NBT_ACTIVATION_KEY) > 0 ? 1 : 0;
//		});
//		ItemProperties.register(ModItems.TOMMY_GUN.get(), JojoMod.resLoc("swing"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && livingEntity.swinging && livingEntity.getItemInHand(livingEntity.swingingArm) == itemStack ? 1 : 0;
//		});
		ItemProperties.register(ModItems.STAND_ARROW_SHARD.get(), JojoMod.resLoc("shard_variant"), (itemStack, clientWorld, livingEntity, seed) -> {
			Integer variant = itemStack.get(ModItemDataComponents.ARROW_SHARD_VARIANT);
			return variant != null ? variant.floatValue() : 0;
		});
//		ItemProperties.register(Items.BOW, JojoMod.resLoc("stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack
//					&& livingEntity.getProjectile(itemStack).getItem() instanceof StandArrowItem ? 1 : 0;
//		});
//		ItemProperties.register(Items.CROSSBOW, JojoMod.resLoc("stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && CrossbowItem.isCharged(itemStack) && (
//					CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW.get()) || 
//					CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW_BEETLE.get())) ? 1 : 0;
//		});
//		ItemProperties.register(ModItems.STAND_DISC.get(), JojoMod.resLoc("stand_id"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return StandDiscItem.validStandDisc(itemStack, true) ? JojoCustomRegistries.STANDS.getNumericId(StandDiscItem.getStandFromStack(itemStack).getType().getRegistryName()) : -1;
//		});
//		ItemProperties.register(ModItems.CASSETTE_RECORDED.get(), JojoMod.resLoc("cassette_distortion"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return CassetteRecordedItem.getCassetteData(itemStack)
//					.map(cap -> MathHelper.clamp(cap.getGeneration(), 0, CassetteCap.MAX_GENERATION))
//					.orElse(0).floatValue();
//		});
//		ItemProperties.register(ModItems.POLAROID.get(), JojoMod.resLoc("is_held"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && (livingEntity.getItemInHand(Hand.MAIN_HAND) == itemStack || livingEntity.getItemInHand(Hand.OFF_HAND) == itemStack) ? 1 : 0;
//		});
	}
}
