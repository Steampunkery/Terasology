/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.chunks;

import com.badlogic.gdx.math.GridPoint3;
import org.terasology.module.sandbox.API;

/**
 */
@API
public interface LitChunk extends CoreChunk {

    byte getSunlight(GridPoint3 pos);

    byte getSunlight(int x, int y, int z);

    boolean setSunlight(GridPoint3 pos, byte amount);

    boolean setSunlight(int x, int y, int z, byte amount);

    byte getSunlightRegen(GridPoint3 pos);

    byte getSunlightRegen(int x, int y, int z);

    boolean setSunlightRegen(GridPoint3 pos, byte amount);

    boolean setSunlightRegen(int x, int y, int z, byte amount);

    byte getLight(GridPoint3 pos);

    byte getLight(int x, int y, int z);

    boolean setLight(GridPoint3 pos, byte amount);

    boolean setLight(int x, int y, int z, byte amount);
}
