package com.smartisanos.sidebar.util.anim;

public class Vector3f {
    public int x;
    public int y;
    public int z;

    public Vector3f(int _x, int _y) {
        x = _x;
        y = _y;
        z = 0;
    }

    public Vector3f(int _x, int _y, int _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public Vector3f(float _x, float _y) {
        this((int) _x, (int) _y);
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
}