package com.example.asus.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.IntegerRes;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created by Winarto on 9/26/2018.
 */

public class Skeletonization {
    List<Point> blackPoint = new ArrayList<Point>();
    List<Point> edge = new ArrayList<Point>();
    List<Point> intersection = new ArrayList<Point>();
    List<Point> intersectionMoreThanThree = new ArrayList<Point>();
    List<Point> intersectionThree = new ArrayList<Point>();
    List<Point> neighbourIntersection = new ArrayList<Point>();
    int[][] pixels;
    Bitmap bmp,realBitmap;
    int thresholdPoint;

    final static int[][] nbrs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1} ,{-1, 1} ,{-1, 0}, {-1, -1}, {0, -1}};
    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}},
                                        {{0, 2, 6}, {0, 4, 6}}};
    public char prediction = '-';
    public Skeletonization(Bitmap bmp, int threshold){
        this.thresholdPoint = threshold;
        this.blackPoint.clear();
        this.edge.clear();
        this.intersection.clear();
        this.realBitmap = bmp;
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
        Log.d("PREDICT", ""+prediction);
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
        int threshold = thresholdPoint*blackPoint.size() /1000;
        Log.d("threshold : ", ""+threshold);
        //Hilangin cabang
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                if(pixels[x][y] == 0)
                    continue;
                int numOfBlack = conditionOne(pixels,x,y);
                if(numOfBlack == 1){
//                    Log.d("ENDPOINT", Integer.toString(x) + " " + Integer.toString(y));
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
                                        add = false;
                                }
                                if(add)
                                    sequenceOfBlack.add(p);
                            }
                        } else if (blackNeighbor.size() > 3 ) {
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
//                    for (Point p : sequenceOfBlack)
//                        Log.d("BLACK", Integer.toString(p.x) + " " + Integer.toString(p.y));
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
    public Bitmap getRealBitmap() {return realBitmap;}

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
            if (countNeighbors(x, y) > 2) {
                intersection.add(blackPoint.get(i));
                if (countNeighbors(x, y) == 3) {
                    intersectionThree.add(blackPoint.get(i));
                } else {
                    intersectionMoreThanThree.add(blackPoint.get(i));
                }
            }
        }
    }

    private void setNeighborIntersection(List<Point> intersection) {
        List<Point> temp = new ArrayList<Point>();
        double threshold = thresholdPoint*blackPoint.size() /2000;
        for(int i = 0; i < intersection.size(); i++) {
            for(int j = i + 1; j < intersection.size(); j++) {
                double distance;
                distance = Math.sqrt( Math.pow(intersection.get(j).x - intersection.get(i).x,2) + Math.pow(Math.abs(intersection.get(j).y - intersection.get(i).y),2));
                if (distance < threshold) {
                    temp.add(intersection.get(j));
                }
            }
        }
        neighbourIntersection = new ArrayList<Point>(
                new HashSet<>(temp));
        intersection.removeAll(neighbourIntersection);
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

    public char predict() {
        int[] chainFrequency = new int[8];
        chainFrequency = getChainFrequencyFromBitmap(realBitmap);
        double[] normalizedFreq = normalize10Histogram(chainFrequency);
        updateBlackPoint();
        updateEdge();
        updateIntersection();
        setNeighborIntersection(intersection);
        int n_int = intersection.size();
        sortPoint(edge);
        sortPoint(intersection);

        setNeighborIntersection(intersectionThree);
        int n_int_3 = intersectionThree.size();

        setNeighborIntersection(intersectionMoreThanThree);
        int n_int_more_3 = intersectionMoreThanThree.size();

        int n_edge = edge.size();
        Log.d("PREDICT EDGE", Integer.toString(n_edge));
        Log.d("PREDICT INTERSECT", Integer.toString(n_int));
        Log.d("PREDICT INTERSECT 3", Integer.toString(n_int_3));
        Log.d("PREDICT INTERSECT > 3", Integer.toString(n_int_more_3));

        if (n_edge == 0) {
            // o, 0, 8, B, D, O

            //B,8
            if (n_int == 2) {
                if (normalizedFreq[7] < 0.01) {
                    return 'B';
                } else {
                    return '8';
                }
            } else {
                if (normalizedFreq[7] < 0.01) {
                    return 'D';
                } else if ( Math.abs((normalizedFreq[0] / normalizedFreq[2]) - 0.5) < 0.1) {
                    return '0';
                } else {
                    return 'o';
                }
            }
        } else if (n_edge == 1) {
            // @, P, 6 or 9, e

            //6
            if (intersection.get(0).y > edge.get(0).y) {
                return '6';
            //@, P, 9, e
            } else {
                if (normalizedFreq[6] > 0.25) {
                    return 'P';
                } else {

                    if (intersection.get(0).x < edge.get(0).x) {
                        if (intersection.get(0).x < bmp.getWidth()/2) {
                            return 'e';
                        } else {
                            return '@';
                        }
                    } else {
                        return '9';
                    }

                }
            }
        } else if (n_edge == 2) {
            // -,2, 4,5 ,7, A, I,/, <, >, C, J , L , M , N, Q, R, S, U, V, W, Z, ^,[, ], {, }, ~, (, ) , a, b, d, g, p, q, s, w v

            if (n_int == 0) {
                //5, 2, I,-, 7, / , <, >, C, J, L, M, N, S, U, V, W, Z. ^, [,],{,},~, G,
                Point edge_0 = edge.get(0);
                Point edge_1 = edge.get(1);

                // - U, V, W, ^, _, M, v, ~
                if (Math.abs(edge_0.y - edge_1.y) < (0.06*bmp.getHeight())) {
                    for(int i = 0; i < 8; i++) {
                        Log.d(""+i, ""+chainFrequency[i]+" "+normalizedFreq[i]);
                    }
                    // M, _
                    if (edge_0.y > 0.6*bmp.getHeight()){
                        if (normalizedFreq[0] + normalizedFreq[4] > 0.7) {
                            return '_';
                        } else {
                            return 'M';
                        }
                    // -, ~
                    } else  if (normalizedFreq[0] + normalizedFreq[4] > 0.5){
                        if (normalizedFreq[0] + normalizedFreq[4] > 0.7) {
                            return '-';
                        } else {
                            return '~';
                        }
                    // U, V, W,w ,v, ^
                    } else if (normalizedFreq[1] + normalizedFreq[7] > 0.2){
                        return '^';
                    } else if (normalizedFreq[3] + normalizedFreq[5] < 0.1) {
                        return 'U';
                    } else {
                        if (normalizedFreq[3] + normalizedFreq[5] > 0.15) {
                            return 'V';
                        } else {
                            return 'W';
                        }
                    }


                    // <,C.[,{, (, c
                } else if (edge_0.x > (bmp.getWidth()/2) && edge_1.x > (bmp.getWidth()/2)) {
                    if (normalizedFreq[5] < 0.05 && normalizedFreq[7] < 0.05){
                        return '[';
                    } else if (Math.abs(edge_1.x - edge_0.x) > 0.1*bmp.getWidth()) {
                        return 'G';
                    }  else if (normalizedFreq[6] > 0.2) {
                        if (normalizedFreq[4] > 0.07) {
                            return '{';
                        } else {
                            return '(';
                        }
                    }  else if (normalizedFreq[4] > 0.2){
                        return '<';
                    // c, C
                    } else {
                        return 'C';
                    }
                // >, 7,],}, ), I,l
                } else if (edge_0.x < (bmp.getWidth()/2) && edge_1.x < (bmp.getWidth()/2)) {
                    // I,],l
                    if (normalizedFreq[1] < 0.008 && normalizedFreq[3] < 0.008){
                        // I,l
                        if (normalizedFreq[5] == 0) {
                             return 'I';
                         } else {
                             return ']';
                         }
                     } else if (normalizedFreq[1] < 0.01) {
                         return '7';
                     } else if (normalizedFreq[4] < 0.07) {
                         return ')';
                     } else if (normalizedFreq[6] > 0.2) {
                         return '}';
                     } else {
                         return '>';
                     }

                // 5, N , S, /, J,
                } else if (edge_0.x > edge_1.x) {
                    for(int i = 0; i < 8; i++) {
                        Log.d(""+i, ""+chainFrequency[i]+" "+normalizedFreq[i]);
                    }
                    // N,J
                    if (normalizedFreq[1] + normalizedFreq[5] < 0.01) {
                        return '/';
                    } else if (normalizedFreq[2] + normalizedFreq[6] > 0.5) {
                        if (normalizedFreq[1] + normalizedFreq[5] > 0.14) {
                            return 'N';
                        } else {
                            return 'J';
                        }
                    // S, 5, /, s
                    } else {
                            if ( normalizedFreq[2] + normalizedFreq[6] > 0.3) {
                                return '5';
                            // s,S
                            } else {
                                return 'S';
                            }
                    }

                // 2, L, Z,\
                } else if (edge_0.x  < edge_1.x ){
                    for(int i = 0; i < 8; i++) {
                        Log.d(""+i, ""+chainFrequency[i]+" "+normalizedFreq[i]);
                    }

                    if (normalizedFreq[2] + normalizedFreq[6] > 0.6 ) {
                        if (normalizedFreq[0] + normalizedFreq[4] > 0.2) {
                            return 'L';
                        } else {
                            return '\\';
                        }
                    } else {
                        if (normalizedFreq[1] + normalizedFreq[5] > 0.05) {
                            return '2';
                        //z,Z,\
                        } else if (normalizedFreq[7] + normalizedFreq[3] < 0.01) {
                            return '\\';
                        } else {
                            return 'Z';
                        }
                     }
                }

            // 4
            } else if (n_int == 1) {
                    return '4';

            // A ,R, Q , a, b, d, g, p, q
            } else if (n_int == 2) {
                // a, b, d, g, p, q, Q

                Point edge_0 = edge.get(0);
                Point edge_1 = edge.get(1);
                Point intersec_0 = intersection.get(0);
                Point intersec_1 = intersection.get(1);


                //a , b, d, g , p, q, Q
                if (edge_0.y < intersection.get(0).y ) {

                    //a, b, p
                    if (edge_0.x < bmp.getWidth()/2) {
                        if (intersection.get(0).x > bmp.getWidth()/2) {
                            return 'a';
                        } else {
                            if ((intersec_0.y - edge_0.y) > (edge_1.y - intersec_1.y)) {
                                return 'b';
                            } else {
                                return 'p';
                            }
                        }
                    //d,g,q,Q
                    } else {
                        if (intersec_0.y > bmp.getHeight()/2 && intersec_1.y > bmp.getHeight()/2) {
                            return 'Q';
                        }
                        if (edge_1.x < bmp.getWidth()/2) {
                            return 'g';
                        } else if ((intersec_0.y - edge_0.y) > (edge_1.y - intersec_1.y)){
                            return 'd';
                        } else {
                            return 'q';
                        }
                    }
                // A R
                } else {
                    if (normalizedFreq[7] < 0.01) {
                        return 'R';
                    } else {
                        return 'A';
                    }
                }
            } else if (n_int == 4) {
                return '&';
            }
        // 1,3, E, , T, Y, F, n , r, h, y, u
        } else if (n_edge == 3){

            Point edge_0 = edge.get(0);
            Point edge_1 = edge.get(1);
            Point edge_2 = edge.get(2);

            // E,F,
            if (edge_0.x > bmp.getWidth()/2 && edge_1.x > bmp.getWidth()/2  ) {
                if (edge_2.x > bmp.getWidth()/2) {
                    return 'E';
                } else {
                    return 'F';
                }

            // n,h, 1
            } else if (edge_1.y > 0.8*bmp.getHeight() && edge_2.y > 0.8*bmp.getHeight()) {
                if ( Math.abs(edge_1.y - intersection.get(0).y) < 0.025*bmp.getHeight()) {
                    return '1';
                } else if ( (intersection.get(0).y - edge_0.y) > 0.3*(edge_1.y - intersection.get(0).y)) {
                    return 'h';
                } else {
                    return 'n';
                }
            // T,y, Y, r, u
            } else if (Math.abs(edge_0.y - edge_1.y) < 0.025*bmp.getHeight()) {
                if (Math.abs(edge_0.x - intersection.get(0).x) < 0.025 * bmp.getWidth() || Math.abs(edge_1.x - intersection.get(0).x) < 0.025 * bmp.getWidth()) {
                    if (intersection.get(0).x > 0.5*bmp.getWidth()) {
                        return 'u';
                    } else {
                        return 'r';
                    }
                }else {
                    if (Math.abs(edge_0.y - intersection.get(0).y) < 0.025 * bmp.getHeight()) {
                        return 'T';
                    } else{
                        if (Math.abs(edge_2.x - intersection.get(0).x) < 0.025 * bmp.getHeight()) {
                            return 'Y';
                        } else {
                            return 'y';
                        }
                    }

                }
            // 3, r (kalo kebetulan tanduk r nya pendek)
            } else if (edge_1.x > intersection.get(0).x) {
                return 'r';
            } else {
                return '3';
            }
        } else if (n_edge == 4) {
            // x,X, +, f, H, $, t,
            Point edge_0 = edge.get(0);
            Point edge_1 = edge.get(1);
            Point edge_2 = edge.get(2);
            Point edge_3 = edge.get(3);
            Log.d("E0", edge_0.toString());
            Log.d("E1", edge_1.toString());
            Log.d("E2", edge_2.toString());
            Log.d("E3", edge_3.toString());

            if (n_int == 1) {
                //X,x, t f,
                if (edge_0.x < edge_1.x)   {
                        return 'x';
                } else {
                    if (edge_0.x >edge_3.x) {
                        return 'f';
                    } else {
                        return 't';
                    }
                }
            // H, m, K,k
            } else if (n_int == 2) {
                if (edge_1.y > intersection.get(0).y && edge_2.y > intersection.get(0).y && edge_3.y > intersection.get(0).y) {
                    return 'm';
                // H,K, k
                } else{
                    for(int i = 0; i < 8; i++) {
                        Log.d(""+i, ""+chainFrequency[i]+" "+normalizedFreq[i]);
                    }
                    if (normalizedFreq[0] + normalizedFreq[4] > 0.2) {
                        return 'H';
                    } else {
                        if ((edge_1.y - edge_0.y) < 0.5*(intersection.get(0).y - edge_0.x ) && (edge_1.x - intersection.get(1).x) > 0.1*bmp.getWidth()) {
                            return 'k';
                        } else {
                            return 'K';
                        }
                    }
                }
            } else if (n_int == 4) {
                return '$';
            }
        } else if (n_edge == 5) {
            return '*';
        } else if (n_edge == 8) {
            return '#';
        }
        return '-';
    }

    public void sortPoint(List<Point> listPoint) {
        int n = listPoint.size();
        Point temp;

        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if(listPoint.get(j-1).y > listPoint.get(j).y){
                    //swap elements
                    temp = listPoint.get(j-1);
                    listPoint.set(j-1,listPoint.get(j));
                    listPoint.set(j,temp);
                } else if (listPoint.get(j-1).y == listPoint.get(j).y){
                    if (listPoint.get(j-1).x > listPoint.get(j).x) {
                        temp = listPoint.get(j-1);
                        listPoint.set(j-1,listPoint.get(j));
                        listPoint.set(j,temp);
                    }
                }
            }
        }

