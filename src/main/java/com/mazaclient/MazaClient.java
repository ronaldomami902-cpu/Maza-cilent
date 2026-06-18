package com.mazaclient;

import com.mazaclient.config.MazaConfig;
import com.mazaclient.config.MazaMenuScreen;
import com.mazaclient.render.EspRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MazaClient implements ClientModInitializer {
    public static final String MOD_ID = "mazaclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MazaConfig CONFIG = new MazaConfig();
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Maza Client 1.0.0 yuklendi!");
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mazaclient.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.mazaclient"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MazaMenuScreen());
                }
            }
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            EspRenderer.render(context);
        });
    }
}
