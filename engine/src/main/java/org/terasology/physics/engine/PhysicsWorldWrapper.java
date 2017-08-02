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

package org.terasology.physics.engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.VecMath;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.HashSet;

/**
 * This class links Terasology's voxel world with the physics engine, providing it with the collision information for each block location.
 *
 */


public class PhysicsWorldWrapper extends btVoxelContentProvider {
    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private WorldProvider world;

    public PhysicsWorldWrapper(WorldProvider world)
    {
        super();
        this.world = world;
    }

    @Override
    public btVoxelInfo getVoxel(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        btCollisionShape shape =  block.getCollisionShape();

        return new btVoxelInfo(
                shape != null && block.isTargetable(),
                shape != null && !block.isPenetrable(),
                block.getId(),
                x,y,z,
                shape,
                VecMath.to(block.getCollisionOffset()),
                0,0,0);
    }

}
