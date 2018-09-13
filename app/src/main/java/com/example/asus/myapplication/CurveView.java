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

public class CurveView extends View{
    Paint paint;
    Path path;
    Path controlPath;
    boolean touching = false;
    //Touch point
    float x, y;
    //Bezier point
    Point[] bezier;
    Point firstTouch;
    Point offset;
    int selectedIndex = -1;
    int color;

    public CurveView(Context context) {
        super(context);
        init();
    }

    public CurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurveView(Context context, AttributeSet attrs, int defStyle) {
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
        bezier[1] = new Point(220, 390);
        bezier[2] = new Point(390,220);
        bezier[3] = new Point(560, 50);
     }

     public void setGraphColor(int c) {
        color = c;
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

        controlPath = new Path();
        paint.setStrokeWidth(1);
        paint.setColor(Color.BLACK);
        controlPath.moveTo(bezier[0].x, bezier[0].y);
        controlPath.lineTo(bezier[1].x, bezier[1].y);
        controlPath.lineTo(bezier[2].x, bezier[2].y);
        controlPath.lineTo(bezier[3].x, bezier[3].y);
        canvas.drawPath(controlPath, paint);
        for(int i = 0; i < bezier.length; i++) {
            //paint.setColor(Color.RED);
            //float[] c = new float[2];
            //c = getPathCoordinate(path, 0.33f * i);
            //canvas.drawCircle(c[0], c[1], 5f, paint);
            paint.setStrokeWidth(2);
            canvas.drawCircle(bezier[i].x, bezier[i].y, 5f, paint);
        }

        path = new Path();
        paint.setStrokeWidth(3);
        paint.setColor(color);
        path.moveTo(bezier[0].x, bezier[0].y);
        for(int i = 1; i < 4; i++) {
            path.quadTo(bezier[i-1].x, bezier[i-1].y, (bezier[i-1].x+bezier[i].x)/2, (bezier[i-1].y+bezier[i].y)/2);
        }
        path.lineTo(bezier[3].x, bezier[3].y);
        //path.cubicTo(bezier[1].x, bezier[1].y, bezier[2].x, bezier[2].y , bezier[3].x, bezier[3].y);
        canvas.drawPath(path, paint);

    }

    private float[] getPathCoordinate(Path p, float x) {
        PathMeasure pm = new PathMeasure(p, false);
        float length = pm.getLength();
        float multiplier = x;
        if (x < 0.0) {multiplier = 0.0f;}
        else if (x > 1.0) {multiplier = 1.0f;}
        float distance = multiplier * length;
        float[] coordinate = new float[2];
        pm.getPosTan(distance, coordinate, null);

        return (coordinate);
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