//        for(int i=0; i < n; i++){
//            int j=1;
//            while (j < (n-i)) {
//                int k = j;
//                while (listPoint.get(j - 1).x == listPoint.get(k).x) {
//                    k++;
//                }
//                for (int x = j - 1; x < k; x++) {
//                    for (int y = j; y < (k - 1); y++) {
//                        if (listPoint.get(j - 1).x > listPoint.get(j).x) {
//                            //swap elements
//                            temp = listPoint.get(j - 1);
//                            listPoint.set(j - 1, listPoint.get(j));
//                            listPoint.set(j, temp);
//                        }
//                    }
//
//                    j++;
//                }
//            }
//        }

    }

    public int[] getChainFrequencyFromBitmap(Bitmap bitmap) {
        int[] chainFrequency = new int[8];
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Vector chainCode = new Vector();
        int pixel;
        int iStart = 0,jStart = 0;
        boolean found = false;

        for (int j = 0; j < height && !found; j++) {
            for (int i = 0; i < width && !found; i++){
                pixel = bitmap.getPixel(i,j);
                if (isPixelBlack(pixel)) {
                    // i itu x, j itu y
                    iStart = i;
                    jStart = j;
                    found = true;
                }
            }
        }
        int iKeliling = iStart, jKeliling = jStart;
        // Mulai keliling
        checkTimur(iKeliling, jKeliling, bitmap, chainCode, chainFrequency, iStart, jStart);

        return chainFrequency;
    }

    public char predictFromChaincode(double[][] characterFrequency,char[] character, int[] chainFrequency) {
        double minSum = -1;
        char idx = '-';
        double[] normalizedFreq = normalize10Histogram(chainFrequency);
        for(int i = 0;i<12;i++) {
            double sum = 0;
            for (int j = 0; j < 8; j++) {
                sum += Math.abs(characterFrequency[i][j] - normalizedFreq[j]);
            }
            if (sum < minSum || minSum == -1) {
                idx = character[i];
                minSum = sum;
            }
        }
        return idx;
    }


    public void checkTimur(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getWidth()) {
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling);
            if (isPixelBlack(pixel)) {
                chainCode.add(0);
                chainFrequency[0]++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkUtara(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkTenggara(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }
        }
    }

    public  void checkTenggara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getHeight() && iKeliling + 1 < bitmap.getWidth()){
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(1);
                chainFrequency[1]++;
                jKeliling++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkSelatan(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }
        }
    }
    public  void checkSelatan(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getHeight()){
            int pixel = bitmap.getPixel(iKeliling, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(2);
                chainFrequency[2]++;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkBaratDaya(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }
        }
    }
    public  void checkBaratDaya(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (iKeliling - 1 >= 0 && jKeliling + 1 < bitmap.getHeight()){
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(3);
                chainFrequency[3]++;
                iKeliling--;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkBarat(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }

        }
    }
    public  void checkBarat(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (iKeliling - 1 >= 0){
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling );
            if (isPixelBlack(pixel)) {
                chainCode.add(4);
                chainFrequency[4]++;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkSelatan(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkBaratLaut(iKeliling, jKeliling, bitmap, chainCode,chainFrequency, iStart, jStart);
            }
        }
    }
    public  void checkBaratLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (jKeliling - 1 >= 0 && iKeliling -1 >= 0){
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(5);
                chainFrequency[5]++;
                jKeliling--;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkUtara(iKeliling, jKeliling, bitmap, chainCode,chainFrequency, iStart, jStart);
            }
        }
    }
    public  void checkUtara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (jKeliling - 1 >= 0){
            int pixel = bitmap.getPixel(iKeliling, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(6);
                chainFrequency[6]++;
                jKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkTimurLaut(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }
        }
    }
    public  void checkTimurLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode,int[] chainFrequency, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getHeight() && jKeliling - 1 >= 0){
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(7);
                chainFrequency[7]++;
                jKeliling--;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
                }
            } else {
                checkTimur(iKeliling,jKeliling,bitmap,chainCode,chainFrequency,iStart,jStart);
            }

        }
    }

    private double[] normalize10Histogram(int[] arr) {
        double[] newHist = new double[arr.length];
        int sum = 0;
        for(int i = 0;i < arr.length;i++)
            sum += arr[i];
        for(int i = 0;i < arr.length;i++)
            newHist[i] = (float)arr[i] / (float)sum;

        return newHist;
    }
}
