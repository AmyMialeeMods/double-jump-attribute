package xyz.amymialee.doublejumpattribute.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amymialee.doublejumpattribute.DoubleJumpComponent;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "computeFallDamage", at = @At("RETURN"), cancellable = true)
    private void doubleJumpAttribute$reduceFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            double damage = cir.getReturnValue();
            DoubleJumpComponent component = DoubleJumpComponent.get(player);
            for (int i = 0; i < component.getRemainingJumps(); i++) {
                damage /= 2;
            }
            cir.setReturnValue((int) damage);
        }
    }
}