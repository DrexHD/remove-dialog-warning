package me.drex.rdw.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.drex.rdw.RemoveDialogWarning.*;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @Inject(
        method = "handleCustomClickAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/ResourceLocation;Ljava/util/Optional;)V"
        ),
        cancellable = true
    )
    public void onHandleCustomClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        ResourceLocation id = packet.id();
        if (!id.equals(DIALOG_ACTION_ID)) return;
        if (!((Object) this instanceof ServerGamePacketListenerImpl game)) return;
        ServerPlayer player = game.player;
        packet.payload().flatMap(Tag::asCompound).ifPresent(root ->
            root.getString(COMMAND_KEY).ifPresent(command -> {
                Map<String, String> templateVariables = new HashMap<>();
                for (Map.Entry<String, Tag> child : root.entrySet()) {
                    String key = child.getKey();
                    Tag value = child.getValue();
                    if (key.equals(COMMAND_KEY) || key.equals(DYNAMIC_KEY)) continue;
                    templateVariables.put(key, value.toString());
                }

                boolean dynamic = root.getBooleanOr(DYNAMIC_KEY, false);
                Commands commands = server.getCommands();
                if (dynamic) {
                    StringTemplate parsedTemplate = StringTemplate.fromString(command);
                    List<String> list = parsedTemplate.variables().stream().map(string -> templateVariables.getOrDefault(string, "")).toList();
                    command = parsedTemplate.substitute(list);
                }

                commands.performPrefixedCommand(player.createCommandSourceStack(), command);
            }));
        ci.cancel();
    }
}
