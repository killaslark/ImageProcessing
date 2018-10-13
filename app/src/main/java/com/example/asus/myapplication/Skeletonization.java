package com.example.asus.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.IntegerRes;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Winarto on 9/26/2018.
 */

public class Skeletonization {
    List<Point> edge = new ArrayList<Point>();
    List<Point> intersection = new ArrayList<Point>();
    int[][] pixels;
    Bitmap bmp;

    final static int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1} ,{-1, 1} ,{-1, 0}, {-1, -1}, {0, -1}};
    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}},
                                        {{0, 2, 6}, {0, 4, 6}}};

    public int prediction = -1;
    public Skeletonization(Bitmap bmp){
        this.bmp = bmp;
        this.pixels = new int[bmp.getWidth()][bmp.getHeight()];
        this.bmp = runAlgorithm(bmp);
    }

    private Bitmap runAlgorithm(Bitmap bmp){
        List<Point> blackPoint = new ArrayList<Point>();
        Bitmap altbmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        for(int x = 0; x < bmp.getWidth(); x++){
            for(int y = 0; y < bmp.getHeight(); y++){
                if(isPixelBlack(bmp.getPixel(x,y))){
                    altbmp.setPixel(x, y, Color.BLACK);
                    pixels[x][y] = altbmp.getPixel(x, y);
                    blackPoint.add(new Point(x,y));
                    //Log.d("PIXEL",Color.);
                } else {
                    pixels[x][y] = 0;
                    //altbmp.setPixel(x, y, Color.WHITE);
                }
            }
        }
        ZhangSuenThinning(blackPoint);

        for(int y = 0; y < bmp.getHeight(); y++){
            for(int x = 0; x < bmp.getWidth(); x++){
                //Log.d("PIXEL",Integer.toString(pixels[x][y]) );
                altbmp.setPixel(x, y, pixels[x][y]);
            }
        }

        Log.d("OKK", "RUNNING");
        generateGeometricProperty(pixels);
        prediction = predict();
        Log.d("OKK", Integer.toString(intersection.size()));
        return altbmp;
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 40;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    // Credits to https://rosettacode.org/wiki/Zhang-Suen_thinning_algorithm
    private void ZhangSuenThinning(List<Point> input) {
        List<Point> toWhite = new ArrayList<Point>();
        List<Point> blackPoint = input;
        List<Integer> changedIndex = new ArrayList<Integer>();
        boolean noChange = false;
        boolean firstStep = false;
        while (firstStep || !noChange) {
            firstStep = !firstStep;
            for(int i = 0; i < blackPoint.size(); i++) {
                int x = blackPoint.get(i).x;
                int y = blackPoint.get(i).y;
                // Handle array out of bounds
                if (x > 0 && x < bmp.getWidth()-1 && y > 0 && y < bmp.getHeight()-1) {
                    // 1. Black Pixel Neighbor >= 2 and <= 6
                    int nn = countNeighbors(x, y);
                    if (nn < 2 || nn > 6)
                        continue;
                    // 2. Number white to black transition = 1
                    if (countTransitions(x, y) != 1)
                        continue;
                    // 3. Check if at least one is white from determined neighbor
                    if(!atLeastOneIsWhite(x, y, firstStep ? 0 : 1))
                        continue;
                    // 4. If all rule is satisfied, mark pixel for deletion
                    toWhite.add(new Point(x, y));
                    changedIndex.add(i);
                }
            }
            if(toWhite.isEmpty()) {
                noChange = true;
            }
            for(Point p : toWhite)
                pixels[p.x][p.y] = 0;
            toWhite.clear();

            for(int i = changedIndex.size()-1; i >= 0; i--) {
                int index = changedIndex.get(i);
                blackPoint.remove(index);
            }
            changedIndex.clear();
        }
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int i = 0; i < nbrs.length-1; i++) {
            if(pixels[x + nbrs[i][0]][y + nbrs[i][1]] != 0) {
                count++;
            }
        }
        return count;
    }

    private int countTransitions(int x, int y) {
        int count = 0;
        for (int i = 0; i < nbrs.length-1; i++) {
            if(pixels[x + nbrs[i][0]][y + nbrs[i][1]] == 0) {
                if(pixels[x + nbrs[i+1][0]][y + nbrs[i+1][1]] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean atLeastOneIsWhite(int x, int y, int step) {
        int count = 0;
        int[][] group = nbrGroups[step];
        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < group[i].length; j++) {
                int[] nbr = nbrs[group[i][j]];
                if(pixels[x + nbr[0]][y + nbr[1]] == 0) {
                    count++;
                    break;
                }
            }
        }
        return count > 1;
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

    private void generateGeometricProperty(int[][] pixels) {
        int neighbor = 0;
        for(int i = 1; i< bmp.getWidth()-1;i++) {
            for(int j = 1; j < bmp.getHeight()-1;j++) {
                if(isPixelBlack(pixels[i][j])) {
                    neighbor = conditionOne(pixels, i, j);
                    if (neighbor == 1) {
                        edge.add(new Point(i,j));
                    } else if (neighbor >= 3)
                        intersection.add(new Point(i,j));
                }
            }
        }
    }

    public int predict() {
        // Sort endpoints by increasing y value
        Collections.sort(edge, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int y = Integer.compare(o1.y, o2.y);
                if (y != 0)
                    return y;
                return Integer.compare(o1.x, o2.x);
            }
        });
        // Combine close intersections into one
        Iterator<Point> it = intersection.iterator();
        if (it.hasNext()) {
            Point curr = it.next();
            while (it.hasNext()) {
                Point next = it.next();
                if (Math.abs(curr.x - next.x) < 5 && Math.abs(curr.y - next.y) < 5) {
                    it.remove();
                } else {
                    curr = next;
                }
            }
        }
        // Count number of endpoints and intersections
        switch (edge.size()) {
            case 0: // 0, 8
                switch (intersection.size()) {
                    case 0:
                        return 0;
                    default:
                        return 8;
                }
            case 1: // 6, 9
                switch (intersection.size()) {
                    case 1:
                        Point e = edge.get(0);
                        Point i = intersection.get(0);
                        if (e.y > i.y) {
                            return 9;
                        } else {
                            return 6;
                        }
                }
            case 2: // 2, 4, 5, 7
                switch (intersection.size()) {
                    case 0:
                        Point e0 = edge.get(0);
                        Point e1 = edge.get(1);
                        if (e0.x > e1.x) {
                            return 5;
                        }
                        if (e1.x > bmp.getWidth() / 2) {
                            return 2;
                        } else {
                            return 7;
                        }
                    case 1:
                        return 4;
                }
            case 3: // 1, 3
                switch (intersection.size()) {
                    case 1:
                        Point e0 = edge.get(0);
                        Point e1 = edge.get(1);
                        Point e2 = edge.get(2);
                        if (e1.x < e0.x && e1.x < e2.x) {
                            return 1;
                        } else if (e1.x > e0.x && e1.x > e2.x) {
                            return 3;
                        }
                }
        }
        return -1;
    }
}
