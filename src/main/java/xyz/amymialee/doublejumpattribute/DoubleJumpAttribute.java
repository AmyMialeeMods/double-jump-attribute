package xyz.amymialee.doublejumpattribute;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import xyz.amymialee.doublejumpattribute.items.JumpBootsItem;

public class DoubleJumpAttribute implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "doublejumpattribute";
    public static final ComponentKey<DoubleJumpComponent> DOUBLE_JUMPS = ComponentRegistry.getOrCreate(id("doublejump"), DoubleJumpComponent.class);
    public static final EntityAttribute JUMPS = Registry.register(Registries.ATTRIBUTE, id("doublejumpattribute"), new ClampedEntityAttribute("attribute." + MOD_ID + '.' + "jumps", 0, 0, 1024).setTracked(true));
    public static final Identifier JUMP_PACKET = id("jump");
    public static final Item JUMP_BOOTS = Registry.register(Registries.ITEM, id("jump_boots"), new JumpBootsItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof()));
    public static final SoundEvent JUMP_SOUND_EVENT = Registry.register(Registries.SOUND_EVENT, id("entity.doublejumpattribute.jump"), SoundEvent.of(id("entity.doublejumpattribute.jump")));
    public static final Identifier DOUBLE_JUMP_STAT = Registry.register(Registries.CUSTOM_STAT, "double_jumped", id("double_jumped"));

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(JUMP_PACKET, (server, playerEntity, playNetworkHandler, packetByteBuf, packetSender) -> DOUBLE_JUMPS.get(playerEntity).performDoubleJump());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> itemGroup.add(JUMP_BOOTS));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, DOUBLE_JUMPS).respawnStrategy(RespawnCopyStrategy.CHARACTER).end(DoubleJumpComponent::new);
    }

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }
}