package com.mazaclient.config;

import com.mazaclient.MazaClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MazaMenuScreen extends Screen {
    private final MazaConfig cfg = MazaClient.CONFIG;

    public MazaMenuScreen() {
        super(Text.literal("Maza Client"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 55, w = 200, h = 20, g = 23;

        addDrawableChild(ButtonWidget.builder(
            tog("§l[ESP] Ana Switch", cfg.espEnabled),
            btn -> { cfg.espEnabled = !cfg.espEnabled; btn.setMessage(tog("§l[ESP] Ana Switch", cfg.espEnabled)); }
        ).dimensions(cx - w/2, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§cRedstone Blok", cfg.redstoneEnabled),
            btn -> { cfg.redstoneEnabled = !cfg.redstoneEnabled; btn.setMessage(tog("§cRedstone Blok", cfg.redstoneEnabled)); }
        ).dimensions(cx - w - 4, y+g, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§eGozlemci", cfg.observerEnabled),
            btn -> { cfg.observerEnabled = !cfg.observerEnabled; btn.setMessage(tog("§eGozlemci", cfg.observerEnabled)); }
        ).dimensions(cx + 4, y+g, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§eFirlatici", cfg.dispenserEnabled),
            btn -> { cfg.dispenserEnabled = !cfg.dispenserEnabled; btn.setMessage(tog("§eFirlatici", cfg.dispenserEnabled)); }
        ).dimensions(cx - w - 4, y+g*2, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§9Cam", cfg.glassEnabled),
            btn -> { cfg.glassEnabled = !cfg.glassEnabled; btn.setMessage(tog("§9Cam", cfg.glassEnabled)); }
        ).dimensions(cx + 4, y+g*2, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§6Sandik", cfg.chestEnabled),
            btn -> { cfg.chestEnabled = !cfg.chestEnabled; btn.setMessage(tog("§6Sandik", cfg.chestEnabled)); }
        ).dimensions(cx - w - 4, y+g*3, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§6Shulker", cfg.shulkerEnabled),
            btn -> { cfg.shulkerEnabled = !cfg.shulkerEnabled; btn.setMessage(tog("§6Shulker", cfg.shulkerEnabled)); }
        ).dimensions(cx + 4, y+g*3, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§5Spawner", cfg.spawnerEnabled),
            btn -> { cfg.spawnerEnabled = !cfg.spawnerEnabled; btn.setMessage(tog("§5Spawner", cfg.spawnerEnabled)); }
        ).dimensions(cx - w - 4, y+g*4, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§aPiston", cfg.pistonEnabled),
            btn -> { cfg.pistonEnabled = !cfg.pistonEnabled; btn.setMessage(tog("§aPiston", cfg.pistonEnabled)); }
        ).dimensions(cx + 4, y+g*4, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            tog("§aChunk Bosluk", cfg.chunkVoidEnabled),
            btn -> { cfg.chunkVoidEnabled = !cfg.chunkVoidEnabled; btn.setMessage(tog("§aChunk Bosluk", cfg.chunkVoidEnabled)); }
        ).dimensions(cx - w/2, y+g*5, w, h).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("§cKapat"),
            btn -> this.close()
        ).dimensions(cx - 55, y+g*6+5, 110, h).build());
    }

    private Text tog(String name, boolean on) {
        return Text.literal(name + (on ? " §a[AC]" : " §c[KAPA]"));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.renderBackground(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§6§lMAZA CLIENT"), width/2, 15, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§7Y: §e" + cfg.yMin + " §7~ §e" + cfg.yMax), width/2, 30, 0xFFFFFF);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
                     }
