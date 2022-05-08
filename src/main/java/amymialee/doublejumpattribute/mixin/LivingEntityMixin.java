package amymialee.doublejumpattribute.mixin;

import amymialee.doublejumpattribute.DoubleJumpAttribute;
import amymialee.doublejumpattribute.client.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccessor {
    @Shadow private int jumpingCooldown;

    @Shadow public abstract @Nullable StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Shadow public abstract double getAttributeValue(EntityAttribute attribute);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public int getJumpingCooldown() {
        return jumpingCooldown;
    }

    @Inject(method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;", require = 1, allow = 1, at = @At("RETURN"))
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue().add(DoubleJumpAttribute.JUMPS);
    }

    @Inject(method = "computeFallDamage", at = @At("HEAD"), cancellable = true)
    protected void computeFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        if (getAttributeValue(DoubleJumpAttribute.JUMPS) > 0) {
            StatusEffectInstance statusEffectInstance = getStatusEffect(StatusEffects.JUMP_BOOST);
            float f = statusEffectInstance == null ? 0.0F : (float)(statusEffectInstance.getAmplifier() + 1);
            cir.setReturnValue(MathHelper.ceil((fallDistance - 3.0F - f - (getAttributeValue(DoubleJumpAttribute.JUMPS) * 3.0f)) * damageMultiplier));
        }
    }
}
