/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RegisterBlockFamily("multiconnect")
@BlockSections({"no_connections","one_connection","line_connection","2d_corner","3d_corner","2d_t","cross","3d_side","five_connections", "all"})
public class MultiConnectFamily extends AbstractBlockFamily{
    public static final String NO_CONNECTIONS = "no_connections";
    public static final String ONE_CONNECTION = "one_connection";
    public static final String TWO_CONNECTIONS_LINE = "line_connection";
    public static final String TWO_CONNECTIONS_CORNER = "2d_corner";
    public static final String THREE_CONNECTIONS_CORNER = "3d_corner";
    public static final String THREE_CONNECTIONS_T = "2d_t";
    public static final String FOUR_CONNECTIONS_CROSS = "cross";
    public static final String FOUR_CONNECTIONS_SIDE = "3d_side";
    public static final String FIVE_CONNECTIONS = "five_connections";
    public static final String SIX_CONNECTIONS = "all";


    private ConnectionCondition connectionCondition;
    private byte connectionSides;
    private boolean horizontalOnly;
    private Map<String, Byte> shapeMapping;

    protected Block archetypeBlock;

    public MultiConnectFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
    }

    public MultiConnectFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        TByteObjectMap<String>[] basicBlocks = new TByteObjectMap[7];
        TByteObjectMap<Block> blocksForConnections = new TByteObjectHashMap<>();

        addConnections(basicBlocks, 0, NO_CONNECTIONS);
        addConnections(basicBlocks, 1, ONE_CONNECTION);
        addConnections(basicBlocks, 2, TWO_CONNECTIONS_LINE);
        addConnections(basicBlocks, 2, TWO_CONNECTIONS_CORNER);
        addConnections(basicBlocks, 3, THREE_CONNECTIONS_CORNER);
        addConnections(basicBlocks, 3, THREE_CONNECTIONS_T);
        addConnections(basicBlocks, 4, FOUR_CONNECTIONS_CROSS);
        addConnections(basicBlocks, 4, FOUR_CONNECTIONS_SIDE);
        addConnections(basicBlocks, 5, FIVE_CONNECTIONS);
        addConnections(basicBlocks, 6, SIX_CONNECTIONS);

        BlockUri blockUri = new BlockUri(definition.getUrn());

        // Now make sure we have all combinations based on the basic set (above) and rotations
        for (byte connections = 0; connections < 64; connections++) {
            // Only the allowed connections should be created
            if ((connections & connectionSides) == connections) {
                Block block = constructBlockForConnections(connections, blockBuilder, definition, basicBlocks);
                if (block == null) {
                    throw new IllegalStateException("Unable to find correct block definition for connections: " + connections);
                }
                block.setUri(new BlockUri(blockUri, new Name(String.valueOf(connections))));
                blocksForConnections.put(connections, block);
            }
        }

        archetypeBlock = blocksForConnections.get(SideBitFlag.getSides(Side.RIGHT, Side.LEFT));

    }



    private void addConnections(TByteObjectMap<String>[] basicBlocks, int index, String connections) {
        if (basicBlocks[index] == null) {
            basicBlocks[index] = new TByteObjectHashMap<>();
        }
        Byte val = shapeMapping.get(connections);
        if (val != null) {
            basicBlocks[index].put(shapeMapping.get(connections), connections);
        }
    }

    private Block constructBlockForConnections(final byte connections, final BlockBuilderHelper blockBuilder,
                                               BlockFamilyDefinition definition, TByteObjectMap<String>[] basicBlocks) {
        int connectionCount = SideBitFlag.getSides(connections).size();
        TByteObjectMap<String> possibleBlockDefinitions = basicBlocks[connectionCount];
        final TByteObjectIterator<String> blockDefinitionIterator = possibleBlockDefinitions.iterator();
        while (blockDefinitionIterator.hasNext()) {
            blockDefinitionIterator.advance();
            final byte originalConnections = blockDefinitionIterator.key();
            final String section = blockDefinitionIterator.value();
            Rotation rot = getRotationToAchieve(originalConnections, connections);
            if (rot != null) {
                return blockBuilder.constructTransformedBlock(definition, section, rot);
            }
        }
        return null;
    }

    private Rotation getRotationToAchieve(byte source, byte target) {
        Collection<Side> originalSides = SideBitFlag.getSides(source);

        Iterable<Rotation> rotations = horizontalOnly ? Rotation.horizontalRotations() : Rotation.values();
        for (Rotation rot : rotations) {
            Set<Side> transformedSides = Sets.newHashSet();
            transformedSides.addAll(originalSides.stream().map(rot::rotate).collect(Collectors.toList()));

            byte transformedSide = SideBitFlag.getSides(transformedSides);
            if (transformedSide == target) {
                return rot;
            }
        }
        return null;
    }

    @Override
    public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
        return null;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return null;
    }

}
