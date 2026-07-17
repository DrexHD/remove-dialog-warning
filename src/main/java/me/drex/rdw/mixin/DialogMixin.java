package me.drex.rdw.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import me.drex.rdw.RemoveDialogWarning;
import net.minecraft.server.dialog.Dialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(Dialog.class)
public interface DialogMixin {
    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static <A> Codec<Dialog> customCodec(
        Codec<Dialog> instance, Function<? super Dialog, ? extends A> type,
        Function<? super A, ? extends MapCodec<? extends Dialog>> codec, Operation<Codec<Dialog>> original
    ) {
        Codec<Dialog> origCodec = original.call(instance, type, codec);
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Dialog, T>> decode(DynamicOps<T> ops, T input) {
                return origCodec.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(Dialog input, DynamicOps<T> ops, T prefix) {
                return ScopedValue.where(RemoveDialogWarning.DIALOG_SCOPE, input).call(() ->
                    origCodec.encode(input, ops, prefix)
                );
            }
        };
    }
}
