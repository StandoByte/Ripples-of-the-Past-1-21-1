package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.client.particle.type.BloodParticle;
import com.github.standobyte.jojo.client.particle.type.MeteoriteVirusParticle;
import com.github.standobyte.jojo.client.particle.type.OnomatopoeiaParticle;
import com.github.standobyte.jojo.client.particle.type.RPSPickPartile;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojoimpl.powers.hamon.client.particle.HamonAuraParticle;
import com.github.standobyte.jojoimpl.powers.hamon.client.particle.HamonSparkParticle;
import com.github.standobyte.jojoimpl.powers.pillarman.client.particles.AtmosphericRiftParticle;
import com.github.standobyte.jojoimpl.powers.pillarman.client.particles.DivineSandstormParticle;
import com.github.standobyte.jojoimpl.powers.pillarman.client.particles.LightGlintParticle;
import com.github.standobyte.jojoimpl.powers.pillarman.client.particles.LightModeFlashParticle;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDRestorationParticle;
import com.github.standobyte.jojoimpl.stands.starplatinum.client.AirStreamParticle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ModParticlesClient {

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(ModParticles.BLOOD.get(),				BloodParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_SPARK.get(),			HamonSparkParticle.HamonParticleFactory::new);
		event.registerSpriteSet(ModParticles.HAMON_SPARK_BLUE.get(),	HamonSparkParticle.HamonParticleFactory::new);
		event.registerSpriteSet(ModParticles.HAMON_SPARK_YELLOW.get(),	HamonSparkParticle.HamonParticleFactory::new);
		event.registerSpriteSet(ModParticles.HAMON_SPARK_RED.get(),		HamonSparkParticle.HamonParticleFactory::new);
		event.registerSpriteSet(ModParticles.HAMON_SPARK_SILVER.get(),	HamonSparkParticle.HamonParticleFactory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA.get(),			HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_BLUE.get(),		HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_YELLOW.get(),	HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_RED.get(),		HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_SILVER.get(),	HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_GREEN.get(),	HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.HAMON_AURA_RAINBOW.get(),	HamonAuraParticle.Factory::new);
		event.registerSpriteSet(ModParticles.BOILING_BLOOD_POP.get(),	LavaParticle.Provider::new);
		event.registerSpriteSet(ModParticles.METEORITE_VIRUS.get(),		MeteoriteVirusParticle.Factory::new);
		event.registerSpriteSet(ModParticles.MENACING.get(),			OnomatopoeiaParticle.GoFactory::new);
		event.registerSpriteSet(ModParticles.RESOLVE.get(),				OnomatopoeiaParticle.DoFactory::new);
		event.registerSpriteSet(ModParticles.SOUL_CLOUD.get(),			SoulCloudParticleFactory::new);
		event.registerSpriteSet(ModParticles.AIR_STREAM.get(),			AirStreamParticle.Factory::new);
		event.registerSpriteSet(ModParticles.CD_RESTORATION.get(),		CrazyDRestorationParticle.Factory::new);
		event.registerSpriteSet(ModParticles.RPS_ROCK.get(),			RPSPickPartile.Factory::new);
		event.registerSpriteSet(ModParticles.RPS_PAPER.get(),			RPSPickPartile.Factory::new);
		event.registerSpriteSet(ModParticles.RPS_SCISSORS.get(),		RPSPickPartile.Factory::new);
		event.registerSpriteSet(ModParticles.SANDSTORM.get(),		 	DivineSandstormParticle.Factory::new);
		event.registerSpriteSet(ModParticles.RIFT.get(),		 		AtmosphericRiftParticle.Factory::new);
		event.registerSpriteSet(ModParticles.LIGHT_SPARK.get(),			LightGlintParticle.Factory::new);
		event.registerSpriteSet(ModParticles.LIGHT_MODE_FLASH.get(),	LightModeFlashParticle.Factory::new);
	}

	private static class SoulCloudParticleFactory extends PlayerCloudParticle.Provider {

		public SoulCloudParticleFactory(SpriteSet sprite) {
			super(sprite);
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			Particle particle = super.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.setColor(1.0F, 1.0F, 0.25F);
			return particle;
		}
	}
}
