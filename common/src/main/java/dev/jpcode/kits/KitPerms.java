package dev.jpcode.kits;

import dev.jpcode.kits.platform.Services;

import net.minecraft.commands.CommandSourceStack;

public final class KitPerms {

    private KitPerms() {}

    static void init() {
    }

    public static boolean checkKit(CommandSourceStack source, String kitName) {
        return Services.PLATFORM.checkPermission(source, "kits.claim." + kitName, 4);
    }

}
