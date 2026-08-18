package com.github.standobyte.jojo.client.particle;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.particle.type.custom.BloodFromEntityParticle;
import com.github.standobyte.jojo.client.particle.type.custom.EntityPosParticle;
import com.github.standobyte.jojo.util.NotYetImplemented;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDRestorationHandItemParticle;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public abstract class CustomParticlesHelper {

//	private static final Map<ResourceLocation, SpriteSet> SPRITE_SETS = new HashMap<>();
//
//	public static void saveSprites(Minecraft mc) {
//		Map<ResourceLocation, ? extends SpriteSet> spritesMap = ClientReflection.getSpriteSets(mc.particleEngine);
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.MENACING.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.CD_RESTORATION.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_BLUE.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_YELLOW.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_RED.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_SPARK_SILVER.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_BLUE.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_YELLOW.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_RED.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_SILVER.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_GREEN.get());
//		CustomParticlesHelper.saveSprites(spritesMap, ModParticles.HAMON_AURA_RAINBOW.get());
//	}

	public static SpriteSet getSavedSpriteSet(ParticleType<?> particleType) {
		throw new NotYetImplemented();
//		return SPRITE_SETS.get(ForgeRegistries.PARTICLE_TYPES.getKey(particleType));
	}

//	private static void saveSprites(Map<ResourceLocation, ? extends SpriteSet> sprites, ParticleType<?> particleType) {
//		ResourceLocation key = ForgeRegistries.PARTICLE_TYPES.getKey(particleType);
//		SPRITE_SETS.put(key, sprites.get(key));
//	}
//
//
//
//	public static void addMenacingParticleEmitter(Entity entity, SimpleParticleType particle) {
//		Minecraft mc = Minecraft.getInstance();
//		ClientReflection.getTrackingEmitters(Minecraft.getInstance().particleEngine).add(
//				new MenacingParticleEmitter(mc.level, entity, particle, mc.player));
//	}

	public static boolean createCDRestorationParticle(LivingEntity entity, InteractionHand hand) {
		if (!ClientGlobals.canSeeStands) return false;

		EntityPosParticle particle = CrazyDRestorationHandItemParticle.createCustomParticle((ClientLevel) entity.level(), entity, hand);
		return addParticle(particle, particle.getPos(), false, false);
	}

	public static boolean createBloodParticle(ParticleOptions type, @Nullable Entity entity, 
			double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		BloodFromEntityParticle particle = BloodFromEntityParticle.createCustomParticle(
				type, Minecraft.getInstance().level, entity, x, y, z, xSpeed, ySpeed, zSpeed);
		return addParticle(particle, new Vec3(x, y, z), false, false);
	}

