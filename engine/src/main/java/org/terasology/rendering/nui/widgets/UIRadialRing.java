/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import org.terasology.math.Region2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

/**
 * A radial menu widget
 */
public class UIRadialRing extends CoreWidget {

    @LayoutConfig
    private UIRadialSection[] sections = {};

    private int selectedTab = -1;
    private boolean hasInitialised;
    private int radius;
    private double sectionAngle;

    private BaseInteractionListener baseInteractionListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            int sectionOver = getSectionOver(event.getRelativeMousePosition());
            if (selectedTab != -1) {
                sections[selectedTab].setSelected(false);
            }

            if (sectionOver != -1) {
                sections[sectionOver].setSelected(true);
            }
            selectedTab = sectionOver;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (selectedTab != -1) {
                sections[selectedTab].setSelected(false);
                sections[selectedTab].activateSection();
                selectedTab = -1;
            }
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        if (!hasInitialised) {
            hasInitialised = true;
            initialise(canvas);
        }
        canvas.addInteractionRegion(baseInteractionListener);
        for (UIRadialSection section : sections) {
            canvas.drawWidget(section);
        }
    }

    private void initialise(Canvas canvas) {
        final double CIRCLE_TO_SQUARE = 0.707106781;
        Region2i region = canvas.getRegion();

        int sectionWidth = region.width / 4;
        double offset = sectionWidth * 1.5;
        radius = sectionWidth * 2;
        sectionAngle = (Math.PI * 2) / sections.length;
        int infoSquareSize = (int) (radius * CIRCLE_TO_SQUARE);
        int sectionSquareSize = (int) (sectionWidth * CIRCLE_TO_SQUARE);
        Region2i infoRegion = new Region2i(
                sectionWidth + infoSquareSize / 4,
                sectionWidth + infoSquareSize / 4,
                infoSquareSize,
                infoSquareSize);

        for (int i = 0; i < sections.length; i++) {
            sections[i].setDrawRegion(new Region2i(
                    (int) (Math.cos(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5),
                    (int) (Math.sin(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5),
                    sectionWidth, sectionWidth));
            sections[i].setInnerRegion(new Region2i(
                    (int) (Math.cos(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5 + sectionSquareSize / 4),
                    (int) (Math.sin(i * sectionAngle + sectionAngle / 2) * offset + sectionWidth * 1.5 + sectionSquareSize / 4),
                    sectionSquareSize, sectionSquareSize));
            sections[i].setInfoRegion(infoRegion);
        }
    }

    /**
     * Subscribes a listener to a section. Will be triggered when the mouse is released over that section
     *
     * @param sectionNum The section to attach the listener to
     * @param listener   The listener to attach
     */
    public void subscribe(int sectionNum, ActivateEventListener listener) {
        if (sectionNum >= 0 && sectionNum < sections.length) {
            sections[sectionNum].addListener(listener);
        }
    }

    /**
     * Unsubscribes a listener from a section. It will no longer be triggered by that section
     *
     * @param sectionNum The section to unsubscribe from
     * @param listener   The listener to unsubscribe
     */
    public void unsubscribe(int sectionNum, ActivateEventListener listener) {
        if (sectionNum >= 0 && sectionNum < sections.length) {
            sections[sectionNum].removeListener(listener);
        }
    }

    @Override
    public GridPoint2 getPreferredContentSize(Canvas canvas, GridPoint2 sizeHint) {
        Region2i p = canvas.getRegion();
        return new GridPoint2(p.width,p.height);
    }


    private int getSectionOver(GridPoint2 mousePos) {
        mousePos.x -= radius;
        mousePos.y -= radius;

        double angle = Math.atan2(mousePos.y, mousePos.x);
        angle = angle < 0 ? angle + Math.PI * 2 : angle;

        double dist = Math.sqrt(mousePos.x * mousePos.x + mousePos.y * mousePos.y);
        if (dist < radius / 2 || dist > radius) {
            return -1;
        }

        return (int) Math.floor(angle / sectionAngle);
    }

}
