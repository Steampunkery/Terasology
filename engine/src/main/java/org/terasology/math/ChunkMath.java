/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.math;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.google.common.math.DoubleMath;
import org.terasology.world.chunks.ChunkConstants;

import java.math.RoundingMode;

/**
 * Collection of math functions.
 *
 */
public final class ChunkMath {

    private ChunkMath() {
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param x The X-coordinate of the block
     * @return The X-coordinate of the chunk
     */
    public static int calcChunkPosX(int x, int chunkPowerX) {
        return (x >> chunkPowerX);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param y The Y-coordinate of the block
     * @return The Y-coordinate of the chunk
     */
    public static int calcChunkPosY(int y, int chunkPowerY) {
        return (y >> chunkPowerY);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param z The Z-coordinate of the block
     * @return The Z-coordinate of the chunk
     */
    public static int calcChunkPosZ(int z, int chunkPowerZ) {
        return (z >> chunkPowerZ);
    }

    public static int calcChunkPosX(int x) {
        return calcChunkPosX(x, ChunkConstants.CHUNK_POWER.x);
    }
    public static int calcChunkPosY(int y) {
        return calcChunkPosY(y, ChunkConstants.CHUNK_POWER.y);
    }
    public static int calcChunkPosZ(int z) {
        return calcChunkPosZ(z, ChunkConstants.CHUNK_POWER.z);
    }

    public static GridPoint3 calcChunkPos(GridPoint3 pos, GridPoint3 chunkPower) {
        return calcChunkPos(pos.x, pos.y, pos.z, chunkPower);
    }

    public static GridPoint3 calcChunkPos(Vector3 pos) {
        return calcChunkPos(new GridPoint3(
                DoubleMath.roundToInt(pos.x, RoundingMode.HALF_UP),
                DoubleMath.roundToInt(pos.y, RoundingMode.HALF_UP),
                DoubleMath.roundToInt(pos.z, RoundingMode.HALF_UP)));
    }

    public static GridPoint3 calcChunkPos(GridPoint3 pos) {
        return calcChunkPos(pos.x, pos.y, pos.z);
    }

    public static GridPoint3 calcChunkPos(int x, int y, int z) {
        return calcChunkPos(x, y, z, ChunkConstants.CHUNK_POWER);
    }

    public static GridPoint3[] calcChunkPos(Region3i region) {
        return calcChunkPos(region, ChunkConstants.CHUNK_POWER);
    }

    public static GridPoint3 calcChunkPos(int x, int y, int z, GridPoint3 chunkPower) {
        return new GridPoint3(calcChunkPosX(x, chunkPower.x), calcChunkPosY(y, chunkPower.y), calcChunkPosZ(z, chunkPower.z));
    }

    public static GridPoint3[] calcChunkPos(Region3i region, GridPoint3 chunkPower) {
        int minX = calcChunkPosX(region.minX(), chunkPower.x);
        int minY = calcChunkPosY(region.minY(), chunkPower.y);
        int minZ = calcChunkPosZ(region.minZ(), chunkPower.z);

        int maxX = calcChunkPosX(region.maxX(), chunkPower.x);
        int maxY = calcChunkPosY(region.maxY(), chunkPower.y);
        int maxZ = calcChunkPosZ(region.maxZ(), chunkPower.z);

        int size = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);

        GridPoint3[] result = new GridPoint3[size];
        int index = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    result[index++] = new GridPoint3(x, y, z);
                }
            }
        }
        return result;
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param blockX The X-coordinate of the block in the world
     * @return The X-coordinate of the block within the chunk
     */
    public static int calcBlockPosX(int blockX, int chunkPosFilterX) {
        return blockX & chunkPosFilterX;
    }

