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

import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;

import java.util.Locale;

/**
 */
public enum StandardCollisionGroup implements CollisionGroup {
    NONE((short) 0b00000000),
    DEFAULT((short) btBroadphaseProxy.CollisionFilterGroups.DefaultFilter),
    STATIC((short) btBroadphaseProxy.CollisionFilterGroups.StaticFilter),
    KINEMATIC((short) btBroadphaseProxy.CollisionFilterGroups.KinematicFilter),
    DEBRIS((short) btBroadphaseProxy.CollisionFilterGroups.DefaultFilter),
    SENSOR((short) btBroadphaseProxy.CollisionFilterGroups.SensorTrigger),
    CHARACTER((short) btBroadphaseProxy.CollisionFilterGroups.CharacterFilter),
    WORLD((short) 128),
    LIQUID((short) 256),
    ALL((short) btBroadphaseProxy.CollisionFilterGroups.AllFilter);

    private short flag;

     StandardCollisionGroup(short flag) {
        this.flag = flag;
    }

    @Override
    public short getFlag() {
        return flag;
    }

    @Override
    public String getName() {
        return "engine:" + toString().toLowerCase(Locale.ENGLISH);
    }
}
