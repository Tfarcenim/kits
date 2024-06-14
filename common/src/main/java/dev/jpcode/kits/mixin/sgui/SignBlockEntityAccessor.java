package dev.jpcode.kits.mixin.sgui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {

    @Accessor
    void setFrontText(SignText frontText);

    @Accessor
    void setBackText(SignText backText);

}
