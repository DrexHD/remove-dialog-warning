package me.drex.rdw;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveDialogWarning implements ModInitializer {
	public static final String MOD_ID = "remove-dialog-warning";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String COMMAND_KEY = MOD_ID + ":command";
	public static final String DYNAMIC_KEY = MOD_ID + ":dynamic";
	public static final ResourceLocation DIALOG_ACTION_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "run_command");

	@Override
	public void onInitialize() {
	}
}
