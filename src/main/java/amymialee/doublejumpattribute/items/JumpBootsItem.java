package amymialee.doublejumpattribute.items;

import amymialee.doublejumpattribute.DoubleJumpAttribute;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.UUID;

public class JumpBootsItem extends ArmorItem implements Wearable {
    private static final UUID MODIFIERS = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public JumpBootsItem(Settings settings) {
        super(new JumpBootsArmourMaterial(), EquipmentSlot.FEET, settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(DoubleJumpAttribute.JUMPS, new EntityAttributeModifier(MODIFIERS, "Jump Modifier", 1, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public void postProcessNbt(NbtCompound nbt) {
        //nbt.putBoolean("Unbreakable", true);
        NbtList nbtList = nbt.getList("Enchantments", 10);
        if (!nbtList.contains(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(Enchantments.FEATHER_FALLING), 4))) {
            nbtList.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(Enchantments.FEATHER_FALLING), 4));
            nbt.put("Enchantments", nbtList);
        }
        super.postProcessNbt(nbt);
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.FEET ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }
}
