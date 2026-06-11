package com.github.standobyte.jojo.mechanics.standarrow;

import static com.github.standobyte.jojo.init.ModItems.discsOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.network.s2c.ItemBreakVisualsPacket;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.DamageUtil;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.jojo.util.functions.StatusEffectUtil;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.github.standobyte.jojo.util.objects_java.LazyNullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandArrowItem extends ArrowItem {
    // dur: 25 | 250; ench: 10 | 25
    private final int enchantability = 20;

    public StandArrowItem(Properties properties) {
        super(properties);
        DispenserBlock.registerProjectileBehavior(this);
    }

    // TODO bow/crossbow model override when shooting a stand arrow
    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new StandArrowEntity(shooter, level, ammo, weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack arrowItem, Direction dispenserDir) {
    	StandArrowEntity arrow = new StandArrowEntity(level, pos.x(), pos.y(), pos.z(), arrowItem.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
    
    
    public static boolean giveStand(Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
        	boolean givePowerTypeStand = entity.getType() == EntityType.PLAYER;
        	
        	// TODO event
        	if (givePowerTypeStand) {
        		StandPower stand = StandPower.get(entity);
        		if (!stand.hasPower()) {
        			StandType standToGive = pickStandToGive(entity);
        			if (standToGive != null) {
        				stand.setStand(standToGive);
        				stand.healingDamageFromArrow = true;
        				return true;
        			}
        		}
        	}
        }
        return false;
    }

    public static Stream<StandType> getStandsForPlayer() {
    	return StandType.getAllEnabledStands().filter(ModStands.PLAYER_CAN_GET_FROM_ARROW::contains);
    }
    
    @Nullable
    public static StandType pickStandToGive(LivingEntity entity) {
    	// TODO use StandAwakening#fatedFutureStands
    	// TODO use a ServerDuplicateCounter
    	List<StandType> stands = StandArrowItem.getStandsForPlayer().toList();
    	if (!stands.isEmpty()) {
    		return stands.get(entity.getRandom().nextInt(stands.size()));
    	}
    	return null;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack arrowItem = player.getItemInHand(usedHand);

        if (!level.isClientSide() && !StandUtil.isEntityStandUser(player)) {
        	boolean gaveStand = StandArrowItem.giveStand(level, player);
        	if (!StandArrowItem.isInvulnerable(player)) {
        		StandArrowItem.dealDamageFromArrow(player, arrowItem, 
        				player, player, false, gaveStand);
        	}
        	if (gaveStand) {
        		ServerLevel serverLevel = (ServerLevel) level;
        		ItemStack arrowSaved = arrowItem.copy();
        		arrowItem.hurtAndBreak(1, serverLevel, player, itemType -> {
        			StandArrowItem.onBreakArrow(serverLevel, player, usedHand, null, itemType, arrowSaved);
        		});
        		if (!arrowItem.isEmpty()) {
        			StandArrowLore.onStandGiven(arrowItem);
        		}
        		return InteractionResultHolder.success(arrowItem);
        	}
        }
        return InteractionResultHolder.fail(arrowItem);
    }
    
    public static void onBreakArrow(ServerLevel level, 
    		@Nullable LivingEntity userEntity, @Nullable InteractionHand usedHand,
    		@Nullable Vec3 pos, 
    		Item item, ItemStack arrowItemStack) {
    	if (pos == null && userEntity != null) {
    		pos = userEntity.getEyePosition().add(new Vec3(0, 0, 0.6)
    				.xRot(-userEntity.getXRot() * MathUtil.DEG_TO_RAD)
    				.yRot(-userEntity.getYRot() * MathUtil.DEG_TO_RAD));
    	}

    	// broken item sound and particles

    	if (userEntity != null && usedHand != null) {
    		userEntity.onEquippedItemBroken(item, UtilFunctions.getHandSlot(usedHand));
    	}
    	else if (pos != null) {
			PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos(pos), new ItemBreakVisualsPacket(pos, item));
    	}

    	// spawn arrow shard items

    	if (pos != null) {
    		StandArrowShardLore lore = StandArrowShardLore.empty()
    				.withArrowItemName(Optional.of(arrowItemStack.getHoverName()));
    		if (userEntity != null) {
    			lore = lore.withUserCharacterName(Optional.of(userEntity.getName()));
    		}
    		for (int i = 0; i < 3; i++) {
    			ItemStack shardItem = ModItems.STAND_ARROW_SHARD.toStack();
    			shardItem.set(ModItemDataComponents.ARROW_SHARD_VARIANT, i);
    			shardItem.set(ModItemDataComponents.ARROW_SHARD_LORE, lore);
    			ItemEntity shardItemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, shardItem);
    			level.addFreshEntity(shardItemEntity);
    			shardItemEntity.setPickUpDelay(40);
    		}
    	}
    }
    
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
    	super.onCraftedBy(stack, level, player);
    	if (!level.isClientSide()) {
    		StandArrowLore.onArrowCrafted(stack, player);
    	}
    }
    
    public static ChunkPos chunkPos(Vec3 pos) {
    	return new ChunkPos(((int) pos.x) >> 4, ((int) pos.z) >> 4);
    }

    // i'm tired of being angry
    public static boolean isInvulnerable(LivingEntity entity) {
    	return entity.isInvulnerable() || entity instanceof Player player && player.getAbilities().invulnerable;
    }
    
    public static void dealDamageFromArrow(LivingEntity entity, ItemStack arrowItem, 
    		Entity directEntity, Entity responsibleEntity, 
    		boolean reducedDamage, boolean gaveStand) {
    	int bleedingEffect = reducedDamage ? 1 : 2;
    	float dmgAmount = reducedDamage ? 12 : 16;

    	entity.addEffect(new MobEffectInstance(ModStatusEffects.BLEEDING, 
    			6000 /* it'll heal anyway */, bleedingEffect, false, false, true));
    	DamageSource dmgSource = DamageUtil.make(entity.level(), ModDamageTypes.STAND_ARROW, directEntity, responsibleEntity);
    	if (gaveStand) {
    		dmgAmount = Math.min(dmgAmount, entity.getHealth() - 1.0F);
    	}
    	entity.hurt(dmgSource, dmgAmount);
    }
    
    public static boolean healArrowDamage(LivingEntity entity) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(0.1F);
		}
		
		MobEffectInstance bleeding = entity.getEffect(ModStatusEffects.BLEEDING);
		if (bleeding != null) {
			if (entity.tickCount % 40 == 39) {
				StatusEffectUtil.reduceEffect(entity, ModStatusEffects.BLEEDING, 0, 1);
			}
		}
		
		return entity.getHealth() < entity.getMaxHealth() || bleeding != null;
    }


    protected LazyNullable<String> lorePrefix = LazyNullable.of(() -> {
    	if (this == ModItems.STAND_ARROW.get() || this == ModItems.STAND_ARROW_BEETLE.get()) {
    		return "jojo_ripples.stand_arrow.lore.ancient";
    	}
    	if (this == ModItems.STAND_ARROW_METEORITE.get()) {
    		return "jojo_ripples.stand_arrow.lore.meteorite";
    	}
    	return null;
    });
    static List<Object> tlArgs = new ArrayList<>(2);
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(CommonComponents.EMPTY);
        
        String lorePrefix = this.lorePrefix.get();
        if (lorePrefix != null) {
        	@Nullable StandArrowLore lore = stack.get(ModItemDataComponents.ARROW_LORE);
        	Component firstCharacterName = lore != null ? lore.firstCharacterName().orElse(null) : null;
        	ResourceKey<Structure> foundAtStructure = lore != null ? lore.foundAtStructure().orElse(null) : null;
        	MutableComponent loreLine;
        	
        	tlArgs.clear();
        	String tlKey = lorePrefix;
        	if (firstCharacterName != null) {
        		tlKey += ".name";
        		tlArgs.add(firstCharacterName);
        	}
        	if (foundAtStructure != null) {
        		Component structureName = clGetStructureNameIfTranslated(foundAtStructure.location());
        		if (structureName != null) {
        			tlKey += ".structure";
        			tlArgs.add(structureName);
        		}
        	}
        	loreLine = Component.translatable(tlKey, tlArgs.toArray());
    		tooltipComponents.add(loreLine.withStyle(ChatFormatting.GRAY));
        	
        	if (lore != null) {
        		if (lore.awakenedAStand()) {
            		tooltipComponents.add(Component.translatable(lorePrefix + ".used").withStyle(ChatFormatting.GRAY));
        		}
        	}
        }
    }
    
    public static void addStandNamesToTooltip(List<Component> tooltipComponents, TooltipContext context) {
        Player player = ClientProxy.getClientPlayer();
        if (player != null) {
            Stream<StandType> stands = StandArrowItem.getStandsForPlayer();
            stands.map(StandInstance::new)
            .sorted(discsOrder(context.registries()))
            .forEach(stand -> {
            	StandSkin defaultSkin = StandSkinsLoader.getInstance().getSkin(stand);
            	Component standName = stand.getStandName().plainCopy().withStyle(ChatFormatting.GRAY);
            	if (defaultSkin != null) {
            		Holder<StoryPart> storyPart = defaultSkin.getStoryPart(context.registries());
            		if (storyPart != null) {
            			Component partIcon = storyPart.value().getPartIconAsText();
            			standName = partIcon.copy().append(standName);
            		}
            	}
                tooltipComponents.add(standName);
            });
        }
    }
	
    static Map<ResourceLocation, Optional<Component>> STRUCTURE_NAMES_CACHE = new HashMap<>();
	@Nullable
	public static Component clGetStructureNameIfTranslated(ResourceLocation id) {
		Optional<Component> name = STRUCTURE_NAMES_CACHE.computeIfAbsent(id, _id -> {
			String tlKey = "prepositional.structure." + id.getNamespace() + "." + id.getPath();
			if (Language.getInstance().has(tlKey)) {
				return Optional.of(Component.translatable(tlKey));
			}
			return Optional.empty();
		});
		return name.orElse(null);
	}

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantability;
    }
    
    // this shit is impossible with purely data-driven enchantments
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    	return enchantment.is(Enchantments.LOYALTY) || super.supportsEnchantment(stack, enchantment);
    }
}
