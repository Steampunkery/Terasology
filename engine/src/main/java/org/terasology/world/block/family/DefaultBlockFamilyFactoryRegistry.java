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
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;

import java.util.Map;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBlockFamilyFactoryRegistry.class);


    private Map<String, Class<?>> registryMap = Maps.newHashMap();

    public void setBlockFamily(String id, Class<?> blockFamily) {
        registryMap.put(id.toLowerCase(), blockFamily);
    }

    @Override
    public BlockFamily getBlockFamily(String blockFamilyId) {
        if (blockFamilyId == null || blockFamilyId.isEmpty()) {
            return new SymmetricFamily();
        }

        try {
            AbstractBlockFamily family = (AbstractBlockFamily) registryMap.get(blockFamilyId.toLowerCase()).newInstance();
            if (family == null) {
                return new SymmetricFamily();
            }
            return family;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Failed to load blockFamily {}", blockFamilyId, e);
            e.printStackTrace();
        }
        return null;
    }


    public void clear() {
        registryMap.clear();
    }
}
