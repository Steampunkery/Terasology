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

package org.terasology.physics;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.google.common.math.DoubleMath;
import org.terasology.entitySystem.entity.EntityRef;

import java.math.RoundingMode;

/**
 * A HitResult holds the result of a ray-trace.
 *
 */
public class HitResult {
    private boolean hit;
    private EntityRef entity;
    private Vector3 hitPoint;
    private Vector3 hitNormal;
    private GridPoint3 blockPosition;
    private final boolean worldHit;

    public HitResult() {
        hit = false;
        entity = EntityRef.NULL;
        worldHit = false;
    }

    /**
     * Creates a HitResult for hitting an entity.
     *
     * @param entity
     * @param hitPoint
     * @param hitNormal
     */
    public HitResult(EntityRef entity, Vector3 hitPoint, Vector3 hitNormal) {
        this.hit = true;
        this.entity = entity;
        this.hitPoint = hitPoint;
        this.hitNormal = hitNormal;
        //This is the block were the hitPoint is inside:
        this.blockPosition = new GridPoint3(
                DoubleMath.roundToInt(hitPoint.x, RoundingMode.HALF_UP),
                DoubleMath.roundToInt(hitPoint.y, RoundingMode.HALF_UP),
                DoubleMath.roundToInt(hitPoint.z, RoundingMode.HALF_UP));
        this.worldHit = false;
    }

    /**
     * Creates a HitResult for hitting a block from the world.
     *
     * @param entity
     * @param hitPoint
     * @param hitNormal
     * @param blockPos
     */
    public HitResult(EntityRef entity, Vector3 hitPoint, Vector3 hitNormal, GridPoint3 blockPos) {
        this.hit = true;
        this.entity = entity;
        this.hitPoint = hitPoint;
        this.hitNormal = hitNormal;
        this.blockPosition = blockPos;
        this.worldHit = true;
    }

    /**
     * @return true if something was hit, false otherwise.
     */
    public boolean isHit() {
        return hit;
    }

    /**
     * @return The entity hit, or EntityRef.NULL if no entity was hit.
     */
    public EntityRef getEntity() {
        return entity;
    }

    /**
     * Returns the point where the hit took place.
     *
     * @return null if isHit() == false, otherwise the point where the hit took
     * place.
     */
    public Vector3 getHitPoint() {
        return hitPoint;
    }

    /**
     * Returns the normal of surface on which the hit took place.
     *
     * @return null if isHit() == false, otherwise the normal of surface on
     * which the hit took place.
     */
    public Vector3 getHitNormal() {
        return hitNormal;
    }

    /**
     * @return The block where the hit took place. If the world was hit, it will
     * return the location of the block that was hit. Otherwise it returns the
     * block location inside which the hit took place. This is different from
     * the block position of the entity that got hit!
     */
    public GridPoint3 getBlockPosition() {
        return blockPosition;
    }

    /**
     * Returns true if the hit has hit the world, rather than an entity.
     *
     * @return true if the world has been hit, false otherwise.
     */
    public boolean isWorldHit() {
        return worldHit;
    }
}