//	public static void createHamonAuraParticle(ParticleOptions type, 
//			LivingEntity user, double x, double y, double z) {
//		SpriteSet sprite = getSavedSpriteSet(type.getType());
//		if (sprite != null) {
//			HamonAuraParticle particle = HamonAura3PersonParticle.createCustomParticle(
//					sprite, Minecraft.getInstance().level, user, x, y, z);
//			addParticle(particle, new Vec3(x, y, z), false, false);
//		}
//		else {
//			Minecraft.getInstance().level.addParticle(type, x, y, z, 0, 0, 0);
//		}
//	}
//
//	public static void summonHamonAuraParticlesFirstPerson(ParticleOptions type, LivingEntity user, float particlesPerTick) {
//		SpriteSet sprite = getSavedSpriteSet(type.getType());
//		if (sprite != null) {
//			Random random = user.getRandom();
//			FirstPersonHamonAura particles = FirstPersonHamonAura.getInstance();
//
//			for (HumanoidArm handSide : HumanoidArm.values()) {
//				GeneralUtil.doFractionTimes(() -> {
//					double x = random.nextDouble() * 0.5 - 0.625;
//					double y = random.nextDouble();
//					double z = random.nextDouble() * 0.5 - 0.25;
//					if (handSide == HumanoidArm.LEFT) {
//						x = -x;
//					}
//
//					particles.add(new FirstPersonHamonAura.HamonAuraPseudoParticle(x, y, z, sprite, handSide));
//				}, particlesPerTick);
//			}
//		}
//	}
//
//	public static void addSendoHamonOverdriveParticle(World level, ParticleOptions pParticleData, Direction.Axis blockAxis, 
//			double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, int lifeTime) {
//		TextureSheetParticle particle = new SendoHamonOverdriveParticle(
//				(ClientWorld) level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, blockAxis);
//		particle.setLifetime(lifeTime);
//		particle.pickSprite(getSavedSpriteSet(pParticleData.getType()));
//		particle.setColor(1, 1, 1);
//		addParticle(particle, new Vec3(pX, pY, pZ), pParticleData.getType().getOverrideLimiter(), false);
//	}
//
//	public static void createHamonGliderChargeParticles(LivingEntity entity) {
//		EntityPosParticle particleLeft = HamonGliderChargingParticle.createCustomParticle((ClientWorld) entity.level, entity, Hand.MAIN_HAND);
//		EntityPosParticle particleRight = HamonGliderChargingParticle.createCustomParticle((ClientWorld) entity.level, entity, Hand.OFF_HAND);
//		addParticle(particleLeft, particleLeft.getPos(), false, false);
//		addParticle(particleRight, particleRight.getPos(), false, false);
//	}
//
//	public static void createHamonSparkParticles(@Nullable Entity entityToFollow, Vec3 pos, int particlesCount) {
//		createHamonSparkParticles(entityToFollow, pos.x, pos.y, pos.z, particlesCount);
//	}
//
//	private static final Random RANDOM = new Random();
//	private static final double SPARK_PARTICLE_DIST = 0.05;
//	private static final double SPARK_PARTICLE_SPEED = 0.25;
//	public static void createHamonSparkParticles(@Nullable Entity entityToFollow, double x, double y, double z, int particlesCount) {
//		Minecraft mc = Minecraft.getInstance();
//		ParticleOptions particleData = ModParticles.HAMON_SPARK.get();
//		for (int i = 0; i < particlesCount; ++i) {
//			double xOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
//			double yOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
//			double zOffset = RANDOM.nextGaussian() * SPARK_PARTICLE_DIST;
//			double xSpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
//			double ySpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
//			double zSpeed = RANDOM.nextGaussian() * SPARK_PARTICLE_SPEED;
//
//			if (entityToFollow == null) {
//				mc.level.addParticle(particleData, false, x + xOffset, y + yOffset, z + zOffset, xSpeed, ySpeed, zSpeed);
//			}
//			else {
//				Particle particle = new HamonSparkEntityOffsetParticle(mc.level, entityToFollow, 
//						x, y, z, xSpeed, ySpeed, zSpeed, particleData.getType());
//				CustomParticlesHelper.addParticle(particle, new Vec3(x, y, z), particleData.getType().getOverrideLimiter(), false);
//			}
//		}
//	}
//
//	// note: use chariot's armor layer if it is on
//	public static <T extends StandEntity> void addStandCrumbleParticles(T standEntity, Vec3 pos, TargetHitPart humanoidPart) {
//		EntityRenderer<? super T> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(standEntity);
//		if (renderer instanceof StandEntityRenderer) {
//			StandEntityRenderer<? super T, ?> standRenderer = (StandEntityRenderer<? super T, ?>) renderer;
//			StandEntityModel<? super T> model = standRenderer.getModel(standEntity);
//
//			ResourceLocation texture = renderer.getTextureLocation(standEntity);
//			if (texture == null) return;
//
//			ModelRenderer.TexturedQuad polygon = HumanoidStandModel.getRandomQuad(model.getRandomCubeAt(humanoidPart));
//			if (polygon != null) {
//				ModelRenderer.PositionTextureVertex[] vertices = polygon.vertices;
//				if (vertices.length > 0) {
//					float u0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).min().getAsDouble();
//					float v0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).min().getAsDouble();
//					float u1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).max().getAsDouble();
//					float v1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).max().getAsDouble();
//
//					Minecraft mc = Minecraft.getInstance();
//					double x = pos.x;
//					double y = pos.y;
//					double z = pos.z;
//					StandCrumbleParticle particle = new StandCrumbleParticle(mc.level, x, y, z, 0, 0, 0);
//					particle.setTextureAndUv(texture, u0, v0, u1, v1);
//					mc.particleEngine.add(particle);
//				}
//			}
//
//		}
//	}

	public static void addBlockBreakParticles(BlockPos blockPos, BlockState blockState) {
		Minecraft.getInstance().particleEngine.destroy(blockPos, blockState);
	}

	public static void addBlockShardBreakParticles(Vec3 pos, BlockState blockState) {
		Minecraft mc = Minecraft.getInstance();
		ParticleEngine particleManager = mc.particleEngine;
		ClientLevel level = mc.level;
		BlockPos blockPos = BlockPos.containing(pos);
		if (!blockState.isAir()) {
			for (int i = 0; i < 4; i++) {
				double x = (Math.random() - 0.5) * 0.2;
				double y = (Math.random() - 0.5) * 0.2;
				double z = (Math.random() - 0.5) * 0.2;
				particleManager.add(new TerrainParticle(level, pos.x + x, pos.y + y, pos.z + z, 
						x * 0.25, y * 0.25, z * 0.25, blockState).updateSprite(blockState, blockPos));
			}
		}
	}
	
	public static void addBlockDestroyParticles(BlockState state, BlockPos blockPos, Vec3 particlesPos) {
		Minecraft mc = Minecraft.getInstance();
		if (!state.isAir() && !IClientBlockExtensions.of(state).addDestroyEffects(state, mc.level, blockPos, mc.particleEngine)) {
			VoxelShape voxelShape = state.getShape(mc.level, blockPos);
			voxelShape.forAllBoxes((double minX, double minY, double minZ, double maxX, double maxY, double maxZ) -> {
				double d1 = Math.min(1.0, maxX - minX);
				double d2 = Math.min(1.0, maxY - minY);
				double d3 = Math.min(1.0, maxZ - minZ);
				int i = Math.max(2, Mth.ceil(d1 / 0.25));
				int j = Math.max(2, Mth.ceil(d2 / 0.25));
				int k = Math.max(2, Mth.ceil(d3 / 0.25));

				for (int l = 0; l < i; l++) {
					for (int i1 = 0; i1 < j; i1++) {
						for (int j1 = 0; j1 < k; j1++) {
							double d4 = ((double)l + 0.5) / (double)i;
							double d5 = ((double)i1 + 0.5) / (double)j;
							double d6 = ((double)j1 + 0.5) / (double)k;
							double d7 = d4 * d1 + minX;
							double d8 = d5 * d2 + minY;
							double d9 = d6 * d3 + minZ;
							mc.particleEngine.add(new TerrainParticle(mc.level,
									particlesPos.x + d7,
									particlesPos.y + d8,
									particlesPos.z + d9,
									d4 - 0.5,
									d5 - 0.5,
									d6 - 0.5,
									state, blockPos).updateSprite(state, blockPos));
						}
					}
				}
			});
		}
	}

