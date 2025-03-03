/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.QuiltCompatEvent;
import net.fabricmc.fabric.impl.networking.QuiltPacketSender;

/**
 * Offers access to events related to the connection to a client on a logical server while a client is in game.
 *
 * @deprecated Use Quilt Networking's {@link org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents} instead.
 */
@Deprecated
public final class ServerPlayConnectionEvents {
	/**
	 * Event indicating a connection entered the PLAY state, ready for registering channel handlers.
	 *
	 * @see ServerPlayNetworking#registerReceiver(ServerPlayNetworkHandler, Identifier, ServerPlayNetworking.PlayChannelHandler)
	 */
	public static final Event<Init> INIT = QuiltCompatEvent.fromQuilt(org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents.INIT,
			init -> init::onPlayInit,
			invokerGetter -> (handler, server) -> invokerGetter.get().onPlayInit(handler, server)
	);

	/**
	 * An event for notification when the server play network handler is ready to send packets to the client.
	 *
	 * <p>At this stage, the network handler is ready to send packets to the client.
	 */
	public static final Event<Join> JOIN = QuiltCompatEvent.fromQuilt(org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents.JOIN,
			join -> (handler, sender, server) -> join.onPlayReady(handler, new QuiltPacketSender(sender), server),
			invokerGetter -> (handler, sender, server) -> invokerGetter.get().onPlayReady(handler, sender, server)
	);

	/**
	 * An event for the disconnection of the server play network handler.
	 *
	 * <p>No packets should be sent when this event is invoked.
	 */
	public static final Event<Disconnect> DISCONNECT = QuiltCompatEvent.fromQuilt(org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents.DISCONNECT,
			disconnect -> disconnect::onPlayDisconnect,
			invokerGetter -> (handler, server) -> invokerGetter.get().onPlayDisconnect(handler, server)
	);

	private ServerPlayConnectionEvents() {
	}

	@FunctionalInterface
	public interface Init {
		void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Join {
		void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server);
	}
}
