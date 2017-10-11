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

package org.terasology.world.internal;

import com.badlogic.gdx.math.GridPoint3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.liquid.LiquidData;

/**
 */
public class ChunkViewCoreImpl implements ChunkViewCore {

    private static final Logger logger = LoggerFactory.getLogger(ChunkViewCoreImpl.class);

    private GridPoint3 offset;
    private Region3i chunkRegion;
    private Region3i blockRegion;
    private Chunk[] chunks;

    private GridPoint3 chunkPower;
    private GridPoint3 chunkFilterSize;

    private Block defaultBlock;

    public ChunkViewCoreImpl(Chunk[] chunks, Region3i chunkRegion, GridPoint3 offset, Block defaultBlock) {
        this.chunkRegion = chunkRegion;
        this.chunks = chunks;
        this.offset = offset;
        setChunkSize(new GridPoint3(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z));
        this.defaultBlock = defaultBlock;
    }

    @Override
    public Region3i getWorldRegion() {
        return blockRegion;
    }

    @Override
    public Region3i getChunkRegion() {
        return chunkRegion;
    }

    @Override
    public Block getBlock(float x, float y, float z) {
        return getBlock(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public Block getBlock(GridPoint3 pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    // TODO: Review
    @Override
    public Block getBlock(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return defaultBlock;
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getBlock(
                ChunkMath.calcBlockPosX(blockX, chunkFilterSize.x),
                ChunkMath.calcBlockPosY(blockY, chunkFilterSize.y),
                ChunkMath.calcBlockPosZ(blockZ, chunkFilterSize.z));
    }

    @Override
    public Biome getBiome(float x, float y, float z) {
        return getBiome(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public Biome getBiome(GridPoint3 pos) {
        return getBiome(pos.x, pos.y, pos.z);
    }

    @Override
    public Biome getBiome(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return BiomeManager.getUnknownBiome();
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        GridPoint3 blockPos = ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize);
        return chunks[chunkIndex].getBiome(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public byte getSunlight(float x, float y, float z) {
        return getSunlight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public byte getSunlight(GridPoint3 pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(float x, float y, float z) {
        return getLight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public byte getLight(GridPoint3 pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getSunlight(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return 0;
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getSunlight(ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize));
    }

    @Override
    public byte getLight(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return 0;
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getLight(ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize));
    }

    @Override
    public void setBlock(GridPoint3 pos, Block type) {
        setBlock(pos.x, pos.y, pos.z, type);
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, Block type) {
        if (blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setBlock(ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), type);
        } else {
            logger.warn("Attempt to modify block outside of the view");
        }
    }

    @Override
    public void setBiome(GridPoint3 pos, Biome biome) {
        setBiome(pos.x, pos.y, pos.z, biome);
    }

    @Override
    public void setBiome(int blockX, int blockY, int blockZ, Biome biome) {
        if (blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            GridPoint3 pos = ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize);
            chunks[chunkIndex].setBiome(pos.x, pos.y, pos.z, biome);
        } else {
            logger.warn("Attempt to modify biome outside of the view");
        }
    }

    @Override
    public LiquidData getLiquid(GridPoint3 pos) {
        return getLiquid(pos.x, pos.y, pos.z);
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        if (!blockRegion.encompasses(x, y, z)) {
            return new LiquidData();
        }

        int chunkIndex = relChunkIndex(x, y, z);
        return chunks[chunkIndex].getLiquid(ChunkMath.calcBlockPos(x, y, z, chunkFilterSize));
    }

    @Override
    public void setLiquid(GridPoint3 pos, LiquidData newState) {
        setLiquid(pos.x, pos.y, pos.z, newState);
    }

    @Override
    public void setLiquid(int x, int y, int z, LiquidData newState) {
        if (blockRegion.encompasses(x, y, z)) {
            int chunkIndex = relChunkIndex(x, y, z);
            chunks[chunkIndex].setLiquid(ChunkMath.calcBlockPos(x, y, z, chunkFilterSize), newState);
        } else {
            throw new IllegalStateException("Attempted to modify liquid data though an unlocked view");
        }
    }

    @Override
    public void setLight(GridPoint3 pos, byte light) {
        setLight(pos.x, pos.y, pos.z, light);
    }

    @Override
    public void setLight(int blockX, int blockY, int blockZ, byte light) {
        if (blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setLight(ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), light);
        } else {
            logger.warn("Attempted to set light at a position not encompassed by the view");
        }
    }

    @Override
    public void setSunlight(GridPoint3 pos, byte light) {
        setSunlight(pos.x, pos.y, pos.z, light);
    }

    @Override
    public void setSunlight(int blockX, int blockY, int blockZ, byte light) {
        if (blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setSunlight(ChunkMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), light);
        } else {
            throw new IllegalStateException("Attempted to modify sunlight though an unlocked view");
        }
    }

    @Override
    public void setDirtyAround(GridPoint3 blockPos) {
        for (GridPoint3 pos : ChunkMath.getChunkRegionAroundWorldPos(blockPos, 1)) {
            chunks[pos.x + offset.x + chunkRegion.size().x * (pos.z + offset.z)].setDirty(true);
        }
    }

    @Override
    public void setDirtyAround(Region3i region) {
        GridPoint3 minPos = new GridPoint3(region.min());
        minPos.sub(1, 1, 1);
        GridPoint3 maxPos = new GridPoint3(region.max());
        maxPos.add(1, 1, 1);

        GridPoint3 minChunk = ChunkMath.calcChunkPos(minPos, chunkPower);
        GridPoint3 maxChunk = ChunkMath.calcChunkPos(maxPos, chunkPower);

        for (GridPoint3 pos : Region3i.createFromMinMax(minChunk, maxChunk)) {
            chunks[pos.x + offset.x + chunkRegion.size().x * (pos.z + offset.z)].setDirty(true);
        }
    }

    @Override
    public boolean isValidView() {
        for (Chunk chunk : chunks) {
            if (chunk.isDisposed()) {
                return false;
            }
        }
        return true;
    }

    protected int relChunkIndex(int x, int y, int z) {
        return TeraMath.calculate3DArrayIndex(ChunkMath.calcChunkPosX(x, chunkPower.x) + offset.x,
                ChunkMath.calcChunkPosY(y, chunkPower.y) + offset.y,
                ChunkMath.calcChunkPosZ(z, chunkPower.z) + offset.z, chunkRegion.size());
    }

    public void setChunkSize(GridPoint3 chunkSize) {
        this.chunkFilterSize = new GridPoint3(TeraMath.ceilPowerOfTwo(chunkSize.x) - 1, TeraMath.ceilPowerOfTwo(chunkSize.y) - 1, TeraMath.ceilPowerOfTwo(chunkSize.z) - 1);
        this.chunkPower = new GridPoint3(TeraMath.sizeOfPower(chunkSize.x), TeraMath.sizeOfPower(chunkSize.y), TeraMath.sizeOfPower(chunkSize.z));

        GridPoint3 blockMin = new GridPoint3(-offset.x * chunkSize.x,-offset.y * chunkSize.y,-offset.z * chunkSize.z);
        GridPoint3 blockSize = chunkRegion.size();
        blockSize.x *= chunkSize.x;
        blockSize.y *= chunkSize.y;
        blockSize.z *= chunkSize.z;
        this.blockRegion = Region3i.createFromMinAndSize(blockMin, blockSize);
    }

    @Override
    public GridPoint3 toWorldPos(GridPoint3 localPos) {
        return new GridPoint3(localPos.x + (offset.x + chunkRegion.min().x) * ChunkConstants.SIZE_X, localPos.y + (offset.y + chunkRegion.min().y) * ChunkConstants.SIZE_Y,
                localPos.z + (offset.z + chunkRegion.min().z) * ChunkConstants.SIZE_Z);
    }
}
