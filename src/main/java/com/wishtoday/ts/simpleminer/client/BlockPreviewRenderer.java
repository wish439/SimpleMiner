/*MIT License
*
*Copyright (c) 2024 Kaf
*
* Modified by iamkaf
* https://github.com/iamkaf/liteminer
*
*Permission is hereby granted, free of charge, to any person obtaining a copy
*of this software and associated documentation files (the "Software"), to deal
*in the Software without restriction, including without limitation the rights
*to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
*copies of the Software, and to permit persons to whom the Software is
*furnished to do so, subject to the following conditions:
*
*The above copyright notice and this permission notice shall be included in all
*copies or substantial portions of the Software.
*
*THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
*IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
*FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
*AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
*LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
*OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
*SOFTWARE.
* */


package com.wishtoday.ts.simpleminer.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.wishtoday.ts.simpleminer.client.SimpleminerClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;

public class BlockPreviewRenderer {
    private static final RenderLayer LINES_NORMAL = RenderLayer.of(
            "liteminer_lines_normal",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.DEBUG_LINES,
            256,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(GameRenderer::getPositionColorProgram))
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .layering(RenderPhase.NO_LAYERING)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .build(false)
    );

    private static final RenderLayer LINES_TRANSPARENT = RenderLayer.of(
            "liteminer_lines_transparent",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.DEBUG_LINES,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(GameRenderer::getPositionColorProgram))
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .layering(RenderPhase.NO_LAYERING)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .writeMaskState(RenderPhase.ALL_MASK)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                    .build(false)
    );

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

    public static boolean renderHighlight(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null || mc.player == null) {
            return true;
        }

        HitResult result = mc.crosshairTarget;
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return true;
        }

        BlockHitResult hitResult = (BlockHitResult) result;
        BlockPos origin = hitResult.getBlockPos();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();


        matrices.push();

        matrices.translate(
                origin.getX() - cameraPos.x,
                origin.getY() - cameraPos.y,
                origin.getZ() - cameraPos.z
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Collection<VoxelShape> shapes = new HashSet<>();

        for (var aabb : mergeBoundingBoxes(SimpleminerClient.getRenderBlocks(), origin)) {
            shapes.add(VoxelShapes.cuboid(aabb.contract(-0.005D)));
        }

        VertexConsumerProvider.Immediate buffers =
                mc.getBufferBuilders().getEntityVertexConsumers();

        VertexConsumer transparentBuilder =
                buffers.getBuffer(LINES_TRANSPARENT);

        orShapes(shapes).forEachEdge((x1, y1, z1, x2, y2, z2) -> {

            double dx = x2 - x1;
            double dy = y2 - y1;
            double dz = z2 - z1;

            double invMag =
                    1.0 / Math.sqrt(dx * dx + dy * dy + dz * dz);

            float nx = (float) (dx * invMag);
            float ny = (float) (dy * invMag);
            float nz = (float) (dz * invMag);

            matrices.peek();

            transparentBuilder.vertex(matrix, (float) x1, (float) y1, (float) z1)
                    .color(10, 206, 245, 180)
                    .normal(nx, ny, nz);

            transparentBuilder.vertex(matrix, (float) x2, (float) y2, (float) z2)
                    .color(10, 206, 245, 180)
                    .normal(nx, ny, nz);
        });

        buffers.draw(LINES_TRANSPARENT);

        VertexConsumer normalBuilder =
                buffers.getBuffer(LINES_NORMAL);

        orShapes(shapes).forEachEdge((x1, y1, z1, x2, y2, z2) -> {

            normalBuilder.vertex(matrix, (float) x1, (float) y1, (float) z1)
                    .color(1f, 1f, 1f, 1f);

            normalBuilder.vertex(matrix, (float) x2, (float) y2, (float) z2)
                    .color(1f, 1f, 1f, 1f);
        });

        buffers.draw(LINES_NORMAL);

        matrices.pop();

        return false;
    }

    static VoxelShape orShapes(Collection<VoxelShape> shapes) {
        VoxelShape combinedShape = VoxelShapes.empty();

        for (VoxelShape shape : shapes) {
            combinedShape =
                    VoxelShapes.combine(
                            combinedShape,
                            shape,
                            BooleanBiFunction.OR
                    );
        }

        return combinedShape;
    }

    private static Collection<Box> mergeBoundingBoxes(Collection<BlockPos> positions, BlockPos referencePoint) {
        BoundingBoxMerger boxMerger = new BoundingBoxMerger();

        positions.stream()
                .map(pos -> relativeBlockPos(pos, referencePoint))
                .sorted()
                .map(Box::new)
                .forEachOrdered(box -> {
                    // Reset current bounds if we encounter a new x or y coordinate
                    if (boxMerger.xCoordTracker != aabbMinX(box) || boxMerger.yCoordTracker != aabbMinY(box)) {
                        boxMerger.currentBounds = null;
                    }

                    boxMerger.xCoordTracker = aabbMinX(box);
                    boxMerger.yCoordTracker = aabbMinY(box);

                    Vec3d center = aabbCenter(box);
                    boxMerger.currentCenter = center;

                    // Attempt to combine with the current bounds or adjacent boxes
                    if (boxMerger.currentBounds != null && boxMerger.canCombine(
                            boxMerger.currentBounds,
                            box,
                            center
                    )) {
                        return;
                    }

                    if (boxMerger.tryCombineAdjacent(center, box)) {
                        return;
                    }

                    // Store as a new bounding box
                    boxMerger.currentBounds = box;
                    boxMerger.positionToBox.put(center, box);
                    boxMerger.boxToPosition.put(box, center);
                });

        return boxMerger.boxToPosition.keySet();
    }

    private static double vecX(Vec3d vector) {
        return vector.x;
    }

    private static int blockX(BlockPos position) {
        return position.getX();
    }

    private static int blockY(BlockPos position) {
        return position.getY();
    }

    private static int blockZ(BlockPos position) {
        return position.getZ();
    }

    private static double vecY(Vec3d vector) {
        return vector.y;
    }

    private static double vecZ(Vec3d vector) {
        return vector.z;
    }

    private static Vec3d blockCenter(BlockPos position) {
        return new Vec3d(blockX(position) + 0.5D, blockY(position) + 0.5D, blockZ(position) + 0.5D);
    }

    private static BlockPos relativeBlockPos(BlockPos position, BlockPos referencePoint) {
        return new BlockPos(
                blockX(position) - blockX(referencePoint),
                blockY(position) - blockY(referencePoint),
                blockZ(position) - blockZ(referencePoint)
        );
    }


    private static Vec3d aabbCenter(Box box) {
        return new Vec3d(
                (aabbMinX(box) + aabbMaxX(box)) / 2.0D,
                (aabbMinY(box) + aabbMaxY(box)) / 2.0D,
                (aabbMinZ(box) + aabbMaxZ(box)) / 2.0D
        );
    }

    private static Vec3d subtractVectors(Vec3d first, Vec3d second) {
        return new Vec3d(vecX(first) - vecX(second), vecY(first) - vecY(second), vecZ(first) - vecZ(second));
    }

    private static Vec3d addVectors(Vec3d first, Vec3d second) {
        return new Vec3d(vecX(first) + vecX(second), vecY(first) + vecY(second), vecZ(first) + vecZ(second));
    }

    private static Box mergeAabbs(Box first, Box second) {
        return new Box(
                Math.min(aabbMinX(first), aabbMinX(second)),
                Math.min(aabbMinY(first), aabbMinY(second)),
                Math.min(aabbMinZ(first), aabbMinZ(second)),
                Math.max(aabbMaxX(first), aabbMaxX(second)),
                Math.max(aabbMaxY(first), aabbMaxY(second)),
                Math.max(aabbMaxZ(first), aabbMaxZ(second))
        );
    }

    private static double aabbMinX(Box box) {
        return box.minX;
    }

    private static double aabbMinY(Box box) {
        return box.minY;
    }

    private static double aabbMinZ(Box box) {
        return box.minZ;
    }

    private static double aabbMaxX(Box box) {
        return box.maxX;
    }

    private static double aabbMaxY(Box box) {
        return box.maxY;
    }

    private static double aabbMaxZ(Box box) {
        return box.maxZ;
    }

    private static String vectorKey(Vec3i vector) {
        return vectorKey(vec3iX(vector), vec3iY(vector), vec3iZ(vector));
    }

    private static String vectorKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static Vec3d directionVector(Direction direction) {
        Vec3i normal = directionNormal(direction);
        return new Vec3d(vec3iX(normal), vec3iY(normal), vec3iZ(normal));
    }

    private static int vec3iX(Vec3i vector) {
        return vector.getX();
    }

    private static int vec3iY(Vec3i vector) {
        return vector.getY();
    }

    private static int vec3iZ(Vec3i vector) {
        return vector.getZ();
    }

    private static Vec3i directionNormal(Direction direction) {
        return direction.getVector();
    }

    private static Direction directionOpposite(Direction direction) {
        return direction.getOpposite();
    }

    private static Direction.Axis directionAxis(Direction direction) {
        return direction.getAxis();
    }

    private static Direction.AxisDirection directionAxisDirection(Direction direction) {
        return direction.getDirection();
    }

    private static final class BoundingBoxMerger {
        private static final Map<String, Direction> DIRECTION_LOOKUP = Arrays.stream(Direction.values())
                .collect(Collectors.toMap(dir -> vectorKey(directionNormal(dir)),
                        dir -> dir,
                        (a, b) -> {
                            throw new IllegalStateException("Duplicate direction detected.");
                        },
                        HashMap::new
                ));

        private final Map<Vec3d, Box> positionToBox = new HashMap<>();
        private final Multimap<Box, Vec3d> boxToPosition = HashMultimap.create();
        private double xCoordTracker = Double.NEGATIVE_INFINITY;
        private double yCoordTracker = Double.NEGATIVE_INFINITY;
        private Vec3d currentCenter = null;
        private Box currentBounds = null;

        /**
         * Determines if two bounding boxes are aligned along a given direction.
         */
        private static boolean isAligned(Box first, Box second, Direction direction) {
            return getAxisValue(first, direction) == getAxisValue(
                    second,
                    directionOpposite(direction)
            ) && Arrays.stream(Direction.values())
                    .filter(d -> directionAxis(d) != directionAxis(direction))
                    .allMatch(d -> getAxisValue(first, d) == getAxisValue(second, d));
        }

        /**
         * Retrieves the value of a bounding box along a specified direction.
         */
        private static double getAxisValue(Box box, Direction direction) {
            Direction.Axis axis = directionAxis(direction);
            boolean positive = directionAxisDirection(direction) == Direction.AxisDirection.POSITIVE;
            return switch (axis) {
                case X -> positive ? aabbMaxX(box) : aabbMinX(box);
                case Y -> positive ? aabbMaxY(box) : aabbMinY(box);
                case Z -> positive ? aabbMaxZ(box) : aabbMinZ(box);
                default -> throw new IllegalStateException("Unknown direction axis: " + axis);
            };
        }

        /**
         * Converts a vector into a direction based on its coordinates.
         */
        private static Direction directionFromVector(Vec3d vector) {
            return DIRECTION_LOOKUP.get(vectorKey((int) vecX(vector), (int) vecY(vector), (int) vecZ(vector)));
        }

        /**
         * Attempts to merge the current bounding box with its neighboring bounding box.
         */
        private boolean canCombine(Box current, Box neighbor, Vec3d center) {
            Direction direction = directionFromVector(subtractVectors(center, aabbCenter(current)));
            return direction != null && isAligned(current, neighbor, direction) && mergeBoxes(
                    current,
                    neighbor,
                    center
            );
        }

        /**
         * Attempts to merge the provided bounding box with any neighboring bounding boxes.
         */
        private boolean tryCombineAdjacent(Vec3d center, Box box) {
            for (Direction direction : Direction.values()) {
                Vec3d adjacentCenter = addVectors(center, directionVector(direction));
                Box adjacentBox = positionToBox.get(adjacentCenter);

                if (adjacentBox != null && isAligned(box, adjacentBox, direction)) {
                    return mergeBoxes(adjacentBox, box, center);
                }
            }
            return false;
        }

        /**
         * Merges two bounding boxes and updates the necessary mappings.
         */
        private boolean mergeBoxes(Box source, Box target, Vec3d center) {
            Box expanded = mergeAabbs(source, target);

            Set<Vec3d> mergedPositions = new HashSet<>(boxToPosition.removeAll(source));
            mergedPositions.forEach(v -> positionToBox.put(v, expanded));

            boxToPosition.putAll(expanded, mergedPositions);
            positionToBox.put(center, expanded);
            boxToPosition.put(expanded, center);

            currentBounds = expanded;
            return true;
        }
    }
}