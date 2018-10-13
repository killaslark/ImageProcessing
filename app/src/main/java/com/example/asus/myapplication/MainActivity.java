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
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private CurveView valueCurveView,redCurveView, greenCurveView, blueCurveView;
    private TextView valueCurveText, redCurveText, greenCurveText, blueCurveText, textNumber;

    private ImageView imageViewBefore,imageViewAfter;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    private BarChart barChartRed,barChartGreen,barChartBlue,barChartGray;
    private Bitmap bitmap;
    private Bitmap secondBitmap;

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
    private int prediction = -1;

    private int[] chainFrequency = new int[8];
    private int[] operand = new int[2];
    private int operator;
    private int operandPointer;
    private final double[][] numberFrequency = new double[][]{
            //Angka 0
            {0.097560976,0.073170732,0.256097561,0.073170732,0.097560976,0.079268293,0.243902439,0.079268293},
            //Angka 1
            {0.053254438,          0,0.384615385,0.047337278,0.082840237, 0.00591716,0.343195266,0.082840237},
            //Angka 2
            {0.190298507,0.044776119,0.104477612,0.141791045,0.22761194,0.029850746,0.097014925,0.164179104},
            //Angka 3
            {0.161172161,0.098901099,0.135531136,0.102564103,0.164835165,0.098901099,0.131868132,0.106227106},
            //Angka 4
            {0.070652174,0.005434783,0.342391304,0.005434783,0.222826087,0.005434783,0.190217391,0.157608696},
            //Angka 5
            {0.215231788,0.069536424,0.145695364,0.079470199,0.195364238,0.072847682,0.158940397,0.062913907},
            //Angka 6
            {0.137339056,0.094420601,0.17167382,0.098712446,0.13304721,0.090128755,0.184549356,0.090128755},
            //Angka 7
            {0.198067633,0,0.188405797,0.120772947,0.183574879,0.004830918,0.193236715,0.111111111},
            //Angka 8
            {0.129943503,0.107344633,0.15819209,0.107344633,0.124293785,0.11299435,0.152542373,0.107344633},
            //Angka 9
            {0.137339056,0.090128755,0.180257511,0.090128755,0.141630901,0.090128755,0.175965665,0.094420601},
            //Simbol +
            {0.246323529,0.003676471,0.246323529,0.003676471,0.246323529,0.003676471,0.246323529,0.003676471},
            //Simbol -
            {0.387096774,0,0.112903226,0,0.387096774,0,0.112903226,0}
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChartRed = (BarChart) findViewById(R.id.barChartRed);
        barChartGreen = (BarChart) findViewById(R.id.barChartGreen);
        barChartBlue = (BarChart) findViewById(R.id.barChartBlue);
        barChartGray = (BarChart) findViewById(R.id.barChartGray);
        imageViewBefore = (ImageView) findViewById(R.id.imageViewBefore);
        imageViewAfter = (ImageView) findViewById(R.id.imageViewAfter);
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
        Button secondFeature = (Button) findViewById(R.id.feature2);
        textNumber = (TextView) findViewById(R.id.textNumber);

        initiateVariables();

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

        secondFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectSecondFeature();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                secondBitmap = bitmap;
                imageViewBefore.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        secondBitmap = bitmap;
                        imageViewBefore.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                    }
                    imageViewBefore.setImageURI(selectedImageUri);
            }
        }
    }

    private void initiateVariables() {
        valueCurveView.setGraphColor(Color.DKGRAY);
        redCurveView.setGraphColor(Color.RED);
        greenCurveView.setGraphColor(Color.GREEN);
        blueCurveView.setGraphColor(Color.BLUE);
        operand[0] = -1;
        operand[1] = -1;
        operator = -1;
        operandPointer = 0;

        for (int i = 0; i < 256; i++) {
            redValue[i] = 0;
            blueValue[i] = 0;
            greenValue[i] = 0;
            grayValue[i] = 0;
            yValue[i] = 0;
            cYValue[i] = 0;
        }

        for (int i = 0; i < 8 ; i++) {
            chainFrequency[i] = 0;
        }

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
            int width = secondBitmap.getWidth();
            int height = secondBitmap.getHeight();
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
                    pixel = secondBitmap.getPixel(i,j);
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
        try {
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

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixel = bitmap.getPixel(i, j);
                    int red, green, blue, alpha, pix;
                    pix = 0;

                    alpha = Color.alpha(pixel);
                    red = ((Color.red(pixel) - minRed) * redStretchFactor);
                    green = ((Color.green(pixel) - minGreen) * greenStretchFactor);
                    blue = ((Color.blue(pixel) - minBlue) * blueStretchFactor);
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
            imageViewAfter.setImageBitmap(newBitmap);
            secondBitmap = newBitmap;
        }
        catch (Error e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to Stretch the Histogram", Toast.LENGTH_LONG).show();
        }
    }

    private void equalizateImage() {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cYValue[0] = yValue[0];
            for (int i = 1; i < 256; i++) {
                cYValue[i] = cYValue[i - 1] + yValue[i];
                TValue[i] = cYValue[i] * 255 / (width * height);
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixel = bitmap.getPixel(i, j);
                    int red, green, blue, alpha, pix;
                    int y, u, v;
                    pix = 0;
                    y = TValue[(int) ((0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)) * 219 / 255) + 16];
                    u = (int) ((-0.299 * Color.red(pixel) - 0.587 * Color.green(pixel) + 0.886 * Color.blue(pixel)) * 224 / 1.772 / 255) + 128;
                    v = (int) ((0.701 * Color.red(pixel) - 0.587 * Color.green(pixel) - 0.114 * Color.blue(pixel)) * 224 / 1.402 / 255) + 128;

                    alpha = (int) Color.alpha(pixel);
                    red = (int) ((y - 16) + (1.370705 * (v - 128)));
                    blue = (int) ((y - 16) + (1.732446 * (u - 128)));
                    green = (int) ((y - 16) - (0.698001 * (v - 128)) - (0.337633 * (u - 128)));
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
            imageViewAfter.setImageBitmap(newBitmap);
            secondBitmap = newBitmap;
        }
        catch (Error e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to equalize the Image", Toast.LENGTH_LONG).show();
        }
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
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
            imageViewAfter.setImageBitmap(newBitmap);
            secondBitmap = newBitmap;
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
        imageViewAfter.setImageBitmap(newBitmap);
        secondBitmap = newBitmap;
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
                    secondBitmap = bitmap;
                    setColor();
                    equalizateImage();
                    dialog.dismiss();
                } else if (items[which].equals("Histogram Stretching")) {
                    secondBitmap = bitmap;
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

    private void SelectSecondFeature() {
        final CharSequence[] items ={"Thinning","Predict","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Second Feature");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Thinning")) {
                    if(bitmap != null)
                    {
                        Skeletonization skeletonization = new Skeletonization(bitmap);
                        secondBitmap = skeletonization.getBitmap();
                        imageViewAfter.setImageBitmap(secondBitmap);
                        prediction = skeletonization.prediction;
                    }
                } else if (items[which].equals("Predict")) {
                    predictSingleCharacter();
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
                    imageViewBefore.setVisibility(View.VISIBLE);
                    imageViewAfter.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Gallery")) {
                    Intent photopickerIntent = new Intent(Intent.ACTION_PICK);
                    File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = pictureDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);
                    photopickerIntent.setDataAndType(data, "image/*");
                    startActivityForResult(photopickerIntent,SELECT_FILE);
                    imageViewBefore.setVisibility(View.VISIBLE);
                    imageViewAfter.setVisibility(View.VISIBLE);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void predictSingleCharacter() {
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
                    Log.d("Found",i+","+ j);
                    iStart = i;
                    jStart = j;
                    Log.d("Size ", width+","+height);
                    found = true;
                }
            }
        }

        int iKeliling = iStart, jKeliling = jStart;

        // Mulai keliling
        checkTimur(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);

        double minSum = -1;
        int idx = -1;
        double[] normalizedFreq = normalize10Histogram(chainFrequency);
        for(int i = 0;i<12;i++) {
            double sum = 0;
            for (int j = 0; j < 8; j++) {
                sum += Math.abs(numberFrequency[i][j] - normalizedFreq[j]);
            }
            if (sum < minSum || minSum == -1) {
                idx = i;
                minSum = sum;
            }
        }
        //Set operand and operator
        if(idx < 10){
            operand[operandPointer] = idx;
            operandPointer = operandPointer+1 > 1 ? 0 : operandPointer+1;
        } else {
            operator = idx;
        }

        for (int i = 0; i < 8; i++) {
            Log.d("Arah[" + i + "] :", chainFrequency[i] + " kemunculan");
            chainFrequency[i] = 0;
        }

        //Update Text
        updateTextView(textNumber);
        Log.d("Number Predicted", Integer.toString(idx));
    }

    private boolean isPixelBlack(int pixel) {
        int limit = 40;
        return (Color.red(pixel) < limit && Color.green(pixel) < limit && Color.blue(pixel) < limit);
    }

    private void checkTimur(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getWidth()) {

            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling);
