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

package org.terasology.world.block.family;

import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;


/**
 */
public abstract class AbstractBlockFamily implements BlockFamily {

    private BlockUri uri;

    protected AbstractBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
    }

    protected AbstractBlockFamily(BlockFamilyDefinition blockFamilyDefinition, BlockBuilderHelper blockBuilderHelper) {
    }


    protected void setBlockUri(BlockUri uri) {
        this.uri = uri;
    }


    @Override
    public BlockUri getURI() {
        return uri;
    }

    @Override
    public String getDisplayName() {
        return getArchetypeBlock().getDisplayName();
    }

    @Override
    public String toString() {
        return "BlockFamily[" + uri.toString() + "]";
    }
}
