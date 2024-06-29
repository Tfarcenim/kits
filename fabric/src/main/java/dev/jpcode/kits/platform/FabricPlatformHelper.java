package dev.jpcode.kits.platform;

import dev.jpcode.kits.KitsFabric;
import dev.jpcode.kits.platform.services.IPlatformHelper;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public MLConfig getConfig() {
        return KitsFabric.CONFIG;
    }

    @Override
    public boolean checkPermission(CommandSourceStack commandSourceStack, String key, int defaultV) {
        return Permissions.check(commandSourceStack,"kits."+ key, defaultV);
    }

    @Override
    public boolean canUseKit(CommandSourceStack commandSourceStack, String key) {
        return checkPermission(commandSourceStack,key, Commands.LEVEL_GAMEMASTERS);
    }
}
