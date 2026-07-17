package me.drex.rdw;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ScopedValue;

public class RemoveDialogWarning implements ModInitializer {
    public static final String MOD_ID = "remove-dialog-warning";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String COMMAND_KEY = MOD_ID + ":command";
    public static final String DYNAMIC_KEY = MOD_ID + ":dynamic";
    public static final String BOOLEAN_TAGS_KEY = MOD_ID + ":boolean_input";
    public static final String STRING_INPUT_KEY = MOD_ID + ":string_input";
    public static final Identifier DIALOG_ACTION_ID = Identifier.fromNamespaceAndPath(MOD_ID, "run_command");
    public static final ScopedValue<Dialog> DIALOG_SCOPE = ScopedValue.newInstance();

    @Override
    public void onInitialize() {
    }
}
