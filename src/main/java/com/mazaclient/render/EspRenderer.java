package com.mazaclient.render;

import com.mazaclient.MazaClient;
import com.mazaclient.config.MazaConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EspRenderer {
    private static final List<BlockPos> foundBlocks = new CopyOnWriteArrayList<>();
    private static final List<long[]> voidChunks = new CopyOnWriteArrayList<>();
    private static long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 2000;
    private static BlockPos lastPlayerPos = null;

    public static void render(WorldRenderContext context) {
        MazaConfig cfg = MazaClient.CONFIG;
        if (!cfg.espEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        BlockPos playerPos = mc.player.getBlockPos();
        boolean timeElapsed = (now - lastScanTime) >= SCAN_INTERVAL_MS;
        boolean movedFar = lastPlayerPos == null || lastPlayerPos.getManhattanDistance(playerPos) > 16;

        if (timeElapsed || movedFar) {
            scanBlocks(mc, playerPos, cfg);
            lastScanTime = now;
            lastPlayerPos = playerPos;
        }

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = context.camera().getPos();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.5f);

        Tessellator tess = Tessellator.getInstance();

        // ESP Outline
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        for (BlockPos pos : foundBlocks) {
            if (!mc.world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;
            float[] color = getColor(mc.world.getBlockState(pos).getBlock(), cfg);
            if (color != null) drawBox(matrices, buf, pos, color);
        }
        if (cfg.chunkVoidEnabled) {
            for (long[] chunk : voidChunks) {
                drawChunk(matrices, buf, (int)chunk[0]*16, (int)chunk[1]*16,
                    cfg.yMin, cfg.yMax, cfg.chunkVoidColor);
            }
        }
        try {
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } catch (Exception ignored) {}

        // Tracers
        if (cfg.tracersEnabled) {
            RenderSystem.lineWidth(1.5f);
            BufferBuilder tracerBuf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            Vec3d start = new Vec3d(camPos.x, camPos.y, camPos.z);
            for (BlockPos pos : foundBlocks) {
                if (!mc.world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;
                float[] color = getColor(mc.world.getBlockState(pos).getBlock(), cfg);
                if (color == null) continue;
                float tx = pos.getX() + 0.5f;
                float ty = pos.getY() + 0.5f;
                float tz = pos.getZ() + 0.5f;
                Matrix4f mx = matrices.peek().getPositionMatrix();
                float dx = tx-(float)start.x, dy = ty-(float)start.y, dz = tz-(float)start.z;
                float len = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
                if (len == 0) continue;
                tracerBuf.vertex(mx, (float)start.x, (float)start.y, (float)start.z)
                    .color(color[0], color[1], color[2], 0.5f).normal(dx/len, dy/len, dz/len);
                tracerBuf.vertex(mx, tx, ty, tz)
                    .color(color[0], color[1], color[2], 0.5f).normal(dx/len, dy/len, dz/len);
            }
            try {
                BuiltBuffer built = tracerBuf.endNullable();
                if (built != null) BufferRenderer.drawWithGlobalProgram(built);
            } catch (Exception ignored) {}
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static void scanBlocks(MinecraftClient mc, BlockPos playerPos, MazaConfig cfg) {
        foundBlocks.clear();
        voidChunks.clear();
        int range = 48;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = cfg.yMin; y <= cfg.yMax; y++) {
                    BlockPos pos = new BlockPos(playerPos.getX()+x, y, playerPos.getZ()+z);
                    if (!mc.world.isChunkLoaded(pos.getX()>>4, pos.getZ()>>4)) continue;
                    if (getColor(mc.world.getBlockState(pos).getBlock(), cfg) != null)
                        foundBlocks.add(pos.toImmutable());
                }
            }
        }
        if (cfg.chunkVoidEnabled) {
            int cx = playerPos.getX()>>4, cz = playerPos.getZ()>>4;
            for (int dcx = -4; dcx <= 4; dcx++) {
                for (int dcz = -4; dcz <= 4; dcz++) {
                    int chunkX = cx+dcx, chunkZ = cz+dcz;
                    if (!mc.world.isChunkLoaded(chunkX, chunkZ)) continue;
                    int air = 0;
                    for (int bx = 0; bx < 16; bx++)
                        for (int bz = 0; bz < 16; bz++)
                            for (int by = cfg.yMin; by <= cfg.yMax; by++)
                                if (mc.world.getBlockState(new BlockPos(chunkX*16+bx, by, chunkZ*16+bz)).isAir()) air++;
                    if (air >= cfg.voidThreshold) voidChunks.add(new long[]{chunkX, chunkZ});
                }
            }
        }
    }

    private static float[] getColor(Block b, MazaConfig c) {
        if (c.redstoneEnabled && b == Blocks.REDSTONE_BLOCK) return c.redstoneColor;
        if (c.observerEnabled && b == Blocks.OBSERVER) return c.observerColor;
        if (c.dispenserEnabled && b == Blocks.DISPENSER) return c.dispenserColor;
        if (c.pistonEnabled && (b == Blocks.PISTON || b == Blocks.STICKY_PISTON)) return c.pistonColor;
        if (c.glassEnabled && (b == Blocks.GLASS || b == Blocks.GLASS_PANE
            || b instanceof StainedGlassBlock || b instanceof StainedGlassPaneBlock)) return c.glassColor;
        if (c.chestEnabled && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST
            || b == Blocks.ENDER_CHEST)) return c.chestColor;
        if (c.shulkerEnabled && b instanceof ShulkerBoxBlock) return c.shulkerColor;
        if (c.spawnerEnabled && (b == Blocks.SPAWNER || b == Blocks.TRIAL_SPAWNER)) return c.spawnerColor;
        return null;
    }

    private static void drawBox(MatrixStack m, BufferBuilder b, BlockPos p, float[] c) {
        float x=p.getX(),y=p.getY(),z=p.getZ(),r=c[0],g=c[1],bl=c[2],a=c[3];
        Matrix4f mx=m.peek().getPositionMatrix();
        ln(b,mx,x,y,z,x+1,y,z,r,g,bl,a); ln(b,mx,x+1,y,z,x+1,y,z+1,r,g,bl,a);
        ln(b,mx,x+1,y,z+1,x,y,z+1,r,g,bl,a); ln(b,mx,x,y,z+1,x,y,z,r,g,bl,a);
        ln(b,mx,x,y+1,z,x+1,y+1,z,r,g,bl,a); ln(b,mx,x+1,y+1,z,x+1,y+1,z+1,r,g,bl,a);
        ln(b,mx,x+1,y+1,z+1,x,y+1,z+1,r,g,bl,a); ln(b,mx,x,y+1,z+1,x,y+1,z,r,g,bl,a);
        ln(b,mx,x,y,z,x,y+1,z,r,g,bl,a); ln(b,mx,x+1,y,z,x+1,y+1,z,r,g,bl,a);
        ln(b,mx,x+1,y,z+1,x+1,y+1,z+1,r,g,bl,a); ln(b,mx,x,y,z+1,x,y+1,z+1,r,g,bl,a);
    }

    private static void drawChunk(MatrixStack m, BufferBuilder b, int sx, int sz, int y0, int y1, float[] c) {
        float r=c[0],g=c[1],bl=c[2],a=c[3];
        Matrix4f mx=m.peek().getPositionMatrix();
        float x0=sx,x1=sx+16,z0=sz,z1=sz+16;
        ln(b,mx,x0,y0,z0,x1,y0,z0,r,g,bl,a); ln(b,mx,x1,y0,z0,x1,y0,z1,r,g,bl,a);
        ln(b,mx,x1,y0,z1,x0,y0,z1,r,g,bl,a); ln(b,mx,x0,y0,z1,x0,y0,z0,r,g,bl,a);
        ln(b,mx,x0,y1,z0,x1,y1,z0,r,g,bl,a); ln(b,mx,x1,y1,z0,x1,y1,z1,r,g,bl,a);
        ln(b,mx,x1,y1,z1,x0,y1,z1,r,g,bl,a); ln(b,mx,x0,y1,z1,x0,y1,z0,r,g,bl,a);
        ln(b,mx,x0,y0,z0,x0,y1,z0,r,g,bl,a); ln(b,mx,x1,y0,z0,x1,y1,z0,r,g,bl,a);
        ln(b,mx,x1,y0,z1,x1,y1,z1,r,g,bl,a); ln(b,mx,x0,y0,z1,x0,y1,z1,r,g,bl,a);
    }

    private static void ln(BufferBuilder b, Matrix4f m, float x1,float y1,float z1,float x2,float y2,float z2,float r,float g,float bl,float a) {
        float dx=x2-x1,dy=y2-y1,dz=z2-z1,len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(len==0) return;
        b.vertex(m,x1,y1,z1).color(r,g,bl,a).normal(dx/len,dy/len,dz/len);
        b.vertex(m,x2,y2,z2).color(r,g,bl,a).normal(dx/len,dy/len,dz/len);
    }
                                    }
