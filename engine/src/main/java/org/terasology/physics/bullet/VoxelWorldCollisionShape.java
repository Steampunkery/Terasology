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
package org.terasology.physics.bullet;

import com.badlogic.gdx.physics.bullet.collision.BroadphaseNativeTypes;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.bulletphysics.collision.shapes.voxel.VoxelPhysicsWorld;

import javax.vecmath.Vector3f;

/**
 * Created by michaelpollind on 4/10/17.
 */
public class VoxelWorldCollisionShape extends btCollisionShape {
    public static final int AABB_SIZE = 2147483647;
   // private VoxelPhysicsWorld world;
    private float collisionMargin = 0.0F;
    protected final Vector3f localScaling = new Vector3f(1.0F, 1.0F, 1.0F);

    protected VoxelWorldCollisionShape(String className, long cPtr, boolean cMemoryOwn) {
        super(className, cPtr, cMemoryOwn);
    }


    public void getAabb(Vector3f aabbMin, Vector3f aabbMax) {
        aabbMin.set(-2.14748365E9F, -2.14748365E9F, -2.14748365E9F);
        aabbMax.set(2.14748365E9F, 2.14748365E9F, 2.14748365E9F);
    }


    public void setLocalScaling(Vector3f scaling) {
        this.localScaling.set(scaling);
    }

    public Vector3f getLocalScaling(Vector3f out) {
        out.set(this.localScaling);
        return out;

    }

    public void calculateLocalInertia(float mass, Vector3f inertia) {
        inertia.set(0.0F, 0.0F, 0.0F);
    }

    public int getShapeType() {
        return BroadphaseNativeTypes.BOX_SHAPE_PROXYTYPE;
        //return BroadphaseNativeType.VOXEL_WORLD_PROXYTYPE;
    }

    public void setMargin(float margin) {
        this.collisionMargin = margin;
    }

    public float getMargin() {
        return this.collisionMargin;
    }

    public String getName() {
        return "World";
    }

    /*public VoxelPhysicsWorld getWorld() {
        return this.world;
    }*/
}
