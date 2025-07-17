package me.drex.rdw.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.action.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;

import static me.drex.rdw.RemoveDialogWarning.*;

@Mixin(Action.class)
public interface ActionMixin {
    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static <A> Codec<Action> customCodec(
        Codec<Action> instance,
        Function<Action, ? extends A> type,
        Function<? super A, ? extends MapCodec<Action>> codec,
        Operation<Codec<Action>> original
    ) {
        return original.call(instance, type, codec).xmap(Function.identity(), action -> {
            PacketContext packetContext = PacketContext.get();
            if (packetContext != null && packetContext.getClientConnection() != null) {
                if (action instanceof CommandTemplate(ParsedTemplate template)) {
                    ParsedTemplateAccessor accessor = (ParsedTemplateAccessor) template;
                    CompoundTag tag = new CompoundTag();
                    tag.putString(COMMAND_KEY, accessor.getRaw());
                    tag.putBoolean(DYNAMIC_KEY, true);
                    return new CustomAll(DIALOG_ACTION_ID, Optional.of(tag));
                } else if (action instanceof StaticAction(ClickEvent value)) {
                    if (value instanceof ClickEvent.RunCommand(String command)) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString(COMMAND_KEY, command);
                        return new CustomAll(DIALOG_ACTION_ID, Optional.of(tag));
                    }
                }
            }
            return action;
        });
    }
}