    public static int calcBlockPosY(int blockY, int chunkPosFilterY) {
        return blockY & chunkPosFilterY;
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param blockZ The Z-coordinate of the block in the world
     * @return The Z-coordinate of the block within the chunk
     */
    public static int calcBlockPosZ(int blockZ, int chunkPosFilterZ) {
        return blockZ & chunkPosFilterZ;
    }

    public static int calcBlockPosX(int blockX) {
        return calcBlockPosX(blockX, ChunkConstants.INNER_CHUNK_POS_FILTER.x);
    }

    public static int calcBlockPosY(int blockY) {
        return calcBlockPosY(blockY, ChunkConstants.INNER_CHUNK_POS_FILTER.y);
    }

    public static int calcBlockPosZ(int blockZ) {
        return calcBlockPosZ(blockZ, ChunkConstants.INNER_CHUNK_POS_FILTER.z);
    }

    public static GridPoint3 calcBlockPos(GridPoint3 worldPos) {
        return calcBlockPos(worldPos.x, worldPos.y, worldPos.z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static GridPoint3 calcBlockPos(int x, int y, int z) {
        return calcBlockPos(x, y, z, ChunkConstants.INNER_CHUNK_POS_FILTER);
    }

    public static GridPoint3 calcBlockPos(int x, int y, int z, GridPoint3 chunkFilterSize) {
        return new GridPoint3(calcBlockPosX(x, chunkFilterSize.x), calcBlockPosY(y, chunkFilterSize.y), calcBlockPosZ(z, chunkFilterSize.z));
    }

    public static Region3i getChunkRegionAroundWorldPos(GridPoint3 pos, int extent) {
        GridPoint3 minPos = new GridPoint3(-extent, -extent, -extent);
        minPos.add(pos);
        GridPoint3 maxPos = new GridPoint3(extent, extent, extent);
        maxPos.add(pos);

        GridPoint3 minChunk = calcChunkPos(minPos);
        GridPoint3 maxChunk = calcChunkPos(maxPos);

        return Region3i.createFromMinMax(minChunk, maxChunk);
    }

    // TODO: This doesn't belong in this class, move it.
    public static Side getSecondaryPlacementDirection(Vector3 direction, Vector3 normal) {
        Side surfaceDir = Side.inDirection(normal);
        GridPoint3 attachDir = surfaceDir.reverse().getVector3i();
        Vector3 rawDirection = new Vector3(direction);
        float dot = rawDirection.dot(attachDir.x,attachDir.y,attachDir.z);
        rawDirection.sub(new Vector3(dot * attachDir.x, dot * attachDir.y, dot * attachDir.z));
        return Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z).reverse();
    }

    /**
     * Produces a region containing the region touching the side of the given region, both in and outside the region.
     *
     * @param region
     * @param side
     * @return
     */
    public static Region3i getEdgeRegion(Region3i region, Side side) {
        GridPoint3 sideDir = side.getVector3i();
        GridPoint3 min = region.min();
        GridPoint3 max = region.max();
        GridPoint3 edgeMin = new GridPoint3(min);
        GridPoint3 edgeMax = new GridPoint3(max);
        if (sideDir.x < 0) {
            edgeMin.x = min.x;
            edgeMax.x = min.x;
        } else if (sideDir.x > 0) {
            edgeMin.x = max.x;
            edgeMax.x = max.x;
        } else if (sideDir.y < 0) {
            edgeMin.y = min.y;
            edgeMax.y = min.y;
        } else if (sideDir.y > 0) {
            edgeMin.y = max.y;
            edgeMax.y = max.y;
        } else if (sideDir.z < 0) {
            edgeMin.z = min.z;
            edgeMax.z = min.z;
        } else if (sideDir.z > 0) {
            edgeMin.z = max.z;
            edgeMax.z = max.z;
        }
        return Region3i.createFromMinMax(edgeMin, edgeMax);
    }

    /**
     * Populates a target array with the minimum value adjacent to each location, including the location itself.
     * TODO: this is too specific for a general class like this. Move to a new class AbstractBatchPropagator
     *
     * @param source
     * @param target
     * @param populateMargins Whether to populate the edges of the target array
     */
    public static void populateMinAdjacent2D(int[] source, int[] target, int dimX, int dimY, boolean populateMargins) {
        System.arraycopy(source, 0, target, 0, target.length);

        // 0 < x < dimX - 1; 0 < y < dimY - 1
        for (int y = 1; y < dimY - 1; ++y) {
            for (int x = 1; x < dimX - 1; ++x) {
                target[x + y * dimX] = Math.min(Math.min(source[x + (y - 1) * dimX], source[x + (y + 1) * dimX]),
                        Math.min(source[x + 1 + y * dimX], source[x - 1 + y * dimX]));
            }
        }

        if (populateMargins) {
            // x == 0, y == 0
            target[0] = Math.min(source[1], source[dimX]);

            // 0 < x < dimX - 1, y == 0
            for (int x = 1; x < dimX - 1; ++x) {
                target[x] = Math.min(source[x - 1], Math.min(source[x + 1], source[x + dimX]));
            }

            // x == dimX - 1, y == 0
            target[dimX - 1] = Math.min(source[2 * dimX - 1], source[dimX - 2]);

            // 0 < y < dimY - 1
            for (int y = 1; y < dimY - 1; ++y) {
                // x == 0
                target[y * dimX] = Math.min(source[dimX * (y - 1)], Math.min(source[dimX * (y + 1)], source[1 + dimX * y]));
                // x == dimX - 1
                target[dimX - 1 + y * dimX] = Math.min(source[dimX - 1 + dimX * (y - 1)], Math.min(source[dimX - 1 + dimX * (y + 1)], source[dimX - 2 + dimX * y]));
            }
            // x == 0, y == dimY - 1
            target[dimX * (dimY - 1)] = Math.min(source[1 + dimX * (dimY - 1)], source[dimX * (dimY - 2)]);

            // 0 < x < dimX - 1; y == dimY - 1
            for (int x = 1; x < dimX - 1; ++x) {
                target[x + dimX * (dimY - 1)] = Math.min(source[x - 1 + dimX * (dimY - 1)], Math.min(source[x + 1 + dimX * (dimY - 1)], source[x + dimX * (dimY - 2)]));
            }

            // x == dimX - 1; y == dimY - 1
            target[dimX - 1 + dimX * (dimY - 1)] = Math.min(source[dimX - 2 + dimX * (dimY - 1)], source[dimX - 1 + dimX * (dimY - 2)]);
        }
    }
}
