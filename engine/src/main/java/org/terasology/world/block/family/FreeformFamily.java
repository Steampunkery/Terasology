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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.*;

/**
 * A freeform family is a pseudo block family that can be combined with any block shape to produce an actual block
 * family.
 *
 */
@RegisterBlockFamily("freeform")
public class FreeformFamily extends AbstractBlockFamily{
    public BlockUri uri;
    public List<String> categories;
    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private  Block block = null;

    public FreeformFamily() {
    }


    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        if(block == null) {
            if (attachmentSide.isHorizontal()) {
                return blocks.get(attachmentSide);
            }
            if (direction != null) {
                return blocks.get(direction);
            } else {
                return blocks.get(Side.FRONT);
            }
        }
        return block;
    }

    @Override
    public Block getArchetypeBlock() {
        if(block == null) {
            return blocks.get(this.getArchetypeSide());
        }
        return block;
    }

    @Override
    public void registerFamily(BlockFamilyDefinition blockFamilyDefinition, BlockBuilderHelper blockBuilderHelper) {
        throw new UnsupportedOperationException("Shape expected");
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public void registerFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        BlockUri uri = null;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        if (shape.isCollisionYawSymmetric()) {
            block = blockBuilder.constructSimpleBlock(definition, shape);
            block.setBlockFamily(this);
            block.setUri(uri);
        } else {
            for (Rotation rot : Rotation.horizontalRotations()) {
                Side side = rot.rotate(Side.FRONT);
                Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot);
                if (block == null) {
                    throw new IllegalArgumentException("Missing block for side: " + side.toString());
                }
                block.setBlockFamily(this);
                block.setUri(new BlockUri(uri, new Name(side.name())));
                blocks.put(side, block);
            }
        }

        this.setBlockUri(uri);
        this.setCategory(definition.getCategories());
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if(block == null) {
            if (getURI().equals(blockUri.getFamilyUri())) {
                try {
                    Side side = Side.valueOf(blockUri.getIdentifier().toString().toUpperCase(Locale.ENGLISH));
                    return blocks.get(side);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return block;
    }

    @Override
    public Iterable<Block> getBlocks() {
        if(block == null) {
            return blocks.values();
        }
        return Arrays.asList(block);
    }

    @Override
    public boolean isFreeformSupported() {
        return true;
    }
}
