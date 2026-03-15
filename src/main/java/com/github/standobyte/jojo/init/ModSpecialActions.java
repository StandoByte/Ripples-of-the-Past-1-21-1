package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.subsystems.entity_useitem.VanillaItemClickAsAction;
import com.github.standobyte.jojo.subsystems.entity_useitem.VanillaItemUseAsAction;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityUnsummonAction;

import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSpecialActions {
	public static final DeferredRegister<SpecialEntityActionType> ACTIONS = DeferredRegister.create(JojoRegistries.NON_POWER_ACTIONS_REG, JojoMod.MOD_ID);
	
	public static final Supplier<SpecialEntityActionType> STAND_UNSUMMON = ACTIONS.register("stand_unsummon", 
			StandEntityUnsummonAction::new);
	
	public static final Supplier<SpecialEntityActionType> RMB_USING_ITEM = ACTIONS.register("rmb_using_item", 
			key -> new VanillaItemUseAsAction(key));
	
	public static final Supplier<SpecialEntityActionType> RMB_CLICK_ITEM = ACTIONS.register("rmb_click_item", 
			key -> new VanillaItemClickAsAction(key));
	
}
