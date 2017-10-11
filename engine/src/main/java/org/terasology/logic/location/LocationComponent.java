/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.location;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Direction;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.rendering.nui.properties.TextField;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Component represent the location and facing of an entity in the world
 *
 */
public final class LocationComponent implements Component, ReplicationCheck {

    public boolean replicateChanges = true;

    // Relative to
    @Replicate
    EntityRef parent = EntityRef.NULL;

    @Replicate
    List<EntityRef> children = Lists.newArrayList();

    // Standard position/rotation
    @Replicate
    @TextField
    Vector3 position = new Vector3();
    @Replicate
    Quaternion rotation = new Quaternion(0, 0, 0, 1);
    @Replicate
    float scale = 1.0f;

    public LocationComponent() {
    }

    public LocationComponent(Vector3 position) {
        this.position.set(position);
    }

    /**
     * @return The position of this component relative to any parent. Can be directly modified to update the component
     */
    public Vector3 getLocalPosition() {
        return position;
    }

    public void setLocalPosition(Vector3 newPos) {
        position.set(newPos);
    }

    public Vector3 getLocalDirection() {
        Vector3 result = Direction.FORWARD.getVector3f();
        return result.mul(getLocalRotation());
    }

    public Quaternion getLocalRotation() {
        return rotation;
    }

    public void setLocalRotation(Quaternion newQuat) {
        rotation.set(newQuat);
    }

    public void setLocalScale(float value) {
        this.scale = value;
    }

    public float getLocalScale() {
        return scale;
    }

    /**
     * @return A new vector containing the world location.
     */
    public Vector3 getWorldPosition() {
        return getWorldPosition(new Vector3());
    }

    public Vector3 getWorldPosition(Vector3 output) {
        output.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.scl(parentLoc.scale);
            output.mul(parentLoc.getLocalRotation());
            output.add(parentLoc.position);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public Vector3 getWorldDirection() {
        Vector3 result = Direction.FORWARD.getVector3f();
        return result.mul(getWorldRotation());
    }

    public Quaternion getWorldRotation() {
        return getWorldRotation(new Quaternion(0, 0, 0, 1));
    }

    public Quaternion getWorldRotation(Quaternion output) {
        output.set(rotation);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.mul(parentLoc.rotation);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public float getWorldScale() {
        float result = scale;
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            result *= parentLoc.getLocalScale();
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return result;
    }

    public void setWorldPosition(Vector3 value) {
        this.position.set(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition());
            this.position.scl(1f / parentLoc.getWorldScale());
            Quaternion rot = new Quaternion(parentLoc.getWorldRotation());
            rot.mul(1,-1,-1,-1);
            rot.transform(this.position);
        }
    }

    public void setWorldRotation(Quaternion value) {
        this.rotation.set(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quaternion worldRot = parentLoc.getWorldRotation();
            worldRot.mul(1,-1,-1,-1);
            this.rotation.mul(worldRot);
        }
    }

    public void setWorldScale(float value) {
        this.scale = value;
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.scale /= parentLoc.getWorldScale();
        }
    }

    public EntityRef getParent() {
        return parent;
    }

    public Collection<EntityRef> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LocationComponent) {
            LocationComponent other = (LocationComponent) o;
            return other.scale == scale && Objects.equals(parent, other.parent) && Objects.equals(position, other.position) && Objects.equals(rotation, other.rotation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, rotation, scale, parent);
    }

    @Override
    public boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner) {
        return initial || replicateChanges;
    }
}
