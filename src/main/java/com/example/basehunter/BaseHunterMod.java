package com.example.basehunter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseHunterMod implements ClientModInitializer {

    public static boolean baseEspEnabled = true;
    public static boolean amethystEspEnabled = true;
    public static boolean tracersEnabled = true;
    public static int minBaseChestCount = 1;

    public static final Set<BlockPos> baseBlocks = ConcurrentHashMap.newKeySet();
    public static final Set<BlockPos> amethystBlocks = ConcurrentHashMap.newKeySet();

    public static final List<BlockCluster> baseClusters = new CopyOnWriteArrayList<>();
    public static final List<BlockCluster> amethystClusters = new CopyOnWriteArrayList<>();

    private static KeyBinding guiKey;
    private int scanTickCounter = 0;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.basehunter.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.basehunter.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                client.setScreen(new HunterGuiScreen());
            }

            if (client.world != null && client.player != null) {
                scanTickCounter++;
                if (scanTickCounter >= 40) {
                    scanTickCounter = 0;
                    scanEnvironment(client);
                }
            }
        });

        WorldRenderEvents.LAST.register(EspRenderer::renderEsp);
    }

    private void scanEnvironment(MinecraftClient client) {
        baseBlocks.clear();
        amethystBlocks.clear();

        ChunkPos playerChunk = client.player.getChunkPos();
        int viewDist = client.options.getClampedViewDistance();

        for (int cx = playerChunk.x - viewDist; cx <= playerChunk.x + viewDist; cx++) {
            for (int cz = playerChunk.z - viewDist; cz <= playerChunk.z + viewDist; cz++) {
                WorldChunk chunk = client.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk != null) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity ||
                            be instanceof ShulkerBoxBlockEntity || be instanceof EnderChestBlockEntity ||
                            be instanceof FurnaceBlockEntity) {
                            baseBlocks.add(be.getPos());
                        }
                    }
                }
            }
        }

        BlockPos playerPos = client.player.getBlockPos();
        int radius = 48;
        for (BlockPos pos : BlockPos.iterate(
                playerPos.add(-radius, -radius, -radius),
                playerPos.add(radius, radius, radius))) {
            Block block = client.world.getBlockState(pos).getBlock();
            if (block == Blocks.AMETHYST_CLUSTER || block == Blocks.BUDDING_AMETHYST) {
                amethystBlocks.add(pos.toImmutable());
            }
        }

        baseClusters.clear();
        baseClusters.addAll(clusterBlocks(baseBlocks, 16.0, minBaseChestCount));

        amethystClusters.clear();
        amethystClusters.addAll(clusterBlocks(amethystBlocks, 12.0, 1));
    }

    private static List<BlockCluster> clusterBlocks(Set<BlockPos> positions, double maxDistance, int minSize) {
        List<BlockCluster> clusters = new ArrayList<>();
        Set<BlockPos> unvisited = new HashSet<>(positions);
        double maxDistSq = maxDistance * maxDistance;

        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            unvisited.remove(start);

            List<BlockPos> currentCluster = new ArrayList<>();
            Queue<BlockPos> queue = new ArrayDeque<>();

            queue.add(start);
            currentCluster.add(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                Iterator<BlockPos> it = unvisited.iterator();
                while (it.hasNext()) {
                    BlockPos candidate = it.next();
                    if (current.getSquaredDistance(candidate) <= maxDistSq) {
                        it.remove();
                        queue.add(candidate);
                        currentCluster.add(candidate);
                    }
                }
            }

            if (currentCluster.size() >= minSize) {
                clusters.add(new BlockCluster(currentCluster));
            }
        }
        return clusters;
    }
}