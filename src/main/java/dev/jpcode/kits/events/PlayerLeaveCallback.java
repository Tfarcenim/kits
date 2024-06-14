package dev.jpcode.kits.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerLeaveCallback {
    Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class,
        (listeners) -> (player) -> {
            for (PlayerLeaveCallback event : listeners) {

                event.onPlayerLeave(player);
            }
        });

    void onPlayerLeave(ServerPlayer player);

}
