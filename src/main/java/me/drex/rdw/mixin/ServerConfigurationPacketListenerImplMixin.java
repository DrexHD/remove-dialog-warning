package me.drex.rdw.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @WrapOperation(
        method = "handleSelectKnownPacks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/config/SynchronizeRegistriesTask;handleResponse(Ljava/util/List;Ljava/util/function/Consumer;)V"
        )
    )
    public void runWithPacketContext(SynchronizeRegistriesTask instance, List<KnownPack> list, Consumer<Packet<?>> consumer, Operation<Void> original) {
        PacketContext.runWithContext((PacketListener) this, () -> {
            original.call(instance, list, consumer);
        });
    }
}
