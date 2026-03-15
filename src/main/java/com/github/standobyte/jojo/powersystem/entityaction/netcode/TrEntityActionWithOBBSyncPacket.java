package com.github.standobyte.jojo.powersystem.entityaction.netcode;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.entityaction.ActionOBB;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrEntityActionWithOBBSyncPacket(int performerId,
                                              int actionId) implements CustomPacketPayload {

    private static CustomPacketPayload.Type<TrEntityActionWithOBBSyncPacket> type;

    public static class Handler implements PacketsRegister.PacketCodecHandler<TrEntityActionWithOBBSyncPacket> {

        public Handler(ResourceLocation packetId) {
            type = new CustomPacketPayload.Type<>(packetId);
        }

        @Override
        public Type<TrEntityActionWithOBBSyncPacket> type() {
            return type;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TrEntityActionWithOBBSyncPacket> reader() {
            return STREAM_CODEC;
        }


        public static final StreamCodec<RegistryFriendlyByteBuf, TrEntityActionWithOBBSyncPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, TrEntityActionWithOBBSyncPacket::performerId,
                ByteBufCodecs.VAR_INT, TrEntityActionWithOBBSyncPacket::actionId,
                TrEntityActionWithOBBSyncPacket::new);

        @Override
        public void handle(TrEntityActionWithOBBSyncPacket payload, IPayloadContext context) {
            Entity entity = ClientProxy.getEntityById(payload.performerId);
            if (entity instanceof LivingEntity living) {
                EntityActionInstance action = LivingComponentAction.getCurEntityAction(living);
                if (action != null && action.id == payload.actionId && action instanceof ActionOBB obbAction && obbAction.extendableOBB() != null) {
                    obbAction.extendableOBB().setIsMovingForward(false);
                    obbAction.extendableOBB().setIsRetracting(true);
                }
            }
        }

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return type;
    }

}
