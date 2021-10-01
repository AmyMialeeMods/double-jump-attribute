package amymialee.doublejumpattribute;

import amymialee.doublejumpattribute.client.LastHurtWrapper;
import amymialee.doublejumpattribute.items.JumpBootsItem;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class DoubleJumpAttribute implements ModInitializer {
    public static final String MODID = "doublejumpattribute";
    public static final Identifier ADD_VELOCITY = new Identifier(MODID, "addplayervelocity");
    public static final Identifier SET_VELOCITY = new Identifier(MODID, "setplayervelocity");
    public static final Identifier DOUBLEJUMPED = new Identifier(MODID, "doublejumped");

    public static final EntityAttribute JUMPS = new ClampedEntityAttribute(
            "attribute." + MODID + '.' + "jumps", 0, 0, 1024).setTracked(true);

    public static double getDoubleJumps(final LivingEntity entity) {
        return entity.getAttributeInstance(JUMPS) == null ? 0 : entity.getAttributeInstance(JUMPS).getValue();
    }

    public static final Item JUMP_BOOTS = new JumpBootsItem(new Item.Settings().group(ItemGroup.COMBAT).maxCount(1).rarity(Rarity.RARE).fireproof());

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MODID, "jump_boots"), JUMP_BOOTS);

        Registry.register(Registry.ATTRIBUTE, new Identifier(MODID, "double_jump_attribute"), JUMPS);

        ServerPlayNetworking.registerGlobalReceiver(DOUBLEJUMPED, (server, playerEntity, playNetworkHandler, packetByteBuf, packetSender) -> {
            ((LastHurtWrapper) playerEntity).doubleJump();
            playerEntity.getServerWorld().playSoundFromEntity(null, playerEntity, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            for (int i = 0; i < playerEntity.getRandom().nextInt(35) + 10; i++) {
                playerEntity.getServerWorld().spawnParticles(ParticleTypes.CLOUD,
                        playerEntity.getX() + playerEntity.getRandom().nextGaussian() * 0.12999999523162842D,
                        playerEntity.getBoundingBox().minY + 0.5D + playerEntity.getRandom().nextGaussian() * 0.12999999523162842D,
                        playerEntity.getZ() + playerEntity.getRandom().nextGaussian() * 0.12999999523162842D,
                        1, 0.0D, 0.0D, 0.0D, 0.15D);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
                CommandManager.literal("setplayervelocity")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("x", FloatArgumentType.floatArg())
                                        .then(CommandManager.argument("y", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity to = EntityArgumentType.getPlayer(ctx, "target");
                                                            float x = FloatArgumentType.getFloat(ctx, "x");
                                                            float y = FloatArgumentType.getFloat(ctx, "y");
                                                            float z = FloatArgumentType.getFloat(ctx, "z");
                                                            to.setVelocity(x, y, z);

                                                            PacketByteBuf buf2 = PacketByteBufs.create();
                                                            buf2.writeFloat(x);
                                                            buf2.writeFloat(y);
                                                            buf2.writeFloat(z);
                                                            ServerPlayNetworking.send(to, SET_VELOCITY, buf2);

                                                            return 0;
                                                        })))))));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
                CommandManager.literal("addplayervelocity")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("x", FloatArgumentType.floatArg())
                                        .then(CommandManager.argument("y", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity to = EntityArgumentType.getPlayer(ctx, "target");
                                                            float x = FloatArgumentType.getFloat(ctx, "x");
                                                            float y = FloatArgumentType.getFloat(ctx, "y");
                                                            float z = FloatArgumentType.getFloat(ctx, "z");
                                                            to.addVelocity(x, y, z);

                                                            PacketByteBuf buf2 = PacketByteBufs.create();
                                                            buf2.writeFloat(x);
                                                            buf2.writeFloat(y);
                                                            buf2.writeFloat(z);
                                                            ServerPlayNetworking.send(to, ADD_VELOCITY, buf2);

                                                            return 0;
                                                        })))))));
    }
}
