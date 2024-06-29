package dev.jpcode.kits;

import dev.jpcode.kits.platform.ForgePlatformHelper;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Mod(Kits.MOD_ID)
public class KitsForge {

    public KitsForge() {
        Kits.LOGGER.info("Kits is getting ready...");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        MinecraftForge.EVENT_BUS.addListener(this::playerJoin);
        MinecraftForge.EVENT_BUS.addListener(this::playerLeave);
        MinecraftForge.EVENT_BUS.addListener(this::clonePlayer);
        MinecraftForge.EVENT_BUS.addListener(this::registerNodes);


        Kits.init();
    }
    public static final TomlConfig.Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<TomlConfig.Server, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(TomlConfig.Server::new);
        SERVER_SPEC = specPair2.getRight();
        SERVER = specPair2.getLeft();
    }


    public void registerNodes(PermissionGatherEvent.Nodes event) {
        ForgePlatformHelper.nodeMap.clear();
        event.addNodes(ForgePlatformHelper.MANAGE_NODE,ForgePlatformHelper.CLAIM_NODE);
        ForgePlatformHelper.nodeMap.put(KitsCommandRegistry.MANAGE,ForgePlatformHelper.MANAGE_NODE);
        ForgePlatformHelper.nodeMap.put(KitsCommandRegistry.CLAIM,ForgePlatformHelper.CLAIM_NODE);

    }

    private void serverStarted(ServerStartedEvent event) {
        Kits.reloadKits(event.getServer());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        KitsCommandRegistry.register(event.getDispatcher(),event.getBuildContext(),event.getCommandSelection());
    }

    private void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerDataManager.onPlayerConnect((ServerPlayer) event.getEntity());
    }

    private void playerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerDataManager.onPlayerLeave((ServerPlayer) event.getEntity());
    }

    private void clonePlayer(PlayerEvent.Clone event) {
        PlayerDataManager.handlePlayerDataRespawnSync((ServerPlayer) event.getOriginal(), (ServerPlayer) event.getEntity(),!event.isWasDeath());
    }

}
