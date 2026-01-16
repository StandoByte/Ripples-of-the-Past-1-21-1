package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojoimpl.stands.StandInitCrazyDiamond;
import com.github.standobyte.jojoimpl.stands.StandInitHierophantGreen;
import com.github.standobyte.jojoimpl.stands.StandInitStarPlatinum;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// XXX (data-driven stands) test the stand datapack configs
// XXX add a way to ban hardcoded stands
public class ModStands {
	public static final DeferredRegister<StandType> DEFAULT_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, JojoMod.MOD_ID);
	
	public static InputUseVanillaMapping USE_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.use_special_ability");
	public static InputUseVanillaMapping SWITCH_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.ability_hotbar");
	
	// Adding all the abilities and skills takes quite a few lines, so I decided to put each into a separate file.
	// Makes it a bit easier to compare them between each other too.
	
	public static final DeferredHolder<StandType, EntityStandType> STAR_PLATINUM = DEFAULT_STANDS.register("star_platinum", StandInitStarPlatinum::create);
	public static final DeferredHolder<StandType, EntityStandType> CRAZY_DIAMOND = DEFAULT_STANDS.register("crazy_diamond", StandInitCrazyDiamond::create);
	public static final DeferredHolder<StandType, EntityStandType> HIEROPHANT_GREEN = DEFAULT_STANDS.register("hierophant_green", StandInitHierophantGreen::create);
	
}
