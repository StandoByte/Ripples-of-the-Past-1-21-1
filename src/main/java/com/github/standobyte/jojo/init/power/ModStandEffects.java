package com.github.standobyte.jojo.init.power;

import java.util.function.Supplier;

import com.github.standobyte.core_subsystems.entitydata.EntityAttachmentType;
import com.github.standobyte.jojoimpl.stands.crazydiamond.DriedBloodDropsEffect;

/**
 * @deprecated Stand effects are now registered in {@link ModStandAbilities}, in the same file as abilities
 */
@Deprecated
public final class ModStandEffects {


	public static final Supplier<EntityAttachmentType<DriedBloodDropsEffect>> CRAZY_D_BLOOD_DROPS = ModStandAbilities.EFFECT_CD_BLOOD_DROPS;


	public static void load() {}

}
