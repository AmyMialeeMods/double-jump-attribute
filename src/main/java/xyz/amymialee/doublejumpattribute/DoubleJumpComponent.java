package xyz.amymialee.doublejumpattribute;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.amymialee.doublejumpattribute.mixin.LivingEntityAccessor;

public class DoubleJumpComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;
    private final LivingEntityAccessor living;
    private int jumpsUsed = 0;
    private int jumpCooldown = 0;

    public DoubleJumpComponent(PlayerEntity player) {
        this.player = player;
        this.living = (LivingEntityAccessor) player;
    }

    public static DoubleJumpComponent get(PlayerEntity player) {
        return DoubleJumpAttribute.DOUBLE_JUMPS.get(player);
    }

    public void sync() {
        DoubleJumpAttribute.DOUBLE_JUMPS.sync(this.player);
    }

    public void performDoubleJump() {
        if (this.living.getJumpingCooldown() <= 0 && this.getRemainingJumps() > 0) {
            this.jumpsUsed++;
            this.player.getWorld().playSoundFromEntity(null, this.player, DoubleJumpAttribute.JUMP_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            this.player.incrementStat(DoubleJumpAttribute.DOUBLE_JUMP_STAT);
            this.player.fallDistance = 0;
            this.living.setJumpingCooldown(10);
            this.sync();
        }
    }

    @Override
    public void tick() {
        if (this.player.isOnGround()) {
            this.jumpsUsed = 0;
        }
    }

    @Override
    public void clientTick() {
        if (this.living.isJumping() && this.jumpCooldown <= 0 && this.getRemainingJumps() > 0) {
            this.jumpCooldown = 12;
            Vec3d vec3d = this.player.getVelocity();
            if (this.player.isSprinting()) {
                vec3d = new Vec3d(vec3d.x, 0.8 * (1 + this.player.getJumpBoostVelocityModifier()), vec3d.z);
                float f = this.player.getYaw() * 0.017453292F;
                this.player.setVelocity(vec3d.add(-MathHelper.sin(f) * 0.2F, 0.0D, MathHelper.cos(f) * 0.2F));
            } else {
                this.player.setVelocity(vec3d.x, 0.7 * (1 + this.player.getJumpBoostVelocityModifier()), vec3d.z);
            }
            this.player.velocityModified = true;
            ClientPlayNetworking.send(DoubleJumpAttribute.JUMP_PACKET, PacketByteBufs.empty());
        }
        if (this.player.isOnGround()) {
            this.jumpCooldown = 8;
        } else {
            if (this.jumpCooldown > 0) this.jumpCooldown--;
        }
        if (this.jumpsUsed > 0 && this.player.getRandom().nextFloat() > 0.75) this.player.getWorld().addParticle(ParticleTypes.CLOUD, this.player.getX(), this.player.getY(), this.player.getZ(), 0, 0, 0);
        this.tick();
    }

    @Override
    public void serverTick() {
        CommonTickingComponent.super.serverTick();
    }

    public int getPossibleJumps() {
        return (int) this.player.getAttributeValue(DoubleJumpAttribute.JUMPS);
    }

    public int getRemainingJumps() {
        return this.getPossibleJumps() - this.jumpsUsed;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        int jumpsUsed = tag.getInt("jumpsUsed");
        if (this.player.getWorld().isClient() && jumpsUsed > this.jumpsUsed) {
            for (int i = 0; i < 12; i++) {
                this.player.getWorld().addParticle(ParticleTypes.CLOUD, this.player.getX() + this.player.getRandom().nextGaussian() * 0.2, this.player.getBoundingBox().minY + 0.5D + this.player.getRandom().nextGaussian() * 0.2, this.player.getZ() + this.player.getRandom().nextGaussian() * 0.2,
                        this.player.getRandom().nextGaussian() * 0.15f, this.player.getRandom().nextFloat() * 0.15f, this.player.getRandom().nextGaussian() * 0.15f);
            }
        }
        this.jumpsUsed = jumpsUsed;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("jumpsUsed", this.jumpsUsed);
    }
}