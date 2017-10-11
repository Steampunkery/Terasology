/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.persistence.typeHandling.mathTypes;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.SimpleTypeHandler;

import java.util.Map;


/**
 */
public class Rect2fTypeHandler extends SimpleTypeHandler<Rectangle> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    @Override
    public PersistedData serialize(Rectangle value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        } else {
            Map<String, PersistedData> map = Maps.newLinkedHashMap();
            map.put(MIN_FIELD, context.create(new Vector2(value.x,value.y), Vector2.class));
            map.put(SIZE_FIELD, context.create(new Vector2(value.width,value.height), Vector2.class));
            return context.create(map);
        }
    }

    @Override
    public Rectangle deserialize(PersistedData data, DeserializationContext context) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();
            Vector2 min = context.deserializeAs(map.get(MIN_FIELD), Vector2.class);
            Vector2 size = context.deserializeAs(map.get(SIZE_FIELD), Vector2.class);
            return new Rectangle(min.x,min.y,size.x,size.y);
        }
        return null;
    }

}
