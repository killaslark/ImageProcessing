package com.example.asus.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CurveView valueCurveView;
    private CurveView redCurveView;
    private CurveView greenCurveView;
    private CurveView blueCurveView;
    private TextView valueCurveText;
    private TextView redCurveText;
    private TextView greenCurveText;
    private TextView blueCurveText;
    private ImageView imageView;
    private ImageView imageView1;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
//    private Integer CREATE_HISTOGRAM = 2;
    private BarChart barChartRed,barChartGreen,barChartBlue,barChartGray;
    private Bitmap bitmap;
    private Bitmap histBitmap;
    private boolean curves = false;
    private int pixel;
    private int[] redValue = new int[256];
    private int[] blueValue = new int[256];
    private int[] greenValue = new int[256];
    private int[] grayValue = new int[256];
    private int[] yValue = new int[256];
    private int[] cYValue = new int[256];
    private int[] TValue = new int[256];
    private Point[] pointValueCurve = new Point[256];
    private Point[] pointRedCurve = new Point[256];
    private Point[] pointGreenCurve = new Point[256];
    private Point[] pointBlueCurve = new Point[256];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChartRed = (BarChart) findViewById(R.id.barChartRed);
        barChartGreen = (BarChart) findViewById(R.id.barChartGreen);
        barChartBlue = (BarChart) findViewById(R.id.barChartBlue);
        barChartGray = (BarChart) findViewById(R.id.barChartGray);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        valueCurveView = (CurveView) findViewById(R.id.valueCurveCorrection);
        redCurveView = (CurveView) findViewById(R.id.redCurveCorrection);
        greenCurveView = (CurveView) findViewById(R.id.greenCurveCorrection);
        blueCurveView = (CurveView) findViewById(R.id.blueCurveCorrection);
        valueCurveText = (TextView) findViewById(R.id.valueCurveText);
        redCurveText = (TextView) findViewById(R.id.redCurveText);
        greenCurveText = (TextView) findViewById(R.id.greenCurveText);
        blueCurveText = (TextView) findViewById(R.id.blueCurveText);

        Button menu = (Button) findViewById(R.id.menu);
        Button feature = (Button) findViewById(R.id.feature);

        for (int i = 0; i < 256; i++) {
            redValue[i] = 0;
            blueValue[i] = 0;
            greenValue[i] = 0;
            grayValue[i] = 0;
            yValue[i] = 0;
            cYValue[i] = 0;
        }

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        feature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectFeature();
            }
        });

        valueCurveView.setGraphColor(Color.DKGRAY);
        redCurveView.setGraphColor(Color.RED);
        greenCurveView.setGraphColor(Color.GREEN);
        blueCurveView.setGraphColor(Color.BLUE);
    }

    @Override
    protected  void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    private void setHistogram(BarChart barChart, String color, int[] colorList) {
            List<BarEntry> entries = new ArrayList<>();

            for(int i = 0; i < 256; i++) {
                entries.add(new BarEntry(i, colorList[i]));
            }
            BarDataSet set = new BarDataSet(entries, color);

            if (color == "Red") {
                set.setColor(Color.rgb(255,0,0));
            } else if (color == "Green") {
                set.setColor(Color.rgb(0,255,0));
            } else if (color == "Blue") {
                set.setColor(Color.rgb(0,0,255));
            } else {
                set.setColor(Color.rgb(0,0,0));
            }

            BarData barData = new BarData(set);
            barChart.getDescription().setEnabled(false); // remove bar description
            barChart.setData(barData);
            barChart.setFitBars(true); // make the x-axis fit exactly all bars
            barChart.invalidate(); // refresh
            XAxis xAxis = barChart.getXAxis(); // make label to bottom
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

    }


    private void setColor() {
        barChartRed.setVisibility(View.VISIBLE);
        barChartGreen.setVisibility(View.VISIBLE);
        barChartBlue.setVisibility(View.VISIBLE);
        barChartGray.setVisibility(View.VISIBLE);
        try {
            int width = histBitmap.getWidth();
            int height = histBitmap.getHeight();
            Integer grayColor;
            for (int i = 0; i < 256; i++) {
                redValue[i] = 0;
                blueValue[i] = 0;
                greenValue[i] = 0;
                grayValue[i] = 0;
                yValue[i] = 0;
            }
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    pixel = histBitmap.getPixel(i,j);
                    redValue[Color.red(pixel)]++ ;
                    blueValue[Color.blue(pixel)]++;
                    greenValue[Color.green(pixel)]++;
                    grayColor = Color.red(pixel) + Color.green(pixel) + Color.blue(pixel);
                    grayColor /= 3;
                    yValue[(int) (( 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) * 219 / 255) + 16]++;
                    grayValue[grayColor]++;
                }
            }
            setHistogram(barChartRed, "Red",redValue);
            setHistogram(barChartGreen, "Green",greenValue);
            setHistogram(barChartBlue, "Blue",blueValue);
            setHistogram(barChartGray, "Gray", grayValue);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to create Histogram", Toast.LENGTH_LONG).show();
        }
    }

    private void histogramStretchImage() {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int minRed, minGreen, minBlue, maxRed, maxGreen, maxBlue;
        minRed = minGreen = minBlue = maxRed = maxGreen = maxBlue = -1;
        int iter = 0;
        // Find minimal value in RGB histograms
        while ((minRed == -1 || minGreen == -1 || minBlue == -1) && iter < 256) {
            if (minRed == -1) {
                if (redValue[iter] > 0) minRed = iter;
            }
            if (minGreen == -1) {
                if (greenValue[iter] > 0) minGreen = iter;
            }
            if (minBlue == -1) {
                if (blueValue[iter] > 0) minBlue = iter;
            }
            iter++;
        }
        // Find maximal value in RGB histograms
        iter = 255;
        while ((maxRed == -1 || maxGreen == -1 || maxBlue == -1) && iter >= 0) {
            if (maxRed == -1) {
                if (redValue[iter] > 0) maxRed = iter;
            }
            if (maxGreen == -1) {
                if (greenValue[iter] > 0) maxGreen = iter;
            }
            if (maxBlue == -1) {
                if (blueValue[iter] > 0) maxBlue = iter;
            }
            iter--;
        }
        int redStretchFactor = 255 / (maxRed - minRed);
        int greenStretchFactor = 255 / (maxGreen - minGreen);
        int blueStretchFactor = 255 / (maxBlue - minBlue);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                pixel = bitmap.getPixel(i, j);
                int red, green, blue, alpha, pix;
                pix = 0;
                
                alpha = Color.alpha(pixel);
                red = ((Color.red(pixel)-minRed) * redStretchFactor);
                green = ((Color.green(pixel)-minGreen) * greenStretchFactor);
                blue = ((Color.blue(pixel)-minBlue) * blueStretchFactor);
                if(red > 255) red = 255;
                if(green > 255) green = 255;
                if(blue > 255) blue = 255;
                if(red < 0) red = 0;
                if(green < 0) green = 0;
                if(blue < 0) blue = 0;
                pix = pix | blue;
                pix = pix | (green << 8);
                pix = pix | (red << 16);
                pix = pix | (alpha << 24);
                newBitmap.setPixel(i, j, pix);
            }
        }
        imageView1.setImageBitmap(newBitmap);
        histBitmap = newBitmap;
    }

    private void equalizateImage() {
        //todo : Equalizer
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cYValue[0] = yValue[0];
        for (int i = 1; i < 256; i++) {
            cYValue[i] = cYValue[i-1] + yValue[i];
            TValue[i] = cYValue[i]*255/(width*height);
        }
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = bitmap.getPixel(i, j);
                int red, green, blue, alpha, pix;
                int y,u,v;
                pix = 0;
                y = TValue[(int) (( 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) * 219 / 255) + 16];
                u = (int) ((-0.299 * Color.red(pixel) - 0.587 * Color.green(pixel) + 0.886 * Color.blue(pixel)) * 224 / 1.772 / 255) + 128;
                v = (int) (( 0.701 * Color.red(pixel) - 0.587 * Color.green(pixel) - 0.114 * Color.blue(pixel)) * 224 / 1.402 / 255) + 128;

                alpha = (int) Color.alpha(pixel);
                red =  (int)((y - 16) + (1.370705 * (v - 128)));
                blue = (int)((y - 16) + (1.732446 * (u - 128)));
                green = (int)((y - 16) - (0.698001 * (v - 128)) - (0.337633 * (u - 128)));
                if(red > 255) red = 255;
                if(green > 255) green = 255;
                if(blue > 255) blue = 255;
                if(red < 0) red = 0;
                if(green < 0) green = 0;
                if(blue < 0) blue = 0;
                pix = pix | blue;
                pix = pix | (green << 8);
                pix = pix | (red << 16);
                pix = pix | (alpha << 24);
                newBitmap.setPixel(i, j, pix);
            }
        }
        imageView1.setImageBitmap(newBitmap);
        histBitmap = newBitmap;
        // update the histogram
        //setColor();
    }

    private int getColorOffset(int color, Point value, Point rgb)
    {
        int valueOffset = (int)(255f - ((value.getY()-50)/2)) - color;
        int rgbOffset = (int)(255f - ((rgb.getY()-50)/2)) - color;
        return (valueOffset+rgbOffset);
    }

    private void applyImageCurve() {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        try {
            pointValueCurve = valueCurveView.getPoints();
            pointRedCurve = redCurveView.getPoints();
            pointGreenCurve = greenCurveView.getPoints();
            pointBlueCurve = blueCurveView.getPoints();

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixel = bitmap.getPixel(i, j);
                    int red, green, blue, alpha, pix;
                    pix = 0;

                    alpha = Color.alpha(pixel);
//                    red = 255 - (int) (pointValueCurve[Color.red(pixel)].y + pointRedCurve[Color.red(pixel)].y - 100) / 4;
//                    green = 255 - (int) (pointValueCurve[Color.green(pixel)].y + pointGreenCurve[Color.green(pixel)].y - 100) / 4;
//                    blue = 255 - (int) (pointValueCurve[Color.blue(pixel)].y + pointBlueCurve[Color.blue(pixel)].y - 100) / 4;
                    red = Color.red(pixel) + getColorOffset(Color.red(pixel), pointValueCurve[Color.red(pixel)], pointRedCurve[Color.red(pixel)]);
                    green = Color.green(pixel) + getColorOffset(Color.green(pixel), pointValueCurve[Color.green(pixel)], pointGreenCurve[Color.green(pixel)]);
                    blue = Color.blue(pixel) + getColorOffset(Color.blue(pixel), pointValueCurve[Color.blue(pixel)], pointBlueCurve[Color.blue(pixel)]);
                    if (red > 255) red = 255;
                    if (green > 255) green = 255;
                    if (blue > 255) blue = 255;
                    if (red < 0) red = 0;
                    if (green < 0) green = 0;
                    if (blue < 0) blue = 0;
                    pix = pix | blue;
                    pix = pix | (green << 8);
                    pix = pix | (red << 16);
                    pix = pix | (alpha << 24);
                    newBitmap.setPixel(i, j, pix);
                }
            }
            imageView1.setImageBitmap(newBitmap);
            histBitmap = newBitmap;
        }
        catch (Error e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to apply image Curve", Toast.LENGTH_LONG).show();
        }
    }

    private void correctImage(){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pointValueCurve = valueCurveView.getPoints();
        for (int i = 0; i < 256; i++) {
            TValue[i] = (int) (255f-((pointValueCurve[i].y-50)/2));
            Log.d("Correction", Integer.toString(i) + ": " + Integer.toString(TValue[i]));
        }
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = bitmap.getPixel(i, j);
                int red, green, blue, alpha, pix;
                int y,u,v;
                pix = 0;
                y = TValue[(int) (( 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) * 219 / 255) + 16];
                u = (int) ((-0.299 * Color.red(pixel) - 0.587 * Color.green(pixel) + 0.886 * Color.blue(pixel)) * 224 / 1.772 / 255) + 128;
                v = (int) (( 0.701 * Color.red(pixel) - 0.587 * Color.green(pixel) - 0.114 * Color.blue(pixel)) * 224 / 1.402 / 255) + 128;

                alpha = (int) Color.alpha(pixel);
                red =  (int)((y - 16) + (1.370705 * (v - 128)));
                blue = (int)((y - 16) + (1.732446 * (u - 128)));
                green = (int)((y - 16) - (0.698001 * (v - 128)) - (0.337633 * (u - 128)));
                if(red > 255) red = 255;
                if(green > 255) green = 255;
                if(blue > 255) blue = 255;
                if(red < 0) red = 0;
                if(green < 0) green = 0;
                if(blue < 0) blue = 0;
                pix = pix | blue;
                pix = pix | (green << 8);
                pix = pix | (red << 16);
                pix = pix | (alpha << 24);
                newBitmap.setPixel(i, j, pix);
            }
        }
        imageView1.setImageBitmap(newBitmap);
        histBitmap = newBitmap;
    }

    private void showHideCurves() {
        if(!curves) {
            valueCurveView.setVisibility(View.VISIBLE);
            redCurveView.setVisibility(View.VISIBLE);
            greenCurveView.setVisibility(View.VISIBLE);
            blueCurveView.setVisibility(View.VISIBLE);
            valueCurveText.setVisibility(View.VISIBLE);
            redCurveText.setVisibility(View.VISIBLE);
            greenCurveText.setVisibility(View.VISIBLE);
            blueCurveText.setVisibility(View.VISIBLE);
            curves = true;
        } else {
            valueCurveView.setVisibility(View.GONE);
            redCurveView.setVisibility(View.GONE);
            greenCurveView.setVisibility(View.GONE);
            blueCurveView.setVisibility(View.GONE);
            valueCurveText.setVisibility(View.GONE);
            redCurveText.setVisibility(View.GONE);
            greenCurveText.setVisibility(View.GONE);
            blueCurveText.setVisibility(View.GONE);
            curves = false;
        }
    }

    private void SelectFeature() {
        final CharSequence[] items ={"Histogram","Equalizer","Histogram Stretching","Show/Hide Curves","Apply Curves","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Feature");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Histogram")) {
                    setColor();
                } else if (items[which].equals("Equalizer")) {
                    histBitmap = bitmap;
                    setColor();
                    equalizateImage();
                    dialog.dismiss();
                } else if (items[which].equals("Histogram Stretching")) {
                    histBitmap = bitmap;
                    setColor();
                    histogramStretchImage();
                    dialog.dismiss();
                } else if (items[which].equals("Show/Hide Curves")){
                    showHideCurves();
                } else if (items[which].equals("Apply Curves")){
                    //correctImage();
                    applyImageCurve();
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SelectImage() {
        final CharSequence[] items ={"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                    imageView.setVisibility(View.VISIBLE);
                    imageView1.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Gallery")) {
                    Intent photopickerIntent = new Intent(Intent.ACTION_PICK);
                    File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = pictureDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);
                    photopickerIntent.setDataAndType(data, "image/*");
                    startActivityForResult(photopickerIntent,SELECT_FILE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView1.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                histBitmap = bitmap;
                imageView.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        histBitmap = bitmap;
                        imageView.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                    }
                    imageView.setImageURI(selectedImageUri);
            }
        }
    }
}
