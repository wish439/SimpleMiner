package com.wishtoday.ts.simpleminer.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.OptionalDouble;
import java.util.Set;

public class BlockPreviewRenderer {
    public static final RenderLayer LINES_NO_DEPTH = RenderLayer.of(
            "lines_no_depth",
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderLayer.LINES_PROGRAM)
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .writeMaskState(RenderPhase.ALL_MASK)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                    .build(false)
    );
    public static void render(Set<BlockPos> poses, WorldRenderContext context) {
        if (!SimpleminerClient.isPressing()) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        HitResult crosshairTarget = mc.crosshairTarget;
        if (crosshairTarget == null) {
            return;
        }
        if (crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ClientWorld world = mc.world;
        if (world == null) return;
        Camera camera = context.camera();
        Vec3d pos = camera.getPos();
        MatrixStack stack = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer buffer = consumers.getBuffer(LINES_NO_DEPTH);
        MatrixStack.Entry entry = stack.peek();
        for (BlockPos pose : poses) {
            BlockState blockState = world.getBlockState(pose);
            if (blockState.isAir()) continue;
            VoxelShape shape = blockState.getOutlineShape(world, pose, ShapeContext.of(camera.getFocusedEntity()));
            double v = pose.getX() - pos.getX();
            double u = pose.getZ() - pos.getZ();
            double h = pose.getY() - pos.getY();
            shape.forEachEdge((minX, minY
                    , minZ, maxX
                    , maxY, maxZ) -> {
                buffer.vertex(entry, (float) (minX + v), (float) (minY + h),(float) (minZ + u))
                        .color(1f, 1f, 1f, 1f)
                        .normal(entry, (float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ));
                buffer.vertex(entry, (float) (maxX + v), (float) (maxY + h),(float) (maxZ + u))
                        .color(1f, 1f, 1f, 1f)
                        .normal(entry, (float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ));
            });
        }

    }
}
