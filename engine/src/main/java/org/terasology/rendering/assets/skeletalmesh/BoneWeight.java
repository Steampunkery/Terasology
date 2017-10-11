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

package org.terasology.rendering.assets.skeletalmesh;

import com.badlogic.gdx.math.Vector3;

/**
 */
public class BoneWeight {
    private Vector3 position = new Vector3();
    private float bias;
    private int boneIndex;
    private Vector3 normal = new Vector3();

    public BoneWeight(Vector3 position, float bias, int boneIndex) {
        this.position.set(position);
        this.bias = bias;
        this.boneIndex = boneIndex;
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getBias() {
        return bias;
    }

    public int getBoneIndex() {
        return boneIndex;
    }

    public Vector3 getNormal() {
        return normal;
    }

    public void setNormal(Vector3 normal) {
        this.normal.set(normal);
    }
}

