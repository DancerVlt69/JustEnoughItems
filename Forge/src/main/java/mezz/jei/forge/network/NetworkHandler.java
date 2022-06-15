package mezz.jei.forge.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.ServerPacketRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final String NETWORK_PROTOCOL_VERSION = "1.0.0";
	private final EventNetworkChannel channel;

	public NetworkHandler() {
		channel = NetworkRegistry.newEventChannel(
			Constants.NETWORK_CHANNEL_ID,
			() -> NETWORK_PROTOCOL_VERSION,
			NetworkHandler::isClientAcceptedVersion,
			NetworkHandler::isServerAcceptedVersion
		);
	}

	private static boolean isClientAcceptedVersion(String version) {
		return true;
	}

	private static boolean isServerAcceptedVersion(String version) {
		return true;
	}

	public void registerServerPacketHandler(ServerPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ClientCustomPayloadEvent event) -> {
			NetworkEvent.Context context = event.getSource().get();
			ServerPlayer player = context.getSender();
			if (player == null) {
				LOGGER.error("Packet error, the sender player is missing for event: {}", event);
				return;
			}
			packetRouter.onPacket(event.getPayload(), player);
			context.setPacketHandled(true);
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientPacketHandler(ClientPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ServerCustomPayloadEvent event) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player == null) {
				LOGGER.error("Packet error, the local player is missing for event: {}", event);
				return;
			}
			packetRouter.onPacket(event.getPayload(), player);
			NetworkEvent.Context context = event.getSource().get();
			context.setPacketHandled(true);
		});
	}
}
