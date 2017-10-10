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

package org.terasology.math;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.common.math.DoubleMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.math.RoundingMode;

/**
 * Some utilities for compatibility with VecMath.
 */
public final class VecMath {

    private VecMath() {
        // no instances
    }


    public static GridPoint3 ToGridPointFloor(Vector3 v){
        return new GridPoint3(DoubleMath.roundToInt(v.x, RoundingMode.FLOOR),
                DoubleMath.roundToInt(v.y, RoundingMode.FLOOR),
                DoubleMath.roundToInt(v.z, RoundingMode.FLOOR));
    }

    public static GridPoint3 ToGridPointFloor(float x, float y, float z) {
        return new GridPoint3(DoubleMath.roundToInt(x, RoundingMode.FLOOR),
                DoubleMath.roundToInt(y, RoundingMode.FLOOR),
                DoubleMath.roundToInt(z, RoundingMode.FLOOR));
    }

}
