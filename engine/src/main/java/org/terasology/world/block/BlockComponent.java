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
package org.terasology.world.block;

import com.badlogic.gdx.math.GridPoint3;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * Used for entities representing a block in the world
 *
 */
public final class BlockComponent implements Component {
    @Replicate
    GridPoint3 position = new GridPoint3();
    @Replicate
    Block block;

    public BlockComponent() {
    }

    public BlockComponent(Block block, GridPoint3 pos) {
        this.block = block;
        this.position.set(pos);
    }

    public GridPoint3 getPosition() {
        return position;
    }

    public void setPosition(GridPoint3 pos) {
        position.set(pos);
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
