package com.smartisanos.sidebar.util.anim;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(float _x, float _y) {
        x = _x;
        y = _y;
        z = 0;
    }

    public Vector3f(float _x, float _y, float _z) {
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
}