//	public static void createParticlesEmitter(Entity entity, ParticleOptions type, int ticks) {
//		Minecraft.getInstance().particleEngine.createTrackingEmitter(entity, type, ticks);
//	}
//
	public static boolean addParticle(Particle particle, Vec3 particlePos, boolean overrideLimiter, boolean alwaysVisible) {
		Minecraft mc = Minecraft.getInstance();
		Camera activerenderinfo = mc.gameRenderer.getMainCamera();
		if (activerenderinfo.isInitialized() && mc.particleEngine != null && activerenderinfo.getPosition().distanceToSqr(particlePos) < 1024.0D) {
			if (alwaysVisible || calculateParticleLevel(mc, mc.level, alwaysVisible) != ParticleStatus.MINIMAL) {
				mc.particleEngine.add(particle);
				return true;
			} 
		}
		return false;
	}

	private static ParticleStatus calculateParticleLevel(Minecraft mc, ClientLevel level, boolean overrideLimiter) {
		ParticleStatus status = mc.options.particles().get();
		if (overrideLimiter && status == ParticleStatus.MINIMAL && level.random.nextInt(10) == 0) {
			status = ParticleStatus.DECREASED;
		}

		if (status == ParticleStatus.DECREASED && level.random.nextInt(3) == 0) {
			status = ParticleStatus.MINIMAL;
		}

		return status;
	}

	public static int particlesSetting() {
		return Minecraft.getInstance().options.particles().get().getId();
	}
}
