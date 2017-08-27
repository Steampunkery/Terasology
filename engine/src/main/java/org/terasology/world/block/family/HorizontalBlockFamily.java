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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 *
 */
@RegisterBlockFamily("horizontal")
public class HorizontalBlockFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {

    private static final ImmutableSet<String> BLOCK_NAMES = ImmutableSet.of("front", "left", "right", "back", "top", "bottom");
    private static final ImmutableList<MultiSection> MULTI_SECTIONS = ImmutableList.of(
            new MultiSection("all", "front", "left", "right", "back", "top", "bottom"),
            new MultiSection("topBottom", "top", "bottom"),
            new MultiSection("sides", "front", "left", "right", "back"));


    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);


    /**
     * @param uri        The asset uri for the block group.
     * @param blocks     The set of blocks that make up the group. Front, Back, Left and Right must be provided - the rest is ignored.
     * @param categories The set of categories this block family belongs to
     */
    public HorizontalBlockFamily(){
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public void registerFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        if (!definition.isFreeform()) {
            throw new IllegalStateException("A shape cannot be provided when creating a family for a non-freeform block family definition");
        }
        BlockUri uri = null;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri,new Name(side.name())));
            blocks.put(side, block);
        }
        this.setBlockUri(uri);
        this.setCategory(definition.getCategories());
    }


    @Override
    public void registerFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        if (definition.isFreeform()) {
            throw new IllegalStateException("A shape must be provided when creating a family for a freeform block family definition");
        }
        BlockUri uri = new BlockUri(definition.getUrn());
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);

            Block block =  blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri,new Name(side.name())));
            blocks.put(side,block);
        }
        this.setCategory(definition.getCategories());
        this.setBlockUri(uri);

    }

    @Override
    public Set<String> getSectionNames() {
        return BLOCK_NAMES;
    }

    @Override
    public ImmutableList<MultiSection> getMultiSections() {
        return MULTI_SECTIONS;
    }

    @Override
    public boolean isFreeformSupported() {
        return false;
    }



    //    public HorizontalBlockFamily(BlockUri uri, Map<Side, Block> blocks, Iterable<String> categories) {
//        this(uri, Side.FRONT, blocks, categories);
//    }

//    public HorizontalBlockFamily(BlockUri uri, Side archetypeSide, Map<Side, Block> blocks, Iterable<String> categories) {
//        super(uri, categories);
//        this.archetypeSide = archetypeSide;
//        for (Side side : Side.horizontalSides()) {
//            Block block = blocks.get(side);
//            if (block == null) {
//                throw new IllegalArgumentException("Missing block for side: " + side.toString());
//            }
//            this.blocks.put(side, block);
//            block.setBlockFamily(this);
//            block.setUri(new BlockUri(uri, new Name(side.name())));
//        }
//    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        if (attachmentSide.isHorizontal()) {
            return blocks.get(attachmentSide);
        }
        if (direction != null) {
            return blocks.get(direction);
        } else {
            return blocks.get(Side.FRONT);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(this.getArchetypeSide());
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toString().toUpperCase(Locale.ENGLISH));
                return blocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }

    @Override
    public Block getBlockForSide(Side side) {
        return blocks.get(side);
    }

    @Override
    public Side getSide(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (block == sideBlockEntry.getValue()) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }


}
