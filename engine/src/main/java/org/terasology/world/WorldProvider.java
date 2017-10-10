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
package org.terasology.world;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import org.terasology.world.block.Block;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.liquid.LiquidData;

/**
 * Provides the basic interface for all world providers.
 *
 */
public interface WorldProvider extends WorldProviderCore {

    /**
     * An active block is in a chunk that is available and fully generated.
     *
     * @param pos
     * @return Whether the given block is active
     */
    boolean isBlockRelevant(GridPoint3 pos);

    boolean isBlockRelevant(Vector3 pos);

    /**
     * @param pos
     * @param state    The new value of the liquid state
     * @param oldState The expected previous value of the liquid state
     * @return Whether the liquid change was made successfully. Will fail of oldState != the current state, or if the underlying chunk is not available
     */
    boolean setLiquid(GridPoint3 pos, LiquidData state, LiquidData oldState);

    /**
     * Returns the liquid state at the given position.
     *
     * @param blockPos
     * @return The state of the block
     */
    LiquidData getLiquid(GridPoint3 blockPos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(Vector3 pos);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    Block getBlock(GridPoint3 pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(Vector3 pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(Vector3 pos);

    byte getTotalLight(Vector3 pos);

    /**
     * Returns the light value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getLight(GridPoint3 pos);

    /**
     * Returns the sunlight value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    byte getSunlight(GridPoint3 pos);

    byte getTotalLight(GridPoint3 pos);

}
