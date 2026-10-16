package com.example.basehunter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class EspRenderer {

    public static void renderEsp(WorldRenderContext context) {
        if (!BaseHunterMod.baseEspEnabled && !BaseHunterMod.amethystEspEnabled) return;

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Vector3f lookVec = camera.getHorizontalPlane();
        Vec3d tracerStart = new Vec3d(
                cameraPos.x + lookVec.x() * 0.1,
                cameraPos.y + lookVec.y() * 0.1,
                cameraPos.z + lookVec.z() * 0.1
        );

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        VertexConsumerProvider.Immediate immediate = context.consumers() instanceof VertexConsumerProvider.Immediate imm
                ? imm
                : MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        VertexConsumer lines = immediate.getBuffer(RenderLayer.getLines());

        if (BaseHunterMod.baseEspEnabled) {
            for (BlockPos pos : BaseHunterMod.baseBlocks) {
                WorldRenderer.drawBox(matrices, lines, new Box(pos), 1.0f, 0.85f, 0.0f, 0.9f);
            }
            if (BaseHunterMod.tracersEnabled) {
                for (BlockCluster cluster : BaseHunterMod.baseClusters) {
                    drawTracer(matrices, lines, tracerStart, cluster.centroid, 1.0f, 0.85f, 0.0f, 0.9f);
                }
            }
        }

        if (BaseHunterMod.amethystEspEnabled) {
            for (BlockPos pos : BaseHunterMod.amethystBlocks) {
                WorldRenderer.drawBox(matrices, lines, new Box(pos), 0.8f, 0.2f, 1.0f, 0.9f);
            }
            if (BaseHunterMod.tracersEnabled) {
                for (BlockCluster cluster : BaseHunterMod.amethystClusters) {
                    drawTracer(matrices, lines, tracerStart, cluster.centroid, 0.8f, 0.2f, 1.0f, 0.9f);
                }
            }
        }

        immediate.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private static void drawTracer(MatrixStack matrices, VertexConsumer vertexConsumer,
                                   Vec3d start, Vec3d end,
                                   float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();

        float dx = (float) (end.x - start.x);
        float dy = (float) (end.y - start.y);
        float dz = (float) (end.z - start.z);
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length > 0.0001f) {
            dx /= length;
            dy /= length;
            dz /= length;
        } else {
            dx = 0.0f;
            dy = 1.0f;
            dz = 0.0f;
        }

        vertexConsumer.vertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
                .color(r, g, b, a)
                .normal(entry, dx, dy, dz);

        vertexConsumer.vertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
                .color(r, g, b, a)
                .normal(entry, dx, dy, dz);
    }
}