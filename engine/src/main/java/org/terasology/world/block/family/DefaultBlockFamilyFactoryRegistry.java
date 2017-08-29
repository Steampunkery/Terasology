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
import org.terasology.context.Context;
import org.terasology.registry.InjectionHelper;

import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBlockFamilyFactoryRegistry.class);
    private Context context;


    private Map<String, Class<? extends AbstractBlockFamily>> registryMap = Maps.newHashMap();

    public void setBlockFamily(String id, Class<? extends AbstractBlockFamily> blockFamily) {
        registryMap.put(id.toLowerCase(), blockFamily);
    }

    public DefaultBlockFamilyFactoryRegistry(Context context) {
        this.context = context;
    }


    @Override
    public BlockFamily createFamily(Class<? extends AbstractBlockFamily> blockFamily) {
        try {
            return (AbstractBlockFamily) InjectionHelper.createWithConstructorInjection(blockFamily,context);
        } catch (NoSuchElementException e) {
            logger.error("Failed to load blockFamily {}", blockFamily, e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Class<? extends AbstractBlockFamily> getBlockFamily(String blockFamilyId)
    {
        if (blockFamilyId == null || blockFamilyId.isEmpty()) {
            return SymmetricFamily.class;
        }
        return registryMap.get(blockFamilyId.toLowerCase());
    }


    @Override
    public String[] getSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if(blockFamily == null)
            return new String[]{};
        BlockSections sections =  blockFamily.getAnnotation(BlockSections.class);
        if(sections == null)
            return new String[]{};
        return sections.value();
    }

    @Override
    public MultiSection[] getMultiSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null)
            return new MultiSection[]{};
        MultiSections sections = blockFamily.getAnnotation(MultiSections.class);
        return sections.value();

    }

    @Override
    public boolean isFreeformSupported(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null)
            return false;
        FreeFormSupported freeFormSupported = blockFamily.getAnnotation(FreeFormSupported.class);

        return freeFormSupported != null;
    }


    public void clear() {
        registryMap.clear();
    }
}
