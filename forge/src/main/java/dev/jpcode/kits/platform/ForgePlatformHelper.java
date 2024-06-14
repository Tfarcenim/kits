package dev.jpcode.kits.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.jpcode.kits.Kits;
import dev.jpcode.kits.LuckPermsHelper;
import dev.jpcode.kits.TomlConfig;
import dev.jpcode.kits.platform.services.IPlatformHelper;

import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;

import net.luckperms.api.util.Tristate;

import net.minecraft.commands.CommandSourceStack;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Objects;

public class ForgePlatformHelper implements IPlatformHelper {
    final MLConfig config = new TomlConfig();

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public MLConfig getConfig() {
        return config;
    }

    @Override
    public boolean checkPermission(CommandSourceStack commandSourceStack, String key, int defaultV) {
        ServerPlayer player;
        try {
            player = commandSourceStack.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            Kits.LOGGER.error(e.getMessage());
            return false;
        }

        if (Objects.requireNonNull(player.getServer()).isDedicatedServer()) {
            User user = LuckPermsHelper.luckPerms.getUserManager().getUser(player.getUUID());
            assert user != null;
            CachedPermissionData permissionData = user.getCachedData().getPermissionData();

            Tristate checkResult = permissionData.checkPermission(key);
            return checkResult.asBoolean();
        } else {
            return player.hasPermissions(defaultV);
        }
    }
}
