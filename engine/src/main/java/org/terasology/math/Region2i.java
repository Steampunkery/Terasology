/*
 * Copyright 2017 MovingBlocks
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

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.Serializable;

public class Region2i implements Serializable, Shape2D {
    public static final Rectangle tmp = new Rectangle();
    public static final Rectangle tmp2 = new Rectangle();
    private static final long serialVersionUID = 5733252015138115702L;
    public int x;
    public int y;
    public int width;
    public int height;

    public Region2i() {
    }

    public Region2i(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Region2i(Region2i rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
    }

    public Region2i set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public float getX() {
        return this.x;
    }

    public Region2i setX(int x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return this.y;
    }

    public Region2i setY(int y) {
        this.y = y;
        return this;
    }

    public float getWidth() {
        return this.width;
    }

    public Region2i setWidth(int width) {
        this.width = width;
        return this;
    }

    public float getHeight() {
        return this.height;
    }

    public Region2i setHeight(int height) {
        this.height = height;
        return this;
    }

    public Vector2 getPosition(Vector2 position) {
        return position.set(this.x, this.y);
    }

    public Region2i setPosition(GridPoint2 position) {
        this.x = position.x;
        this.y = position.y;
        return this;
    }

    public Region2i setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Region2i setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Region2i setSize(int sizeXY) {
        this.width = sizeXY;
        this.height = sizeXY;
        return this;
    }

    public Vector2 getSize(Vector2 size) {
        return size.set(this.width, this.height);
    }

    public boolean contains(float x, float y) {
        return this.x <= x && this.x + this.width >= x && this.y <= y && this.y + this.height >= y;
    }

    public boolean contains(Vector2 point) {
        return this.contains(point.x, point.y);
    }

    public boolean contains(Circle circle) {
        return circle.x - circle.radius >= this.x && circle.x + circle.radius <= this.x + this.width && circle.y - circle.radius >= this.y && circle.y + circle.radius <= this.y + this.height;
    }

    public boolean contains(Rectangle rectangle) {
        float xmin = rectangle.x;
        float xmax = xmin + rectangle.width;
        float ymin = rectangle.y;
        float ymax = ymin + rectangle.height;
        return xmin > this.x && xmin < this.x + this.width && xmax > this.x && xmax < this.x + this.width && ymin > this.y && ymin < this.y + this.height && ymax > this.y && ymax < this.y + this.height;
    }

    public boolean overlaps(Rectangle r) {
        return this.x < r.x + r.width && this.x + this.width > r.x && this.y < r.y + r.height && this.y + this.height > r.y;
    }

    public Region2i set(Region2i rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
        return this;
    }

    public Region2i merge(Region2i rect) {
        int minX = Math.min(this.x, rect.x);
        int maxX = Math.max(this.x + this.width, rect.x + rect.width);
        this.x = minX;
        this.width = maxX - minX;
        int minY = Math.min(this.y, rect.y);
        int maxY = Math.max(this.y + this.height, rect.y + rect.height);
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public Region2i merge(int x, int y) {
        int minX = Math.min(this.x, x);
        int maxX = Math.max(this.x + this.width, x);
        this.x = minX;
        this.width = maxX - minX;
        int minY = Math.min(this.y, y);
        int maxY = Math.max(this.y + this.height, y);
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public Region2i merge(GridPoint2 vec) {
        return this.merge(vec.x, vec.y);
    }

    public Region2i merge(GridPoint2[] vecs) {
        int minX = this.x;
        int maxX = this.x + this.width;
        int minY = this.y;
        int maxY = this.y + this.height;

        for(int i = 0; i < vecs.length; ++i) {
            GridPoint2 v = vecs[i];
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
        }

        this.x = minX;
        this.width = maxX - minX;
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public float getAspectRatio() {
        return this.height == 0 ? 0 : (float) this.width / (float) this.height;
    }

    public Vector2 getCenter(Vector2 vector) {
        vector.x = this.x + this.width / 2.0F;
        vector.y = this.y + this.height / 2.0F;
        return vector;
    }


    public String toString() {
        return "[" + this.x + "," + this.y + "," + this.width + "," + this.height + "]";
    }

    public Region2i fromString(String v) {
        int s0 = v.indexOf(44, 1);
        int s1 = v.indexOf(44, s0 + 1);
        int s2 = v.indexOf(44, s1 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && v.charAt(0) == '[' && v.charAt(v.length() - 1) == ']') {
            try {
                int x = Integer.parseInt(v.substring(1, s0));
                int y = Integer.parseInt(v.substring(s0 + 1, s1));
                int width = Integer.parseInt(v.substring(s1 + 1, s2));
                int height = Integer.parseInt(v.substring(s2 + 1, v.length() - 1));
                return this.set(x, y, width, height);
            } catch (NumberFormatException var9) {
                ;
            }
        }

        throw new GdxRuntimeException("Malformed Rectangle: " + v);
    }

    public int area() {
        return this.width * this.height;
    }

    public float perimeter() {
        return 2.0F * (this.width + this.height);
    }

    public int hashCode() {
        int result = 31 + this.height;
        result = 31 * result + this.width;
        result = 31 * result + this.x;
        result = 31 * result + this.y;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Rectangle other = (Rectangle)obj;
            if (this.height != other.height) {
                return false;
            } else if (this.width != other.width) {
                return false;
            } else if (this.x != other.x) {
                return false;
            } else {
                return this.y == other.y;
            }
        }
    }
}
