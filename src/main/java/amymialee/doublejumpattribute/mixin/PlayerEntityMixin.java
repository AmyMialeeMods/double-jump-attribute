package amymialee.doublejumpattribute.mixin;

import amymialee.doublejumpattribute.DoubleJumpAttribute;
import amymialee.doublejumpattribute.DoubleJumpAttributeConfig;
import amymialee.doublejumpattribute.client.LastHurtWrapper;
import amymialee.doublejumpattribute.client.LivingEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements LastHurtWrapper {
    @Shadow public abstract void jump();
    @Shadow public abstract boolean isSpectator();

    @Unique private boolean isDoubleJumping = false;
    @Unique public int doubleJumpCount = 0;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    @Override
    public void doubleJump() {
        ((PlayerEntity) ((Object) this)).incrementStat(DoubleJumpAttribute.DOUBLE_JUMP_STAT);
        fallDistance = 0;
        setDoubleJumpAmount(doubleJumpCount + 1);
        double d = this.getJumpVelocity();
        Vec3d vec3d = this.getVelocity();
        double j = this.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 1 + (0.1 * (this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1)) : 1;
        if (this.isSprinting()) {
            this.setVelocity(vec3d.x, d * 1.8 * j, vec3d.z);
            float f = this.getYaw() * 0.017453292F;
            this.setVelocity(this.getVelocity().add(-MathHelper.sin(f) * 0.2F, 0.0D, MathHelper.cos(f) * 0.2F));
        } else {
            this.setVelocity(vec3d.x, d * 1.5 * j, vec3d.z);
        }
        this.velocityDirty = true;
        if (this.world.isClient) {
            sendPacketClient();
        }
    }

    @Unique
    @Environment(EnvType.CLIENT)
    private static void sendPacketClient() {
        ClientPlayNetworking.send(DoubleJumpAttribute.DOUBLEJUMPED, PacketByteBufs.empty());
    }

    @Inject(method = "tickMovement", at = @At(value = "HEAD"))
    public void tickMovementHead(CallbackInfo ci) {
        if (((LivingEntityAccessor) this).getJumpingCooldown() == 10 && jumping) {
            isDoubleJumping = true;
        } else if (!jumping) {
            isDoubleJumping = false;
        }
        int jumps = (int) DoubleJumpAttribute.getDoubleJumps(this);
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
        if ((!itemStack.isOf(Items.ELYTRA) || this.isFallFlying()) && jumping && !isOnGround() && (doubleJumpCount < jumps + DoubleJumpAttributeConfig.load().jumpJumpCount) && ((LivingEntityAccessor) this).getJumpingCooldown() == 0 && !isSpectator() && !isDoubleJumping) {
            isDoubleJumping = true;
            doubleJump();
        } else if (isOnGround()) {
            setDoubleJumpAmount(0);
        }
    }

    @Unique
    @Override
    public void setDoubleJumpAmount(int value) {
        doubleJumpCount = value;
    }
}
