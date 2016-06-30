package com.smartisanos.sidebar.util.anim;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3f(float _x, float _y) {
        x = _x;
        y = _y;
    }

    public Vector3f(int _x, int _y) {
        x = _x;
        y = _y;
        z = 0;
    }

    public Vector3f(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public Vector3f(int _x, int _y, int _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Vector3f) {
            Vector3f v = (Vector3f) o;
            if (x == v.x && y == v.y && z == v.z) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new String("("+x+", "+y+", "+z+")");
    }

    @Override
    protected Vector3f clone() {
        return new Vector3f(x, y, z);
    }

    /**
     *
     * <code>subtract</code> subtracts the values of a given vector from those
     * of this vector creating a new vector object. If the provided vector is
     * null, null is returned.
     *
     * @param vec
     *            the vector to subtract from this vector.
     * @return the result vector.
     */
    public Vector3f subtract(Vector3f vec) {
        return new Vector3f(x - vec.x, y - vec.y, z - vec.z);
    }

    /**
     * <code>crossLocal</code> calculates the cross product of this vector
     * with a parameter vector v.
     *
     * @param v
     *            the vector to take the cross product of with this.
     * @return this.
     */
    public Vector3f crossLocal(Vector3f v) {
        return crossLocal(v.x, v.y, v.z);
    }

    /**
     * <code>crossLocal</code> calculates the cross product of this vector
     * with a parameter vector v.
     *
     * @param otherX
     *            x component of the vector to take the cross product of with this.
     * @param otherY
     *            y component of the vector to take the cross product of with this.
     * @param otherZ
     *            z component of the vector to take the cross product of with this.
     * @return this.
     */
    public Vector3f crossLocal(float otherX, float otherY, float otherZ) {
        float tempx = ( y * otherZ ) - ( z * otherY );
        float tempy = ( z * otherX ) - ( x * otherZ );
        z = (int)((x * otherY) - (y * otherX));
        x = (int)tempx;
        y = (int)tempy;
        return this;
    }

    /**
     * <code>normalizeLocal</code> makes this vector into a unit vector of
     * itself.
     *
     * @return this.
     */
    public Vector3f normalizeLocal() {
        // NOTE: this implementation is more optimized
        // than the old jme normalize as this method
        // is commonly used.
        float length = x * x + y * y + z * z;
        if (length != 1f && length != 0f){
            length = 1.0f / FastMath.sqrt(length);
            x *= length;
            y *= length;
            z *= length;
        }
        return this;
    }
}