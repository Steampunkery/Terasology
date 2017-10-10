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

package org.terasology.math;

import com.badlogic.gdx.math.Vector3;
import com.bulletphysics.BulletGlobals;

/**
 */
public final class Vector3fUtil {
    private Vector3fUtil() {
    }


    /**
     * @return The reflection of direction against normal
     */
    public static Vector3 reflect(Vector3 direction, Vector3 normal, Vector3 out) {
        out.set(normal);
        out.scl(-2.0f * direction.dot(normal));
        out.add(direction);
        return out;
    }

    /**
     * @return the portion of direction that is parallel to normal
     */
    public static Vector3 getParallelComponent(Vector3 direction, Vector3 normal, Vector3 out) {
        out.set(normal);
        out.scl(direction.dot(normal));
        return out;
    }

    /**
     * @return the portion of direction that is perpendicular to normal
     */
    public static Vector3 getPerpendicularComponent(Vector3 direction, Vector3 normal, Vector3 out) {
        Vector3 perpendicular = getParallelComponent(direction, normal, out);
        perpendicular.scl(-1);
        perpendicular.add(direction);
        return perpendicular;
    }

    public static Vector3 safeNormalize(Vector3 v, Vector3 out) {
        out.set(v);
        out.nor();
        if (out.len() < BulletGlobals.SIMD_EPSILON) {
            out.set(0, 0, 0);
        }
        return out;
    }

    public static Vector3 min(Vector3 a, Vector3 b, Vector3 out) {
        out.x = Math.min(a.x, b.x);
        out.y = Math.min(a.y, b.y);
        out.z = Math.min(a.z, b.z);
        return out;
    }

    public static Vector3 max(Vector3 a, Vector3 b, Vector3 out) {
        out.x = Math.max(a.x, b.x);
        out.y = Math.max(a.y, b.y);
        out.z = Math.max(a.z, b.z);
        return out;
    }
}
