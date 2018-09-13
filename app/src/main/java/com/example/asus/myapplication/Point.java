package com.example.asus.myapplication;

public class Point {
    float x, y;

    public Point(){
        this.x = 0;
        this.y = 0;
    }
    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPoint(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
