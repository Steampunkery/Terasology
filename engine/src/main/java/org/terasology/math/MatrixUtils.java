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

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Collection of matrix utilities.
 *
 */
public final class MatrixUtils {

    private MatrixUtils() {
    }

    /**
     * Copies the given matrix into a newly allocated FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @return A new FloatBuffer containing the matrix in column-major form.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4 m) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrixToFloatBuffer(m, buffer);
        return buffer;
    }

    /**
     * Copies the given matrix into a newly allocated FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m the matrix to copy
     * @return A new FloatBuffer containing the matrix in column-major form.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3 m) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        matrixToFloatBuffer(m, buffer);
        return buffer;
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix3 m, FloatBuffer fb) {
        fb.put(m.val[Matrix3.M00]);
        fb.put(m.val[Matrix3.M10]);
        fb.put(m.val[Matrix3.M20]);
        fb.put(m.val[Matrix3.M01]);
        fb.put(m.val[Matrix3.M11]);
        fb.put(m.val[Matrix3.M21]);
        fb.put(m.val[Matrix3.M02]);
        fb.put(m.val[Matrix3.M12]);
        fb.put(m.val[Matrix3.M22]);

        fb.flip();
        return fb;
    }

    /**
     * Copies the given matrix into an existing FloatBuffer.
     * The order of the elements is column major (as used by OpenGL).
     *
     * @param m  the matrix to copy
     * @param fb the float buffer to copy the matrix into
     * @return The provided float buffer.
     */
    public static FloatBuffer matrixToFloatBuffer(Matrix4 m, FloatBuffer fb) {
        fb.put(m.val[Matrix4.M00]);
        fb.put(m.val[Matrix4.M10]);
        fb.put(m.val[Matrix4.M20]);
        fb.put(m.val[Matrix4.M30]);

        fb.put(m.val[Matrix4.M01]);
        fb.put(m.val[Matrix4.M11]);
        fb.put(m.val[Matrix4.M21]);
        fb.put(m.val[Matrix4.M31]);

        fb.put(m.val[Matrix4.M02]);
        fb.put(m.val[Matrix4.M12]);
        fb.put(m.val[Matrix4.M22]);
        fb.put(m.val[Matrix4.M32]);

        fb.put(m.val[Matrix4.M03]);
        fb.put(m.val[Matrix4.M13]);
        fb.put(m.val[Matrix4.M23]);
        fb.put(m.val[Matrix4.M33]);

        fb.flip();
        return fb;
    }

    public static Matrix4 createViewMatrix(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        return createViewMatrix(new Vector3(eyeX, eyeY, eyeZ), new Vector3(centerX, centerY, centerZ), new Vector3(upX, upY, upZ));
    }

    public static Matrix4 createViewMatrix(Vector3 eye, Vector3 center, Vector3 up) {
        Matrix4 m = new Matrix4();

        Vector3 f = new Vector3(center);
        f.sub(eye);

        f.nor();
        up.nor();

        Vector3 s = new Vector3(f);
        s.crs(up);
        s.nor();

        Vector3 u = new Vector3(s);
        u.crs(f);
        u.nor();

        m.val[Matrix4.M00]= s.x;
        m.val[Matrix4.M10] = s.y;
        m.val[Matrix4.M20] = s.z;
        m.val[Matrix4.M30] = 0;
        m.val[Matrix4.M01] = u.x;
        m.val[Matrix4.M11] = u.y;
        m.val[Matrix4.M21] = u.z;
        m.val[Matrix4.M31] = 0;
        m.val[Matrix4.M02] = -f.x;
        m.val[Matrix4.M12] = -f.y;
        m.val[Matrix4.M22] = -f.z;
        m.val[Matrix4.M32] = 0;
        m.val[Matrix4.M03] = 0;
        m.val[Matrix4.M13] = 0;
        m.val[Matrix4.M23] = 0;
        m.val[Matrix4.M33] = 1;

        m.val[Matrix4.M30] = -eye.x;
        m.val[Matrix4.M31] = -eye.y;
        m.val[Matrix4.M32] = -eye.z;

        m.tra();

        return m;
    }

    public static Matrix4 createOrthogonalProjectionMatrix(float left, float right, float top, float bottom, float near, float far) {
        Matrix4 m = new Matrix4();

        float lateral = right - left;
        float vertical = top - bottom;
        float forward = far - near;
        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        m.val[Matrix4.M00] = 2.0f / lateral;
        m.val[Matrix4.M10] = 0.0f;
        m.val[Matrix4.M20] = 0.0f;
        m.val[Matrix4.M30] = tx;
        m.val[Matrix4.M01] = 0.0f;
        m.val[Matrix4.M11] = 2.0f / vertical;
        m.val[Matrix4.M21] = 0.0f;
        m.val[Matrix4.M31] = ty;
        m.val[Matrix4.M02] = 0.0f;
        m.val[Matrix4.M12] = 0.0f;
        m.val[Matrix4.M22] = -2.0f / forward;
        m.val[Matrix4.M32] = tz;
        m.val[Matrix4.M03] = 0.0f;
        m.val[Matrix4.M13] = 0.0f;
        m.val[Matrix4.M23] = 0.0f;
        m.val[Matrix4.M33] = 1.0f;

        m.tra();

        return m;
    }

    public static Matrix4 createPerspectiveProjectionMatrix(float fovY, float aspectRatio, float zNear, float zFar) {
        Matrix4 m = new Matrix4();

        float f = 1.0f / (float) Math.tan(fovY * 0.5f);

        m.val[Matrix4.M00] = f / aspectRatio;
        m.val[Matrix4.M10] = 0;
        m.val[Matrix4.M20] = 0;
        m.val[Matrix4.M30] = 0;
        m.val[Matrix4.M01] = 0;
        m.val[Matrix4.M11] = f;
        m.val[Matrix4.M21] = 0;
        m.val[Matrix4.M31] = 0;
        m.val[Matrix4.M02] = 0;
        m.val[Matrix4.M12] = 0;
        m.val[Matrix4.M22] = (zFar + zNear) / (zNear - zFar);
        m.val[Matrix4.M32] = (2 * zFar * zNear) / (zNear - zFar);
        m.val[Matrix4.M03] = 0;
        m.val[Matrix4.M13] = 0;
        m.val[Matrix4.M23] = -1;
        m.val[Matrix4.M33] = 0;

        m.tra();

        return m;
    }

    public static Matrix4 calcViewProjectionMatrix(Matrix4 vm, Matrix4 p) {
        Matrix4 result = new Matrix4(p);
        result.mul(vm);
        return result;
    }

    public static Matrix4 calcModelViewMatrix(Matrix4 m, Matrix4 vm) {
        Matrix4 result = new Matrix4(m);
        result.mul(vm);
        return result;
    }

    public static Matrix3 calcNormalMatrix(Matrix4 mv) {
        Matrix3 result = new Matrix3();
        result.val[Matrix3.M00] = mv.val[Matrix4.M00];
        result.val[Matrix3.M10] = mv.val[Matrix4.M10];
        result.val[Matrix3.M20] = mv.val[Matrix4.M20];
        result.val[Matrix3.M01] = mv.val[Matrix4.M01];
        result.val[Matrix3.M11] = mv.val[Matrix4.M11];
        result.val[Matrix3.M21] = mv.val[Matrix4.M21];
        result.val[Matrix3.M02] = mv.val[Matrix4.M02];
        result.val[Matrix3.M12] = mv.val[Matrix4.M12];
        result.val[Matrix3.M22] = mv.val[Matrix4.M22];

        result.inv();
        result.transpose();
        return result;
    }
}
