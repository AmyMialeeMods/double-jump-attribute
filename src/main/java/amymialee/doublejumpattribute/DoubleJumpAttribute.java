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
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    public static final Identifier JUMP_SOUND_ID = new Identifier(MODID, "entity.doublejumpattribute.jump");
    public static SoundEvent JUMP_SOUND_EVENT = new SoundEvent(JUMP_SOUND_ID);
    public static final Identifier DOUBLE_JUMP_STAT = new Identifier(MODID, "double_jumped");

    public static DoubleJumpAttributeConfig config = null;

    @Override
    public void onInitialize() {
        config = DoubleJumpAttributeConfig.load();

        Registry.register(Registry.SOUND_EVENT, JUMP_SOUND_ID, JUMP_SOUND_EVENT);
        Registry.register(Registry.ITEM, new Identifier(MODID, "jump_boots"), JUMP_BOOTS);
        Registry.register(Registry.ATTRIBUTE, new Identifier(MODID, "double_jump_attribute"), JUMPS);
        Registry.register(Registry.CUSTOM_STAT, "double_jumped", DOUBLE_JUMP_STAT);

        ServerPlayNetworking.registerGlobalReceiver(DOUBLEJUMPED, (server, playerEntity, playNetworkHandler, packetByteBuf, packetSender) -> {
            ((LastHurtWrapper) playerEntity).doubleJump();
            playerEntity.getServerWorld().playSoundFromEntity(null, playerEntity, JUMP_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            for (int i = 0; i < DoubleJumpAttributeConfig.load().jumpParticleCount; i++) {
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
                                                            if (!ctx.getSource().hasPermissionLevel(2)) {
                                                                ctx.getSource().sendError(new LiteralText("Insufficient Permissions.").formatted(Formatting.RED));
                                                                return 0;
                                                            }
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

                                                            ctx.getSource().sendFeedback(new LiteralText("Set velocity of " + to.getDisplayName().getString() + " to " + x + " " + y + " " + z + ".").formatted(Formatting.GRAY), true);
                                                            return 0;
                                                        })))))));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
                CommandManager.literal("addplayervelocity")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("x", FloatArgumentType.floatArg())
                                        .then(CommandManager.argument("y", FloatArgumentType.floatArg())
                                                .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                                        .executes(ctx -> {
                                                            if (!ctx.getSource().hasPermissionLevel(2)) {
                                                                ctx.getSource().sendError(new LiteralText("Insufficient Permissions.").formatted(Formatting.RED));
                                                                return 0;
                                                            }
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

                                                            ctx.getSource().sendFeedback(new LiteralText("Added " + x + " " + y + " " + z + " velocity to " + to.getDisplayName().getString() + ".").formatted(Formatting.GRAY), true);
                                                            return 0;
                                                        })))))));
    }
}
