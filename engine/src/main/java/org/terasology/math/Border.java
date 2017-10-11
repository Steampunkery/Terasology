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

import com.badlogic.gdx.math.GridPoint2;

import java.util.Objects;

/**
 * The size of a border, supporting independent widths on each side.
 * <br><br>
 * Immutable
 *
 */
public class Border {
    public static final Border ZERO = new Border(0, 0, 0, 0);

    private int left;
    private int right;
    private int top;
    private int bottom;

    public Border(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getTotalWidth() {
        return left + right;
    }

    public int getTotalHeight() {
        return top + bottom;
    }

    public boolean isEmpty() {
        return left == 0 && right == 0 && top == 0 && bottom == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Border) {
            Border other = (Border) obj;
            return left == other.left && right == other.right
                    && top == other.top && bottom == other.bottom;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right, top, bottom);
    }

    public Region2i shrink(Region2i region) {
        return new Region2i(region.x + getLeft(), region.y + getTop(),region.width - getTotalWidth(), region.height - getTotalHeight());
    }

    public GridPoint2 shrink(GridPoint2 size) {
        return new GridPoint2(size.x - getTotalWidth(), size.y - getTotalHeight());
    }

    public GridPoint2 getTotals() {
        return new GridPoint2(getTotalWidth(), getTotalHeight());
    }

    public GridPoint2 grow(GridPoint2 size) {
        // Note protection against overflow
        return new GridPoint2(TeraMath.addClampAtMax(size.x, getTotalWidth()), TeraMath.addClampAtMax(size.y, getTotalHeight()));
    }

    public Region2i grow(Region2i region) {
        // Note protection against overflow of the size
        return new Region2i(region.x - getLeft(), region.y - getTop(),TeraMath.addClampAtMax(region.width, getTotalWidth()), TeraMath.addClampAtMax(region.height, getTotalHeight()));
    }
}
