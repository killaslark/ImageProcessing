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
    List<Point> blackPoint = new ArrayList<Point>();
    List<Point> edge = new ArrayList<Point>();
    List<Point> intersection = new ArrayList<Point>();
    int[][] pixels;
    Bitmap bmp;

    final static int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1} ,{-1, 1} ,{-1, 0}, {-1, -1}, {0, -1}};
    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}},
                                        {{0, 2, 6}, {0, 4, 6}}};
    public int prediction = -1;
    public Skeletonization(Bitmap bmp){
        this.blackPoint.clear();
        this.edge.clear();
        this.intersection.clear();
        this.bmp = bmp;
        this.pixels = new int[bmp.getWidth()][bmp.getHeight()];
        this.bmp = runAlgorithm(bmp);
    }

    private Bitmap runAlgorithm(Bitmap bmp){
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
        ZhangSuenThinning();
        pixels = postprocess(pixels);
        pixels = removeFalseEndpoint(pixels);
        for(int y = 0; y < bmp.getHeight(); y++){
            for(int x = 0; x < bmp.getWidth(); x++){
                //Log.d("PIXEL",Integer.toString(pixels[x][y]) );
                altbmp.setPixel(x, y, pixels[x][y]);
            }
        }

        Log.d("OKK", "RUNNING");
        prediction = predict();
        Log.d("PREDICT", Integer.toString(prediction));
        return altbmp;
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 40;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    // Credits to https://rosettacode.org/wiki/Zhang-Suen_thinning_algorithm
    private void ZhangSuenThinning() {
        List<Point> toWhite = new ArrayList<Point>();
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

    private int[][] postprocess(int[][] pixels) {
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

    private int[][] removeFalseEndpoint(int[][] pixels){
        List<Point> toWhite = new ArrayList<Point>();
        int threshold = 15;
        //Hilangin cabang
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                if(pixels[x][y] == 0)
                    continue;

                int numOfBlack = conditionOne(pixels,x,y);
                if(numOfBlack == 1){
                    Log.d("ENDPOINT", Integer.toString(x) + " " + Integer.toString(y));
                    int i = 0;
                    List<Point> sequenceOfBlack = new ArrayList<>();
                    sequenceOfBlack.add(new Point(x,y));
                    for(i = 0;i < threshold;i++){
                        List<Point> blackNeighbor = getBlackNeighbor(pixels,sequenceOfBlack.get(sequenceOfBlack.size()-1).x,
                                sequenceOfBlack.get(sequenceOfBlack.size()-1).y);
                        if(blackNeighbor.size() <= 2){
                            for (Point p : blackNeighbor) {
                                boolean add = true;
                                for(Point p1 : sequenceOfBlack) {
                                    if(p.x == p1.x && p.y == p1.y)
                                        add =false;
                                }
                                if(add)
                                    sequenceOfBlack.add(p);
                            }
                        } else if(blackNeighbor.size() > 3) {
                            //Asumsi size > 3 tidak boleh dihapus cabangnya (cth angka 4)
                            i = threshold;
                            break;
                        }else{
                            //Penanganan supaya titik tidak putus
                            for(int j = 0; j < blackNeighbor.size();j++){
                                for(Point p1 : sequenceOfBlack) {
                                    if(blackNeighbor.get(j).x == p1.x && blackNeighbor.get(j).y == p1.y) {
                                        blackNeighbor.remove(j);
                                        break;
                                    }
                                }
                            }
                            int xdiff1 = sequenceOfBlack.get(sequenceOfBlack.size()-1).x-blackNeighbor.get(0).x,
                                    ydiff1 = sequenceOfBlack.get(sequenceOfBlack.size()-1).y - blackNeighbor.get(0).y,
                                    xdiff2 = sequenceOfBlack.get(sequenceOfBlack.size()-1).x - blackNeighbor.get(1).x,
                                    ydiff2 = sequenceOfBlack.get(sequenceOfBlack.size()-1).y - blackNeighbor.get(1).y;
                            if(Integer.signum(xdiff1) == -Integer.signum(xdiff2) || Integer.signum(ydiff1) == -Integer.signum(ydiff2))
                                sequenceOfBlack.remove(sequenceOfBlack.size()-1);
                            break;
                        }
                    }
                    for (Point p : sequenceOfBlack)
                        Log.d("BLACK", Integer.toString(p.x) + " " + Integer.toString(p.y));
                    if(i < threshold)
                    {
                        for (Point p : sequenceOfBlack)
                            pixels[p.x][p.y] = 0;
                    }
                    sequenceOfBlack.clear();
                }
            }
        }
        return pixels;
    }

    private List<Point> getBlackNeighbor(int[][] pixels, int x, int y){
        List<Point> black = new ArrayList<Point>();
        if(pixels[x][y-1] != 0)
            black.add(new Point(x,y-1));
        if(pixels[x+1][y-1] != 0)
            black.add(new Point(x+1,y-1));
        if(pixels[x+1][y] != 0)
            black.add(new Point(x+1,y));
        if(pixels[x+1][y+1] != 0)
            black.add(new Point(x+1,y+1));
        if(pixels[x][y+1] != 0)
            black.add(new Point(x,y+1));
        if(pixels[x-1][y+1] != 0)
            black.add(new Point(x-1,y+1));
        if(pixels[x-1][y] != 0)
            black.add(new Point(x-1,y));
        if(pixels[x-1][y-1] != 0)
            black.add(new Point(x-1,y-1));
        return black;
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

    private void updateBlackPoint() {
        blackPoint.clear();
        for(int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                if(pixels[x][y] != 0) {
                    blackPoint.add(new Point(x, y));
                }
            }
        }
    }

    private void updateIntersection() {
        intersection.clear();
        for(int i = 0; i < blackPoint.size(); i++) {
            int x = blackPoint.get(i).x;
            int y = blackPoint.get(i).y;
            if (countNeighbors(x, y) > 2)
                intersection.add(blackPoint.get(i));
        }
    }

    private void updateEdge() {
        edge.clear();
        for(int i = 0; i < blackPoint.size(); i++) {
            int x = blackPoint.get(i).x;
            int y = blackPoint.get(i).y;
            if (countNeighbors(x, y) == 1)
                edge.add(blackPoint.get(i));
        }
    }

    public int predict() {
        updateBlackPoint();
        updateEdge();
        updateIntersection();
        int n_int = intersection.size();
        int n_edge = edge.size();
        Log.d("PREDICT EDGE", Integer.toString(n_edge));
        Log.d("PREDICT INTERSECTION", Integer.toString(n_int));

        if (n_edge == 0) {
            // 0 or 8
            if (n_int > 0) {
                return 8;
            } else {
                return 0;
            }
        } else if (n_edge == 1 && n_int == 1) {
            // 6 or 9
            if (intersection.get(0).y > edge.get(0).y) {
                return 6;
            } else {
                return 9;
            }
        } else if (n_edge == 2) {
            // 1 or 2 or 3 or 4 or 5 or 7
            if (n_int > 0) {
                return 4;
            } else {
                // 1 or 2 or 3 or 5 or 7
                Point edge_0 = edge.get(0);
                Point edge_1 = edge.get(1);
                int xdiff = edge_0.x - edge_1.x;
                int ydiff = edge_0.y - edge_1.y;
                Log.d("PREDICT XDIFF", Integer.toString(xdiff));
                Log.d("PREDICT YDIFF", Integer.toString(ydiff));
                if (Math.abs(xdiff) < 5) {
                    return 3;
                } else if (xdiff < 0 && ydiff > 0) {
                    return 5;
                } else {
                    float manhattan_distance = Math.abs(xdiff) + Math.abs(ydiff);
                    Log.d("PREDICT AREA", Integer.toString(bmp.getHeight() * bmp.getWidth()));
                    manhattan_distance = manhattan_distance * 1;
                    Log.d("PREDICT MANHATTAN", Integer.toString((int)manhattan_distance));
                    if(manhattan_distance >= 65 && manhattan_distance < 75) {
                        return 7;
                    } else if (manhattan_distance >= 75 && manhattan_distance < 95) {
                        return 2;
                    } else if (manhattan_distance >= 95 && manhattan_distance < 105) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        } else {
            return -1;
        }
    }
}
