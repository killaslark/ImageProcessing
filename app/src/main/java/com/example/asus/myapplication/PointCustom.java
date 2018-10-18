package com.example.asus.myapplication;

import android.graphics.Point;

public class PointCustom extends Point {
    PointCustom() {
    }

    PointCustom(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof PointCustom)) {
            return false;
        }

        PointCustom user = (PointCustom) o;

        return user.x == x && user.y == y;
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
