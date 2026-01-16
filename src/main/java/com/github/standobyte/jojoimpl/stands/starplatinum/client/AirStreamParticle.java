package com.github.standobyte.jojoimpl.stands.starplatinum.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AirStreamParticle extends TextureSheetParticle {
    private final float yRot;
    private final float xRot;

    protected AirStreamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 2.0F;
        this.yRot = (float) Mth.atan2(xSpeed, zSpeed);
        this.xRot = (float) Mth.atan2(ySpeed, Mth.sqrt((float) (xSpeed * xSpeed + zSpeed * zSpeed)));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        renderFromRotation(vertexConsumer, camera, partialTick, yRot, xRot, true);
        renderFromRotation(vertexConsumer, camera, partialTick, yRot, xRot, false);
    }

    private void renderFromRotation(VertexConsumer vertexConsumer, Camera camera, float partialTick, float yRot, float xRot, boolean mirror) {
        Vec3 cameraPos = camera.getPosition();
        float particleX = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float particleY = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float particleZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());

        Quaternionf quaternion = new Quaternionf();
        quaternion.rotateY(yRot);
        quaternion.rotateX(-xRot);

        if (mirror) {
            quaternion.rotateY((float) (Math.PI / 2.0F));
        } else {
            quaternion.rotateY((float) (-Math.PI / 2.0F));
        }

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float scale = this.getQuadSize(partialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(quaternion);
            vertex.mul(scale);
            vertex.add(particleX, particleY, particleZ);
        }

        float u0 = mirror ? getU1() : getU0();
        float u1 = mirror ? getU0() : getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partialTick);

        int r = (int) (this.rCol * 255.0F);
        int g = (int) (this.gCol * 255.0F);
        int b = (int) (this.bCol * 255.0F);
        int a = (int) (this.alpha * 255.0F);

        vertexConsumer.addVertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).setUv(u1, v1).setColor(r, g, b, a).setLight(light);
        vertexConsumer.addVertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).setUv(u1, v0).setColor(r, g, b, a).setLight(light);
        vertexConsumer.addVertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).setUv(u0, v0).setColor(r, g, b, a).setLight(light);
        vertexConsumer.addVertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).setUv(u0, v1).setColor(r, g, b, a).setLight(light);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            AirStreamParticle particle = new AirStreamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            particle.scale(1.5F);
            particle.setLifetime(16);
            particle.hasPhysics = false;
            return particle;
        }
    }
}