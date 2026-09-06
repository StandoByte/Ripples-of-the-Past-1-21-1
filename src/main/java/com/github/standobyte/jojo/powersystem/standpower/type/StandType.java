package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.SimpleTagKey;
import com.github.standobyte.jojo.network.s2c.StandSkinSoundPacket;
import com.github.standobyte.jojo.network.s2c.TrNonEntityStandSummonPacket;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandAwakening.AwakeningStage;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.powersystem.standpower.datapack.StandTypeClass;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand.BlankSummonedStand;
import com.github.standobyte.jojo.util.functions.AttributeUtil;
import com.github.standobyte.jojo.util.functions.JojoModUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.language.IConfigurable;

public class StandType extends PowerType {
	static {
		SimpleTagKey.SimpleTagType.registerType(StandType.class, StandType::getId, "stands");
	}

	protected final ResourceLocation standTypeId;
	protected StandStats stats;
	protected boolean isEnabled;
	@Deprecated(forRemoval = true) public boolean hasSummonMechanic = true;
	// If this is set to null, the Stand type will have no summon/unsummon mechanic.
	@Nullable public Supplier<SummonedStand> makeSummonedStandObj = BlankSummonedStand::new;
	protected boolean playSummonSound = true;
	protected boolean playUnsummonSound = true;
	
	public StandCreationSource createdIn = StandCreationSource.REGISTRY;
	public List<Component> discExtraTooltip = new ArrayList<>();
	/** The lesser this field is, the earlier the disc will appear in the creative tab. Default is 100. */
	public int discCategoryPriority = 100;
	public boolean translucentDisc = false;
	public int discStoryPartPriority = 100;
	public String skinUIType;
	
	public StandType(StandStats stats, MovesetBuilder moveset, 
			ResourceLocation id) {
		super(id, moveset);
		this.standTypeId = id;
		if (stats == null) stats = new StandStats(0, 0, 0, 0, 0, 0);
		this.stats = stats;
		this.isEnabled = true;
		addAddonCredits();
	}
	
	public <T extends StandType> T init(Consumer<T> init) {
		@SuppressWarnings("unchecked")
		T cast = (T) this;
		init.accept(cast);
		return cast;
	}
	
	public <T extends StandType> T discTooltipWIP() { return discTooltipWIP(false); }
	
	public <T extends StandType> T discTooltipWIP(boolean translucentDisc) { 
		return init(stand -> {
			stand.discExtraTooltip.add(
					Component.translatable("item.jojo_ripples.stand_disc.wip")
					.withStyle(ChatFormatting.ITALIC).withColor(0x808000));
			stand.discCategoryPriority = 200;
			stand.translucentDisc = translucentDisc;
		});
	}
	
	public <T extends StandType> T discTooltipOld() { 
		return init(stand -> {
			stand.discExtraTooltip.add(
					Component.translatable("item.jojo_ripples.stand_disc.old")
					.withStyle(ChatFormatting.ITALIC).withColor(0x808000));
			stand.discCategoryPriority = 201;
			stand.translucentDisc = true;
		});
	}
	
	public <T extends StandType> T discTooltipExperimental() { 
		return init(stand -> {
			stand.discExtraTooltip.add(
					Component.translatable("item.jojo_ripples.stand_disc.experimental")
					.withStyle(ChatFormatting.ITALIC).withColor(0x800000));
			stand.discCategoryPriority = 300;
			stand.translucentDisc = true;
		});
	}
	
	public void discTooltipDatapack() { 
		this.createdIn = StandCreationSource.DATAPACK;
		this.discExtraTooltip.add(
				Component.translatable("item.jojo_ripples.stand_disc.data_pack")
				.withStyle(ChatFormatting.ITALIC).withColor(0x6060ff));
		this.discCategoryPriority = 400;
	}

