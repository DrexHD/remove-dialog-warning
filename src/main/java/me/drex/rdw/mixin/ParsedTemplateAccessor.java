package me.drex.rdw.mixin;

import net.minecraft.server.dialog.action.ParsedTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParsedTemplate.class)
public interface ParsedTemplateAccessor {
    @Accessor
    String getRaw();
}
