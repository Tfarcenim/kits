package dev.jpcode.kits;

import dev.jpcode.kits.config.KitsClothConfig;

import me.shedaniel.autoconfig.AutoConfig;

import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class KitsFabric implements ModInitializer {

    public static KitsClothConfig CONFIG;

    @Override
    public void onInitialize() {
        Kits.LOGGER.info("Kits is getting ready...");
        AutoConfig.register(KitsClothConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(KitsClothConfig.class).getConfig();

        ServerLifecycleEvents.SERVER_STARTING.register(Kits::reloadKits);

        CommandRegistrationCallback.EVENT.register(KitsCommandRegistry::register);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerDataManager.onPlayerConnect(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerDataManager.onPlayerLeave(handler.player));
        ServerPlayerEvents.COPY_FROM.register(PlayerDataManager::handlePlayerDataRespawnSync);

        Kits.init();
    }



}
