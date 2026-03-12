package com.github.standobyte.jojo.mc.entity.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class StandArrowEntity extends AbstractArrow {

    public StandArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public StandArrowEntity(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntityTypes.STAND_ARROW.get(), owner.getX(), owner.getEyeY() - (double)0.1F, owner.getZ(), level, pickupItemStack, firedFromWeapon);
        this.setOwner(owner);
    }
    
    public StandArrowEntity(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntityTypes.STAND_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.STAND_ARROW.get());
    }

    // todo Stand arrow anti-disappear on hitting an entity
}
