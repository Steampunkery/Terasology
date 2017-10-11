/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks;

import com.badlogic.gdx.math.GridPoint3;
import org.terasology.math.Region3i;
import org.terasology.module.sandbox.API;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

/**
 * This interface describes the core of a chunk:
 * <ul>
 * <li>Chunk position</li>
 * <li>Block read/write</li>
 * <li>Liquid read/write</li>
 * <li>Chunk to world position conversion</li>
 * <li>Chunk size and region</li>
 * <li>Locking</li>
 * </ul>
 * This is everything available during chunk generation. Light is not included because it is a derived from the blocks.
 */
@API
public interface CoreChunk {
    GridPoint3 getPosition();

    Block getBlock(GridPoint3 pos);

    Block getBlock(int x, int y, int z);

    Block setBlock(int x, int y, int z, Block block);

    Block setBlock(GridPoint3 pos, Block block);

    Biome setBiome(int x, int y, int z, Biome biome);

    Biome getBiome(int x, int y, int z);

    void setLiquid(GridPoint3 pos, LiquidData state);

    void setLiquid(int x, int y, int z, LiquidData newState);

    LiquidData getLiquid(GridPoint3 pos);

    LiquidData getLiquid(int x, int y, int z);

    GridPoint3 getChunkWorldOffset();

    int getChunkWorldOffsetX();

    int getChunkWorldOffsetY();

    int getChunkWorldOffsetZ();

    GridPoint3 chunkToWorldPosition(GridPoint3 blockPos);

    GridPoint3 chunkToWorldPosition(int x, int y, int z);

    int chunkToWorldPositionX(int x);

    int chunkToWorldPositionY(int y);

    int chunkToWorldPositionZ(int z);

    int getChunkSizeX();

    int getChunkSizeY();

    int getChunkSizeZ();

    Region3i getRegion();

    int getEstimatedMemoryConsumptionInBytes();

    ChunkBlockIterator getBlockIterator();
}
