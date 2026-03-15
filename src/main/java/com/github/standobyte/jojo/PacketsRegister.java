package com.github.standobyte.jojo;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.explosion.CustomExplosionPacket;
import com.github.standobyte.jojo.entityattachment.custom_effect.TrEntityCustomEffectsPacket;
import com.github.standobyte.jojo.entityattachment.custom_effect.sync.TrStandEffectSynchedDataPacket;
import com.github.standobyte.jojo.mechanics.clothes.TrClothesItemsPacket;
import com.github.standobyte.jojo.mechanics.clothes.sewing.ClSetSewingMachineItemPacket;
import com.github.standobyte.jojo.mechanics.resolve.ResolveBoostsPacket;
import com.github.standobyte.jojo.mechanics.resolve.TrResolvePacket;
import com.github.standobyte.jojo.network.c2s.ClAbilityInputPacket;
import com.github.standobyte.jojo.network.c2s.ClAimTargetPacket;
import com.github.standobyte.jojo.network.c2s.ClDebugCommandPacket;
import com.github.standobyte.jojo.network.c2s.ClNoParamsPacket;
import com.github.standobyte.jojo.network.c2s.ClSetStandSkinPacket;
import com.github.standobyte.jojo.network.s2c.BloodParticlesPacket;
import com.github.standobyte.jojo.network.s2c.BrokenBlocksParticlesAndSoundsPacket;
import com.github.standobyte.jojo.network.s2c.DatapackStandsPacket;
import com.github.standobyte.jojo.network.s2c.DeflectedBulletPacket;
import com.github.standobyte.jojo.network.s2c.EntitySyncMotionBypassingPacket;
import com.github.standobyte.jojo.network.s2c.ItemBreakVisualsPacket;
import com.github.standobyte.jojo.network.s2c.StandEntitySoundPacket;
import com.github.standobyte.jojo.network.s2c.StandSkinSoundPacket;
import com.github.standobyte.jojo.network.s2c.TrAbilityUsePacket;
import com.github.standobyte.jojo.network.s2c.TrAimTargetPacket;
import com.github.standobyte.jojo.network.s2c.TrNonEntityStandSummonPacket;
import com.github.standobyte.jojo.network.s2c.TrPowerDataPacket;
import com.github.standobyte.jojo.network.s2c.TrPowerStandInstancePacket;
import com.github.standobyte.jojo.network.s2c.TrPowerTypePacket;
import com.github.standobyte.jojo.network.s2c.TrResetDeathTimePacket;
import com.github.standobyte.jojo.network.s2c.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.s2c.TrStandSkinPacket;
import com.github.standobyte.jojo.network.s2c.TrSyncStandOffsetPacket;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionPhaseTimePacket;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionWithOBBSyncPacket;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.TrActionSynchedDataPacket;
import com.github.standobyte.jojo.powersystem.standpower.StandAwakeningDataPacket;
import com.github.standobyte.jojo.powersystem.standpower.packet.StandExpPacket;
import com.github.standobyte.jojo.powersystem.standpower.packet.TrStaminaPacket;
import com.github.standobyte.jojo.powersystem.unlockableskill.ClLearnSkillPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input.ClStandItemInputPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ClExtendedContainerClickPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerClosePacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerOpenPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetContentPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetDataPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetSlotPacket;
import com.github.standobyte.jojo.subsystems.entity_grab.TrSetGrabbedEntityPacket;
import com.github.standobyte.jojo.subsystems.entity_possessionv2.TrPossessEntityPacket;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.SetClientControllerPacket;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.mob.ClControlledMobCommandPacket;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.mob.ClMobControlMovementPacket;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.ClStandManualMovementPacket;
import com.github.standobyte.jojo.subsystems.entity_useitem.ClStandClickPacket;
import com.github.standobyte.jojo.subsystems.itemtracking.TrackedItemPacket;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenChunkBlocksPacket;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.CDBlocksRestoredPacket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketsRegister {

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClAbilityInputPacket.Handler(JojoMod.resLoc("clkey")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClNoParamsPacket.Handler(JojoMod.resLoc("clsignal")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClAimTargetPacket.Handler(JojoMod.resLoc("clientaim")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClLearnSkillPacket.Handler(JojoMod.resLoc("cllearnskill")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClSetStandSkinPacket.Handler(JojoMod.resLoc("clskin")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClStandManualMovementPacket.Handler(JojoMod.resLoc("clstandmove")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClMobControlMovementPacket.Handler(JojoMod.resLoc("clmobctrlmove")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClControlledMobCommandPacket.Handler(JojoMod.resLoc("clmobitemslot")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClStandClickPacket.Handler(JojoMod.resLoc("clstandclick")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClDebugCommandPacket.Handler(JojoMod.resLoc("cldebug")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClStandItemInputPacket.Handler(JojoMod.resLoc("clstanditem")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClSetSewingMachineItemPacket.Handler(JojoMod.resLoc("clsewingitem")));
		registerPacket(registrar, PayloadRegistrar::playToServer, new ClExtendedContainerClickPacket.Handler(JojoMod.resLoc("clslotclick")));

		registerPacket(registrar, PayloadRegistrar::playToClient, new DatapackStandsPacket.Handler(JojoMod.resLoc("datastands")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrAbilityUsePacket.Handler(JojoMod.resLoc("abilityuse")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityActionInstancePacket.Handler(JojoMod.resLoc("action")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrActionSynchedDataPacket.Handler(JojoMod.resLoc("actiondata")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityActionPhaseTimePacket.Handler(JojoMod.resLoc("actionphase")));
        registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityActionWithOBBSyncPacket.Handler(JojoMod.resLoc("obbsync")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new StandExpPacket.Handler(JojoMod.resLoc("standxp")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPowerStandInstancePacket.Handler(JojoMod.resLoc("standinst")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPowerTypePacket.Handler(JojoMod.resLoc("plpowertype")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPowerDataPacket.Handler(JojoMod.resLoc("powerdata")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrSetStandEntityPacket.Handler(JojoMod.resLoc("standentity")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrNonEntityStandSummonPacket.Handler(JojoMod.resLoc("nestandsummon")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrEntityCustomEffectsPacket.Handler(JojoMod.resLoc("standeffect")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrStandEffectSynchedDataPacket.Handler(JojoMod.resLoc("steffdata")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrStaminaPacket.Handler(JojoMod.resLoc("stamina")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrResolvePacket.Handler(JojoMod.resLoc("resolve")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ResolveBoostsPacket.Handler(JojoMod.resLoc("resolveboost")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new StandAwakeningDataPacket.Handler(JojoMod.resLoc("standawake")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrAimTargetPacket.Handler(JojoMod.resLoc("aim")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrStandSkinPacket.Handler(JojoMod.resLoc("standskin")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new StandSkinSoundPacket.Handler(JojoMod.resLoc("standsound")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new StandEntitySoundPacket.Handler(JojoMod.resLoc("standsound2")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrSyncStandOffsetPacket.Handler(JojoMod.resLoc("standoffset")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new SetClientControllerPacket.Handler(JojoMod.resLoc("ctrltarget")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrPossessEntityPacket.Handler(JojoMod.resLoc("possess")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new CustomExplosionPacket.Handler(JojoMod.resLoc("expl")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new BrokenBlocksParticlesAndSoundsPacket.Handler(JojoMod.resLoc("blbreak")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrackedItemPacket.Handler(JojoMod.resLoc("itemtrack")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrClothesItemsPacket.Handler(JojoMod.resLoc("clothes")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrSetGrabbedEntityPacket.Handler(JojoMod.resLoc("grab")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ExternalContainerOpenPacket.Handler(JojoMod.resLoc("extcopen")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ExternalContainerClosePacket.Handler(JojoMod.resLoc("extcclose")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ExternalContainerSyncSetSlotPacket.Handler(JojoMod.resLoc("extcslot")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ExternalContainerSyncSetContentPacket.Handler(JojoMod.resLoc("extccont")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ExternalContainerSyncSetDataPacket.Handler(JojoMod.resLoc("extcdata")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new EntitySyncMotionBypassingPacket.Handler(JojoMod.resLoc("motfix")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new TrResetDeathTimePacket.Handler(JojoMod.resLoc("undeath")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new DeflectedBulletPacket.Handler(JojoMod.resLoc("projdefl")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new BloodParticlesPacket.Handler(JojoMod.resLoc("blood")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new BrokenChunkBlocksPacket.Handler(JojoMod.resLoc("brokenblocks")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new CDBlocksRestoredPacket.Handler(JojoMod.resLoc("restoreblocks")));
		registerPacket(registrar, PayloadRegistrar::playToClient, new ItemBreakVisualsPacket.Handler(JojoMod.resLoc("itemparticle")));
	}

	
	public static interface PacketHandler<T extends CustomPacketPayload> {
		CustomPacketPayload.Type<T> type();
		void handle(T payload, IPayloadContext context);
	}
	
	public static interface PacketOGHandler<T extends CustomPacketPayload> extends PacketHandler<T> {
		void encode(T packet, RegistryFriendlyByteBuf buf);
		T decode(RegistryFriendlyByteBuf buf);
	}
	
	public static interface PacketCodecHandler<T extends CustomPacketPayload> extends PacketHandler<T> {
		StreamCodec<? super RegistryFriendlyByteBuf, T> reader();
	}
	
	public static <T extends CustomPacketPayload> void registerPacket(PayloadRegistrar registrar, PacketType packetType, PacketOGHandler<T> handler) {
		packetType.register(registrar, handler.type(), StreamCodec.ofMember(handler::encode, handler::decode), handler::handle);
	}
	
	public static <T extends CustomPacketPayload> void registerPacket(PayloadRegistrar registrar, PacketType packetType, PacketCodecHandler<T> handler) {
		packetType.register(registrar, handler.type(), handler.reader(), handler::handle);
	}
	
	@FunctionalInterface
	public static interface PacketType {
		<T extends CustomPacketPayload> void register(PayloadRegistrar registrar, 
				CustomPacketPayload.Type<T> type, 
				StreamCodec<? super RegistryFriendlyByteBuf, T> reader, 
				IPayloadHandler<T> handler);
	}
}
