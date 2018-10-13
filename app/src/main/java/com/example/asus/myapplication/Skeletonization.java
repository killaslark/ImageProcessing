package com.example.asus.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Winarto on 9/26/2018.
 */

public class Skeletonization {
    Bitmap bmp;
    public Skeletonization(Bitmap bmp){
        this.bmp = bmp;
        this.bmp = runAlgorithm(bmp);
    }
    private Bitmap runAlgorithm(Bitmap bmp){
        Bitmap altbmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        int[][] pixels = new int[bmp.getWidth()][bmp.getHeight()];
        for(int x = 0; x < bmp.getWidth(); x++){
            for(int y = 0; y < bmp.getHeight(); y++){
                if(isPixelBlack(bmp.getPixel(x,y))){
                    altbmp.setPixel(x, y, Color.BLACK);
                    pixels[x][y] = altbmp.getPixel(x, y);
                    //Log.d("PIXEL",Color.);
                } else {
                    pixels[x][y] = 0;
                    //altbmp.setPixel(x, y, Color.WHITE);
                }
            }
        }

        for(int x = 0; x < bmp.getWidth(); x++){
            for(int y = 0; y < bmp.getHeight(); y++){
                Log.d("PIXEL",Integer.toString(pixels[x][y]) );
                //altbmp.setPixel(x, y, pixels[x][y]);
            }
        }
        int[][] temp = pixels;
        pixels = thin(temp);
        pixels = postprocess(pixels);
        for(int y = 0; y < bmp.getHeight(); y++){
            for(int x = 0; x < bmp.getWidth(); x++){
                //Log.d("PIXEL",Integer.toString(pixels[x][y]) );
                altbmp.setPixel(x, y, pixels[x][y]);
            }
        }
        Log.d("OKK", "RUNNING");
        return altbmp;
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 40;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    private int[][] thin(int[][] pixels){
        List<Point> toWhite = new ArrayList<Point>();
        boolean change = true;
        boolean done = false;
        while (!done) {
            change = !change;
            for(int x = 0; x < pixels.length; x++){
                for(int y = 0; y < pixels[x].length; y++){
                    if(x != 0 && y != 0 && x != bmp.getWidth()-1 && y != bmp.getHeight()-1){
                        //condition 1. 2 <= p1 <= 6
                        if(pixels[x][y] == 0)
                            continue;
                        int num = conditionOne(pixels, x,y);
                        if(!(num >= 2 && num <= 6))
                            continue;
                        //condition 2 p1 = 1
                        num = conditionTwo(pixels, x, y);
                        if(!(num == 1))
                            continue;
                        if(!(passOne(pixels,x,y)) && !change)
                            continue;
                        if(!(passTwo(pixels,x,y)) && change)
                            continue;
                        //all conditions have been satisfied then replace p1 to transparent
                        toWhite.add(new Point(x, y));
                        //pixels[x][y] = 0;
                    }
                }
            }
            for (Point p : toWhite)
                pixels[p.x][p.y] = 0;
            if (toWhite.size() == 0)
                done = true;
            toWhite.clear();
        }
//        for(int y = 0; y < bmp.getHeight(); y++){
//            for(int x = 0; x < bmp.getWidth(); x++){
//                Log.d("PIXELNEW",Integer.toString(pixels[x][y]) );
//            }
//        }
        return pixels;
    }

    private int[][] postprocess(int[][]pixels) {
        List<Point> toWhite = new ArrayList<Point>();
        for(int i=0;i < 2; i++) {
            for (int x = 0; x < pixels.length; x++) {
                for (int y = 0; y < pixels[x].length; y++) {
                    int c = pixels[x][y];
                    if(c == 0)
                        continue;
                    int     e = pixels[x+1][y],
                            ne = pixels[x+1][y-1],
                            n = pixels[x][y-1],
                            nw = pixels[x-1][y-1],
                            w = pixels[x-1][y],
                            sw = pixels[x-1][y+1],
                            s = pixels[x][y+1],
                            se = pixels[x+1][y+1];

                    if(i == 0)
                    {
                        //North bias
                        if(!(c != 0 && !(n != 0 &&
                                ((e != 0 && ne == 0 && sw == 0 && (w == 0 || s == 0)) ||
                                        (w != 0 && nw == 0 && se == 0 && (e == 0 || s == 0))))))
                        {
                            toWhite.add(new Point(x,y));
                        }
                    } else {
                        //South bias
                        if(!(c != 0 && !(s != 0 &&
                                ((e != 0 && se == 0 && nw == 0 && (w == 0 || n == 0)) ||
                                        (w != 0 && sw == 0 && ne == 0 && (e == 0 || n == 0))))))
                        {
                            toWhite.add(new Point(x,y));
                        }
                    }
                }
            }
            for (Point p : toWhite)
                pixels[p.x][p.y] = 0;
            toWhite.clear();
        }
        return pixels;
    }

    private int conditionOne(int[][] pixels, int x, int y){
        int count = 0;
        if(pixels[x][y-1] != 0)
            count++;
        if(pixels[x+1][y-1] != 0)
            count++;
        if(pixels[x+1][y] != 0)
            count++;
        if(pixels[x+1][y+1] != 0)
            count++;
        if(pixels[x][y+1] != 0)
            count++;
        if(pixels[x-1][y+1] != 0)
            count++;
        if(pixels[x-1][y] != 0)
            count++;
        if(pixels[x-1][y-1] != 0)
            count++;
        return count;
    }
    private int conditionTwo(int[][] pixels, int x, int y){
        int count = 0;
        if((pixels[x][y-1] != 0) == false && (pixels[x+1][y-1] != 0) == true)
            count++;
        if((pixels[x+1][y-1] != 0) == false && (pixels[x+1][y] != 0) == true)
            count++;
        if((pixels[x+1][y] != 0) == false && (pixels[x+1][y+1] != 0) == true)
            count++;
        if((pixels[x+1][y+1] != 0) == false && (pixels[x][y+1] != 0) == true)
            count++;
        if((pixels[x][y+1] != 0) == false && (pixels[x-1][y+1] != 0) == true)
            count++;
        if((pixels[x-1][y+1] != 0) == false && (pixels[x-1][y] != 0) == true)
            count++;
        if((pixels[x-1][y] != 0) == false && (pixels[x-1][y-1] != 0) == true)
            count++;
        if((pixels[x-1][y-1] != 0) == false && (pixels[x][y-1] != 0) == true)
            count++;
        return count;
    }
    private boolean passOne(int[][] pixels, int x, int y){
        int count = 0;

        if(pixels[x][y-1] == 0)
            count++;
        if(pixels[x+1][y] == 0)
            count++;
        if(pixels[x][y+1] == 0)
            count++;
        if(pixels[x+1][y] == 0)
            count++;
        if(pixels[x][y+1] == 0)
            count++;
        if(pixels[x-1][y] == 0)
            count++;
        return count > 1;
    }
    private boolean passTwo(int[][] pixels, int x, int y){
        int count = 0;
        if(pixels[x][y-1] == 0)
            count++;
        if(pixels[x+1][y] == 0)
            count++;
        if(pixels[x-1][y] == 0)
            count++;
        if(pixels[x][y-1] == 0)
            count++;
        if(pixels[x][y+1] == 0)
            count++;
        if(pixels[x-1][y] == 0)
            count++;
        return count > 1;
    }
    public Bitmap getBitmap() {
        return bmp;
    }
}