	public void addAddonCredits() {
		if (createdIn == StandCreationSource.REGISTRY) {
			String modId = standTypeId.getNamespace();
			if (!modId.equals(JojoMod.MOD_ID)) {
				ModList.get().getModContainerById(modId)
				.map(mod -> mod.getModInfo())
				.flatMap(modInfo -> modInfo instanceof IConfigurable ? ((IConfigurable) modInfo).getConfigElement("authors") : Optional.empty())
				.map(authorsString -> authorsString instanceof String ? (String) authorsString : null)
				.ifPresent(authors -> {
					authors = authors.replace(", StandoByte", "").replace("StandoByte, ", "");
					discExtraTooltip.add(
							Component.translatable("item.jojo_ripples.stand_disc.addon_author", authors)
							.withStyle(ChatFormatting.ITALIC).withColor(0x00b0b0));
				});
			}
		}
	}
	
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = super.makeConfigTemplate();
		StandTypeClass.addClassToJson(json, this.getClass());
		json.add("stats", stats.makeConfigTemplate());
		return json;
	}
	
	@Override
	public void applyConfig(JsonElement json) {
		super.applyConfig(json);
		JsonObject config = json.getAsJsonObject();
		Optional.ofNullable(config.get("stats")).ifPresent(stats::applyConfig);
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		stats.restoreDefaults();
	}
	
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	
	public StandStats getStandStats() {
		return stats;
	}
	
	
	@Nonnull
	@Override
	public StandTypePersistentData newDataInstance() {
		return new StandTypePersistentData(this);
	}
	
	
	public void onUserSummonCommand(LivingEntity user, StandPower standPower) {
		if (user.level().isClientSide()) return;
		
		if (!standPower.isSummoned() && onTrySummon(user, standPower)) {
			summon(user, standPower);
		}
		else {
			unsummon(user, standPower);
		}
	}
	
	public boolean summon(LivingEntity user, StandPower standPower) {
		if (!standPower.isSummoned() && standPower.canUsePower() && hasSummonMechanic(standPower)) {
			SummonedStand summonedStand = makeSummonedStand(standPower);
			if (summonedStand == null) return false;
			
			standPower.setSummonedStand(summonedStand);
			if (user != null && !user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, 
						new TrNonEntityStandSummonPacket(user.getId(), true, summonedStand));
				if (playSummonSound) {
					StandSkinSoundPacket soundPacket = StandSkinSoundPacket.play(
							user.position(), ModSoundEvents.STAND_SUMMON, 
							standPower, user.getSoundSource(), 1, 1);
					if (soundPacket != null) {
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, soundPacket);
					}
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean onTrySummon(LivingEntity user, StandPower standPower) {
		AwakeningStage awakeningStage = standPower.userStandAwakeningState.stage;
		return switch (awakeningStage) {
			case FULL_CONTROL -> true;
			case PARTIALLY_AWAKENED -> {
				JojoModUtil.sendOverlayMsg(user, Component.translatable("stand_summon.not_in_full_control"));
				yield false;
			}
			case AWAKENING_PASSIVE -> {
				JojoModUtil.sendOverlayMsg(user, Component.translatable("stand_summon.dormant"));
				yield false;
			}
		};
	}
	
	/**
	 * If this is overriden to return null, the Stand type will have no summon/unsummon mechanic.
	 */
	protected SummonedStand makeSummonedStand(StandPower standPower) {
		return makeSummonedStandObj != null ? makeSummonedStandObj.get() : null;
	}
	
	public boolean hasSummonMechanic(StandPower standPower) {
		return makeSummonedStandObj != null;
	}
	
	public void unsummon(LivingEntity user, StandPower standPower) {
		if (!user.level().isClientSide()) {
			SummonedStand standEntity = standPower.getSummonedStand();
			if (standEntity != null) {
				playUnsummonSound(user, standPower);
				if (standEntity.unsummonCommand()) {
					forceUnsummon(user, standPower);
				}
			}
		}
	}
	
	protected void playUnsummonSound(LivingEntity user, StandPower standPower) {
		if (playUnsummonSound) {
			StandSkinSoundPacket soundPacket = StandSkinSoundPacket.play(
					user.position(), ModSoundEvents.STAND_UNSUMMON, 
					standPower, user.getSoundSource(), 1, 1);
			if (soundPacket != null) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, soundPacket);
			}
		}
	}
	
	public void forceUnsummon(LivingEntity user, StandPower standPower) {
		if (standPower.isSummoned()) {
			standPower.setSummonedStand(null);
			if (user != null && !user.level().isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, 
						new TrNonEntityStandSummonPacket(user.getId(), false, null));
			}
		}
	}
	
	public boolean showHUD(StandPower standPower) {
		SummonedStand summonedStand = standPower.getSummonedStand();
		return summonedStand != null && !(summonedStand instanceof SummonedStand.SyncableSummonedStand stand && stand.isBeingUnsummoned());
	}
	
	
	public boolean usesStamina(StandPower standPower) {
		return true;
	}
	
	public float getMaxStamina(StandPower standPower) {
		return getBaseMaxStamina(standPower) * getStaminaMultiplier(standPower);
	}
	
	protected float getBaseMaxStamina(StandPower standPower) {
		return 1000;
	}
	
	public float getStaminaRegen(StandPower standPower) {
		return getBaseStaminaRegen(standPower) * getStaminaMultiplier(standPower);
	}
	
	protected float getBaseStaminaRegen(StandPower standPower) {
		if (standPower.isSummoned()) {
			LivingEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				EntityActionInstance action = LivingComponentAction.getCurEntityAction(standEntity);
				if (action != null && !action.standRegensStamina) {
					return 0;
				}
			}
			return 1.5f;
		}
		return 3;
	}
	
	protected static float getStaminaMultiplier(StandPower standPower) {
		double durability = AttributeUtil.getValueOrDefault(standPower.getUser(), ModEntityAttributes.STAND_DURABILITY);
		return StandStatFormulas.getStaminaMultiplier(durability);
	}
	
	
	public boolean usesResolve(StandPower standPower) {
		return true;
	}
	
	
	@Override
	public Map<String, StandUnlockableSkill> getUnlockableSkills() {
		return (Map<String, StandUnlockableSkill>) super.getUnlockableSkills();
	}
	
	
	@Override
	public ResourceLocation getId() {
		return standTypeId;
	}
	
	@Nullable
	public static StandType fromId(ResourceLocation id) {
		StandType stand = DataDrivenStandsLoader.getDatapackStand(id);
		if (stand == null) {
			stand = JojoRegistries.DEFAULT_STANDS_REG.get(id);
		}
		if (stand != null && !stand.isEnabled()) {
			stand = null;
		}
		return stand;
	}
	
	public static Stream<StandType> getAllEnabledStands() {
		return Stream.concat(
				JojoRegistries.DEFAULT_STANDS_REG.entrySet().stream().map(Map.Entry::getValue).filter(StandType::isEnabled), 
				DataDrivenStandsLoader.getAllDatapackStands());
	}
	
//	public static final StreamCodec<ByteBuf, StandType> SYNC_VIA_ID = 
//			ResourceLocation.STREAM_CODEC.map(StandType::fromId, StandType::getId);
//	
	
	
	@Override
	public PowerClass<StandPower> getPowerClass() {
		return PowerClass.STAND;
	}
	
	
	public static String makeTlKey(ResourceLocation standId) {
		return Util.makeDescriptionId("stand", standId);
	}
	
	public Lazy<Component> name = Lazy.of(() -> Component.translatable(makeTlKey(this.getId())));
	@Override
	public Component getName(Power<?> playerPowerData) {
		return ((StandPower) playerPowerData).getStandInstance()
				.map(stand -> stand.getStandName())
				.orElseGet(this.name::get);
	}
	
	
	@Deprecated
	public StandSkinsScreen.SkinView makeSkinUIElement(StandSkin skin, StandSkinsScreen screen, int x, int y, int standY, int row, int column, boolean isBottomRow) {
		return null;
	}
	
}
