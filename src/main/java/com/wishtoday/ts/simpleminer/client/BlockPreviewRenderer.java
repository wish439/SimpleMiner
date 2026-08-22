package com.wishtoday.ts.simpleminer.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.*;

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
        //List<Box> merge = SimpleBoxMerger.merge(new LongOpenHashSet(poses.stream().map(BlockPos::asLong).collect(Collectors.toSet())));
        //VoxelShape merged = merge(merge);
        VertexConsumer buffer = consumers.getBuffer(LINES_NO_DEPTH);
        MatrixStack.Entry entry = stack.peek();
        //stack.push();
        /*stack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        merged.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            buffer.vertex(entry, (float) (minX), (float) (minY),(float) (minZ))
                    .color(1f, 1f, 1f, 1f)
                    .normal(entry, (float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ));
            buffer.vertex(entry, (float) (maxX), (float) (maxY),(float) (maxZ))
                    .color(1f, 1f, 1f, 1f)
                    .normal(entry, (float) (maxX - minX), (float) (maxY - minY), (float) (maxZ - minZ));
        });
        stack.pop();*/
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

    /*public static void render(Set<BlockPos> poses, WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        HitResult crosshairTarget = mc.crosshairTarget;
        if (crosshairTarget == null || crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }
        ClientWorld world = mc.world;
        if (world == null) return;

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        MatrixStack stack = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        // 1. 合并 Box
        List<Box> boxes = SimpleBoxMerger.merge(
                new LongOpenHashSet(poses.stream().map(BlockPos::asLong).collect(Collectors.toSet()))
        );

        // 2. 矩阵整体平移到 -camera（相当于把世界坐标转成相机相对坐标）
        stack.push();
        stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        MatrixStack.Entry entry = stack.peek();

        // 3. 画每个 Box 的 12 条棱
        VertexConsumer buffer = consumers.getBuffer(LINES_NO_DEPTH);
        for (Box box : boxes) {
            float x1 = (float)box.minX, y1 = (float)box.minY, z1 = (float)box.minZ;
            float x2 = (float)box.maxX, y2 = (float)box.maxY, z2 = (float)box.maxZ;

            // 底面 (y = y1)
            drawLine(buffer, entry, x1,y1,z1, x2,y1,z1);
            drawLine(buffer, entry, x2,y1,z1, x2,y1,z2);
            drawLine(buffer, entry, x2,y1,z2, x1,y1,z2);
            drawLine(buffer, entry, x1,y1,z2, x1,y1,z1);

            // 顶面 (y = y2)
            drawLine(buffer, entry, x1,y2,z1, x2,y2,z1);
            drawLine(buffer, entry, x2,y2,z1, x2,y2,z2);
            drawLine(buffer, entry, x2,y2,z2, x1,y2,z2);
            drawLine(buffer, entry, x1,y2,z2, x1,y2,z1);

            // 垂直棱
            drawLine(buffer, entry, x1,y1,z1, x1,y2,z1);
            drawLine(buffer, entry, x2,y1,z1, x2,y2,z1);
            drawLine(buffer, entry, x2,y1,z2, x2,y2,z2);
            drawLine(buffer, entry, x1,y1,z2, x1,y2,z2);
        }

        stack.pop();
    }

    private static void drawLine(VertexConsumer buffer, MatrixStack.Entry entry, float x1, float y1, float z1, float x2, float y2, float z2) {
        buffer.vertex(entry, x1, y1, z1).color(1f, 1f, 1f, 1f).normal(entry, 0, 1, 0);
        buffer.vertex(entry, x2, y2, z2).color(1f, 1f, 1f, 1f).normal(entry, 0, 1, 0);
    }*/

    private static VoxelShape merge(List<Box> boxes) {
        Set<VoxelShape> shapes = new HashSet<>();
        for (Box box : boxes) {
            shapes.add(VoxelShapes.cuboid(box.expand(0.005D)));
        }

        VoxelShape fullCube = VoxelShapes.empty();

        for (VoxelShape shape : shapes) {
            fullCube = VoxelShapes.combine(fullCube, shape, BooleanBiFunction.OR);
        }
        return fullCube;
    }
}
