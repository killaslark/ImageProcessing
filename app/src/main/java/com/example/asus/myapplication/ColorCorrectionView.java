package com.example.asus.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
/**
 * Created by Winarto on 9/11/2018.
 */

public class ColorCorrectionView extends View{
    class Point {
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

    Paint paint;
    Path path;
    boolean touching = false;
    //Touch point
    float x, y;
    //Bezier point
    Point[] bezier;
    Point firstTouch;
    Point offset;
    int selectedIndex = -1;

    public ColorCorrectionView(Context context) {
        super(context);
        init();
    }

    public ColorCorrectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorCorrectionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        paint = new Paint();

        paint.setStyle(Paint.Style.STROKE);
        firstTouch = new Point();
        offset = new Point();
        bezier = new Point[4];
        bezier[0] = new Point(50,560);
        bezier[1] = new Point(178, 432);
        bezier[2] = new Point(306,304);
        bezier[3] = new Point(560, 50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        getParent().requestDisallowInterceptTouchEvent(true);
        if(touching){
            if(selectedIndex == -1) {
                double closestPoint = Math.hypot(x - bezier[0].x, y - bezier[0].y);
                selectedIndex = 0;
                for (int i = 1; i < bezier.length; i++) {
                    if (Math.hypot(x - bezier[i].x, y - bezier[i].y) < closestPoint)
                    {
                        closestPoint = Math.hypot(x - bezier[i].x, y - bezier[i].y);
                        selectedIndex = i;
                    }
                }
                firstTouch.setPoint(x,y);
                offset.setPoint(bezier[selectedIndex].x-x, bezier[selectedIndex].y-y);
            } else {
                float yMove = y - firstTouch.y;
                bezier[selectedIndex].y = firstTouch.y + offset.y + yMove;
            }
            Log.d("Touched ", "X: " + Float.toString(x) + "Y: " + Float.toString(y));
        } else {
            selectedIndex = -1;
        }

        path = new Path();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        path.moveTo(bezier[0].x, bezier[0].y);
        path.cubicTo(bezier[1].x, bezier[1].y, bezier[2].x, bezier[2].y , bezier[3].x, bezier[3].y);
        canvas.drawPath(path, paint);

        /*path.reset();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        path.moveTo(50, 50);
        path.lineTo(300, 50);
        path.lineTo(100, 400);
        path.lineTo(400, 400);*/

        PathMeasure pm = new PathMeasure(path, false);
        float aCoordinates[] = {0f, 0f};

        pm.getPosTan(pm.getLength() * 0.1f, aCoordinates, null);
        for(int i = 0;i < aCoordinates.length;i++)
        {
            Log.d("Coordinate ", "X:" + Float.toString(aCoordinates[0]) + ", Y: " + Float.toString(aCoordinates[1]));
        }
        canvas.drawPath(path, paint);

    }

    public Point[] getPoints() {
        Point[] pointArray = new Point[256];
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        float distance = 0f;
        float speed = length / 256;
        int counter = 0;
        float[] aCoordinates = new float[2];

        while ((distance < length) && (counter < 256)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);
            pointArray[counter] = new Point(aCoordinates[0],
                    aCoordinates[1]);
            counter++;
            distance = distance + speed;
        }

        return pointArray;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch(action){
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                touching = true;
                break;
            default:
                touching = false;
        }
        invalidate();

        return true;
    }
}
