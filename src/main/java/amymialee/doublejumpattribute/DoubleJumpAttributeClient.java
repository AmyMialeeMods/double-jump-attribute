package amymialee.doublejumpattribute;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class DoubleJumpAttributeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DoubleJumpAttribute.PACKET_ADD_VELOCITY, (minecraftClient, playNetworkHandler, packetByteBuf, packetSender) -> {
            float x = packetByteBuf.readFloat();
            float y = packetByteBuf.readFloat();
            float z = packetByteBuf.readFloat();
            minecraftClient.execute(() -> {
                assert minecraftClient.player != null;
                minecraftClient.player.addVelocity(x, y, z);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DoubleJumpAttribute.PACKET_SET_VELOCITY, (minecraftClient, playNetworkHandler, packetByteBuf, packetSender) -> {
            float x = packetByteBuf.readFloat();
            float y = packetByteBuf.readFloat();
            float z = packetByteBuf.readFloat();
            minecraftClient.execute(() -> {
                assert minecraftClient.player != null;
                minecraftClient.player.setVelocityClient(x, y, z);
            });
        });
    }
}