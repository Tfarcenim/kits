package dev.jpcode.kits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.jpcode.kits.KitsMod.LOGGER;

import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin
{
    /**
     * Injects into the initialisation of the title screen and logs a message.
     *
     * @param info callback info
     */
    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info)
    {
        LOGGER.info("This is an example mixin - you've reached the title screen!");
    }
}