//            Log.d("Timur" , iKeliling +","+ jKeliling);
            if (isPixelBlack(pixel)) {
                chainCode.add(0);
                chainFrequency[0]++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkUtara(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTenggara(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }

    private  void checkTenggara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getHeight() && iKeliling + 1 < bitmap.getWidth()){
//            Log.d("Tenggara" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(1);
                chainFrequency[1]++;
                jKeliling++;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkSelatan(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkSelatan(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling + 1 < bitmap.getHeight()){
//            Log.d("Selatan" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(2);
                chainFrequency[2]++;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBaratDaya(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkBaratDaya(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling - 1 >= 0 && jKeliling + 1 < bitmap.getHeight()){
//            Log.d("BaratDaya" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling + 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(3);
                chainFrequency[3]++;
                iKeliling--;
                jKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }

        }
    }
    private  void checkBarat(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling - 1 >= 0){
//            Log.d("Barat" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling );
            if (isPixelBlack(pixel)) {
                chainCode.add(4);
                chainFrequency[4]++;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkSelatan(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkBaratLaut(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);
            }
        }
    }
    private  void checkBaratLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling - 1 >= 0 && iKeliling -1 >= 0){
//            Log.d("BaratLaut" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling - 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(5);
                chainFrequency[5]++;
                jKeliling--;
                iKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkUtara(iKeliling, jKeliling, bitmap, chainCode, iStart, jStart);
            }
        }
    }
    private  void checkUtara(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (jKeliling - 1 >= 0){
//            Log.d("Utara" , iKeliling +","+ jKeliling);
            int pixel = bitmap.getPixel(iKeliling, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(6);
                chainFrequency[6]++;
                jKeliling--;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTimurLaut(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
            }
        }
    }
    private  void checkTimurLaut(int iKeliling, int jKeliling, Bitmap bitmap, Vector chainCode, int iStart,int jStart) {
        if (iKeliling + 1 < bitmap.getHeight() && jKeliling - 1 >= 0){
//            Log.d("TimurLaut" , iKeliling +","+jKeliling);
            int pixel = bitmap.getPixel(iKeliling + 1, jKeliling - 1);
            if (isPixelBlack(pixel)) {
                chainCode.add(7);
                chainFrequency[7]++;
                jKeliling--;
                iKeliling++;
                if (iKeliling != iStart || jKeliling != jStart) {
                    checkBarat(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
                }
            } else {
                checkTimur(iKeliling,jKeliling,bitmap,chainCode,iStart,jStart);
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

    private void updateTextView(TextView textView){
//        String operand1 = operand[0] == -1 ? "Operand1" : Integer.toString(operand[0]);
//        String operand2 = operand[1] == -1 ? "Operand2" : Integer.toString(operand[1]);
//        String operatorText = "Operator";
//        String count = "";
//
//        if(operator == 10) operatorText = "+";
//        else if(operator == 11) operatorText = "-";
//
//        if(operand[0] != -1 && operand[1] != -1 && operator != -1)
//        {
//            if(operator == 10) count = " = " + Integer.toString(operand[0]+operand[1]);
//            else if(operator == 11) count = " = " + Integer.toString(operand[0]-operand[1]);
//        }
//
//        String predictedText = operand1 + " " + operatorText + " " + operand2 + count;
        textView.setText(Integer.toString(prediction));
    }

}
