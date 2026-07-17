package me.drex.rdw.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
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
            target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/Identifier;Ljava/util/Optional;)V"
        ),
        cancellable = true
    )
    public void onHandleCustomClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        Identifier id = packet.id();
        if (!id.equals(DIALOG_ACTION_ID)) return;
        if (!((Object) this instanceof ServerGamePacketListenerImpl game)) return;
        ServerPlayer player = game.player;
        packet.payload().flatMap(Tag::asCompound).ifPresent(root ->
            root.getString(COMMAND_KEY).ifPresent(command -> {
                Commands commands = server.getCommands();
                boolean dynamic = root.getBooleanOr(DYNAMIC_KEY, false);
                if (dynamic) {
                    Map<String, String> templateVariables = new HashMap<>();
                    for (Map.Entry<String, Tag> child : root.entrySet()) {
                        String key = child.getKey();
                        Tag value = child.getValue();
                        if (key.startsWith(MOD_ID + ":")) continue;

                        // We cannot use the raw tag values. Some values need special treatment
                        // Check ValueGetter.asTemplateSubstitution() vs ValueGetter.asTag()
                        String templateVariable = switch (value) {
                            case StringTag(String s) -> {
                                boolean requiresEscapeWithoutQuotes = root.getListOrEmpty(STRING_INPUT_KEY)
                                    .contains(StringTag.valueOf(key));
                                if (requiresEscapeWithoutQuotes) {
                                    yield StringTag.escapeWithoutQuotes(s);
                                }
                                yield s;
                            }
                            case ByteTag(byte b) -> {
                                String k = (b == 0) ? "false" : "true";
                                yield root.getCompoundOrEmpty(BOOLEAN_TAGS_KEY)
                                    .getCompoundOrEmpty(key)
                                    .getStringOr(k, k);
                            }
                            case FloatTag(float v) -> {
                                int intV = (int) v;
                                if ((float) intV == v) {
                                    yield Integer.toString(intV);
                                }
                                yield Float.toString(v);
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + value);
                        };

                        templateVariables.put(key, templateVariable);
                    }

                    StringTemplate parsedTemplate = StringTemplate.fromString(command);
                    List<String> list = parsedTemplate.variables().stream().map(string -> templateVariables.getOrDefault(string, "")).toList();
                    command = parsedTemplate.substitute(list);
                }

                commands.performPrefixedCommand(player.createCommandSourceStack(), command);
            }));
        ci.cancel();
    }
}
