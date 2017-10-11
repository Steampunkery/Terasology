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
package org.terasology.rendering.nui.layouts;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import org.terasology.input.Keyboard;
import org.terasology.math.Region2i;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;

import java.util.Iterator;
import java.util.List;

/**
 * A layout that allows positioning to a virtual coordinate system, which is mapped to screen coordinates using a
 * viewport.
 *
 */
public class ZoomableLayout extends CoreLayout {
    private List<PositionalWidget> widgets = Lists.newArrayList();
    private Vector2 pixelSize;
    private GridPoint2 screenSize;
    private Vector2 windowPosition = new Vector2();
    private Vector2 windowSize = new Vector2(50, 50);

    private GridPoint2 last;

    private InteractionListener dragListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            last = new GridPoint2(event.getRelativeMousePosition());
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2 p = screenToWorld(last);
            p.sub(screenToWorld(event.getRelativeMousePosition()));
            p.add(windowPosition);

            setWindowPosition(p);
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            if (event.getKeyboard().isKeyDown(Keyboard.Key.LEFT_SHIFT.getId())) {
                float scale = 1 + event.getWheelTurns() * 0.05f;
                zoom(scale, scale, event.getRelativeMousePosition());
            }
            return false;
        }
    };

    public ZoomableLayout() {
    }

    public ZoomableLayout(String id) {
        super(id);
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        if (element instanceof PositionalWidget) {
            PositionalWidget positionalWidget = (PositionalWidget) element;
            addWidget(positionalWidget);
        }
    }

    public void addWidget(PositionalWidget widget) {
        if (widget != null) {
            widgets.add(widget);
            widget.onAdded(this);
        }
    }

    @Override
    public void removeWidget(UIWidget element) {
        if (element instanceof PositionalWidget) {
            PositionalWidget positionalWidget = (PositionalWidget) element;
            removeWidget(positionalWidget);
        }
    }

    public void removeWidget(PositionalWidget widget) {
        if (widget != null) {
            widget.onRemoved(this);
            widgets.remove(widget);
        }
    }

    public void removeAll() {
        for (PositionalWidget widget : widgets) {
            widget.onRemoved(this);
        }
        widgets.clear();
    }

    @Override
    public void onDraw(Canvas canvas) {
        setScreenSize(canvas.size());
        calculateSizes();

        canvas.addInteractionRegion(dragListener);
        for (PositionalWidget widget : widgets) {
            if (!widget.isVisible()) {
                continue;
            }
            GridPoint2 screenStart = worldToScreen(widget.getPosition());
            Vector2 worldEnd = new Vector2(widget.getPosition());
            worldEnd.add(widget.getSize());
            GridPoint2 screenEnd = worldToScreen(worldEnd);
            canvas.drawWidget(widget, new Region2i(screenStart.x,screenStart.y,screenEnd.x - screenStart.x + 1,screenEnd.y - screenStart.y + 1));
        }
    }

    @Override
    public GridPoint2 getPreferredContentSize(Canvas canvas, GridPoint2 sizeHint) {
        return new GridPoint2();
    }

    @Override
    public GridPoint2 getMaxContentSize(Canvas canvas) {
        return new GridPoint2(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void update(float delta) {
        for (PositionalWidget widget : widgets) {
            widget.update(delta);
        }
    }

    public List<PositionalWidget> getWidgets() {
        return widgets;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return new Iterator<UIWidget>() {
            private Iterator<PositionalWidget> delegate = widgets.iterator();

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public UIWidget next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
            }
        };
    }

    public Vector2 screenToWorld(GridPoint2 screenPos) {
        Vector2 world = new Vector2(screenPos.x / pixelSize.x, screenPos.y / pixelSize.y);
        world.add(windowPosition);
        return world;
    }

    public GridPoint2 worldToScreen(Vector2 world) {
        return new GridPoint2(TeraMath.ceilToInt((world.x - windowPosition.x) * pixelSize.x), TeraMath.ceilToInt((world.y - windowPosition.y) * pixelSize.y));
    }

    public void setWindowPosition(Vector2 pos) {
        windowPosition = pos;
    }

    public void setWindowSize(Vector2 size) {
        windowSize = size;
    }

    public void setScreenSize(GridPoint2 size) {
        screenSize = size;
    }

    public Vector2 getPixelSize() {
        return pixelSize;
    }

    public GridPoint2 getScreenSize() {
        return screenSize;
    }

    public Vector2 getWindowPosition() {
        return windowPosition;
    }

    public Vector2 getWindowSize() {
        return windowSize;
    }

    public void calculateSizes() {
        if (windowSize.x > windowSize.y) {
            windowSize.x = windowSize.y;
        }

        if (windowSize.x < windowSize.y) {
            windowSize.y = windowSize.x;
        }

        if ((screenSize.x != 0) && (screenSize.y != 0)) {
            if (screenSize.x > screenSize.y) {
                windowSize.x *= (float) screenSize.x / screenSize.y;
            } else {
                windowSize.y *= (float) screenSize.y / screenSize.x;
            }
        }

        if ((windowSize.x > 0) && (windowSize.y > 0)) {
            pixelSize = new Vector2(screenSize.x / windowSize.x, screenSize.y / windowSize.y);
        } else {
            pixelSize = new Vector2();
        }
    }

    public void zoom(float zoomX, float zoomY, GridPoint2 mousePos) {
        Vector2 mouseBefore = screenToWorld(mousePos);

        windowSize.x *= zoomX;
        windowSize.y *= zoomY;
        calculateSizes();

        Vector2 mouseAfter = screenToWorld(mousePos);

        windowPosition.x -= mouseAfter.x - mouseBefore.x;
        windowPosition.y -= mouseAfter.y - mouseBefore.y;
    }


    public interface PositionalWidget<L extends ZoomableLayout> extends UIWidget {
        Vector2 getPosition();

        Vector2 getSize();

        void onAdded(L layout);

        void onRemoved(L layout);
    }
}
