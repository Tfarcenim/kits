package dev.jpcode.kits;

import dev.jpcode.kits.platform.MLConfig;

import net.minecraftforge.common.ForgeConfigSpec;

public class TomlConfig implements MLConfig {
    @Override
    public String starterKit() {
        return Server.starterKit.get();
    }

    @Override
    public boolean starterKitSetCooldown() {
        return Server.starterKitSetCooldown.get();
    }

    public static class Server {

        public static ForgeConfigSpec.BooleanValue starterKitSetCooldown;
        public static ForgeConfigSpec.ConfigValue<String> starterKit;

        public Server(ForgeConfigSpec.Builder builder) {
            starterKitSetCooldown = builder.define("starter_kit_set_cooldown",true);
            starterKit = builder.define("starter_kit","");
        }

    }

}
