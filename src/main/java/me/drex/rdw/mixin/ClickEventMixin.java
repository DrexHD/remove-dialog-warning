package me.drex.rdw.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;

import static me.drex.rdw.RemoveDialogWarning.COMMAND_KEY;
import static me.drex.rdw.RemoveDialogWarning.DIALOG_ACTION_ID;

@Mixin(ClickEvent.class)
public interface ClickEventMixin {
    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static <A> Codec<ClickEvent> customCodec(
        Codec<ClickEvent> instance,
        String typeKey,
        Function<ClickEvent, ? extends A> type,
        Function<? super A, ? extends MapCodec<ClickEvent>> codec,
        Operation<Codec<ClickEvent>> original
    ) {
        return original.call(instance, typeKey, type, codec).xmap(Function.identity(), clickEvent -> {
            PacketContext packetContext = PacketContext.get();
            if (packetContext != null && packetContext.getClientConnection() != null) {
                if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString(COMMAND_KEY, command);
                    return new ClickEvent.Custom(DIALOG_ACTION_ID, Optional.of(tag));
                }
            }
            return clickEvent;
        });
    }
}