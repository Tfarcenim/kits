package dev.jpcode.kits.platform;

import dev.jpcode.kits.Kits;
import dev.jpcode.kits.KitsCommandRegistry;
import dev.jpcode.kits.TomlConfig;
import dev.jpcode.kits.platform.services.IPlatformHelper;

import net.minecraft.commands.CommandSourceStack;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ForgePlatformHelper implements IPlatformHelper {
    final MLConfig config = new TomlConfig();

    private static boolean canManage(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context) {
        return player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
    }

    private static boolean canClaim(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context) {
        return player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
    }

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

    public static Map<String,PermissionNode<Boolean>> nodeMap = new HashMap<>();
    public static final PermissionDynamicContextKey<String> CONTEXT_KEY = new PermissionDynamicContextKey<>(String.class,"kit_name", Function.identity());
    public static PermissionNode<Boolean> MANAGE_NODE = new PermissionNode<>(Kits.MOD_ID, KitsCommandRegistry.MANAGE, PermissionTypes.BOOLEAN, ForgePlatformHelper::canManage);
    public static PermissionNode<Boolean> CLAIM_NODE = new PermissionNode<>(Kits.MOD_ID, KitsCommandRegistry.CLAIM, PermissionTypes.BOOLEAN, ForgePlatformHelper::canClaim,CONTEXT_KEY);

    @Override
    public boolean checkPermission(CommandSourceStack commandSourceStack, String key, int defaultV) {
        if (nodeMap.containsKey(key)) {
            return canUseCommand(commandSourceStack,nodeMap.get(key));
        } else {
            return commandSourceStack.hasPermission(defaultV);
        }
    }


    /**
     * Simple utility to catch exceptions with the test commands above.
     * Without that, the expected UnregisteredPermissionNode exception, triggers further exceptions and therefore isn't visible anymore.
     * This is only required to handle the intended error in the permission API, and should not be necessary with correct use.
     */
    private static boolean canUseCommand(CommandSourceStack src, PermissionNode<Boolean> booleanPermission, PermissionDynamicContext<?>... context)
    {
        try
        {
            return src.getEntity() != null && src.getEntity() instanceof ServerPlayer && PermissionAPI.getPermission((ServerPlayer) src.getEntity(), booleanPermission, context);
        } catch (Exception e)
        {
            Kits.LOGGER.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean canUseKit(CommandSourceStack commandSourceStack, String key) {
        return canUseCommand(commandSourceStack,CLAIM_NODE,CONTEXT_KEY.createContext(key) );
    }
}
