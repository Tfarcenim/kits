package dev.jpcode.kits.config;

import dev.jpcode.kits.Kits;

import dev.jpcode.kits.platform.MLConfig;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Kits.MOD_ID)
public class KitsClothConfig implements ConfigData, MLConfig {
    //server
    public boolean starterKitCooldown = true;
    public String starterkit = "";


    @Override
    public String starterKit() {
        return starterkit;
    }

    @Override
    public boolean starterKitSetCooldown() {
        return starterKitCooldown;
    }
}
