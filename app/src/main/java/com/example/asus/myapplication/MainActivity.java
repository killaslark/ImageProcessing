package com.example.asus.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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


    private ImageView imageView;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
//    private Integer CREATE_HISTOGRAM = 2;
    private BarChart barChartRed,barChartGreen,barChartBlue,barChartGray;
    private Bitmap bitmap;
    private int pixel;
    private int[] redValue = new int[256];
    private int[] blueValue = new int[256];
    private int[] greenValue = new int[256];
    private int[] grayValue = new int[256];
    private int[] yValue = new int[256];
    private int[] uValue = new int[256];
    private int[] vValue = new int[256];
    private int[] cGrayValue = new int[256];
    private int[] TValue = new int[256];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barChartRed = (BarChart) findViewById(R.id.barChartRed);
        barChartGreen = (BarChart) findViewById(R.id.barChartGreen);
        barChartBlue = (BarChart) findViewById(R.id.barChartBlue);
        barChartGray = (BarChart) findViewById(R.id.barChartGray);
        imageView = (ImageView) findViewById(R.id.imageView);

        Button menu = (Button) findViewById(R.id.menu);
        Button feature = (Button) findViewById(R.id.feature);

        for (int i = 0; i < 256; i++) {
            redValue[i] = 0;
            blueValue[i] = 0;
            greenValue[i] = 0;
            grayValue[i] = 0;
            cGrayValue[i] = 0;
            yValue[i] = 0;
            uValue[i] = 0;
            vValue[i] = 0;
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
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Integer grayColor;
            for (int i = 0; i < 256; i++) {
                redValue[i] = 0;
                blueValue[i] = 0;
                greenValue[i] = 0;
                grayValue[i] = 0;
            }
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    pixel = bitmap.getPixel(i,j);
                    redValue[Color.red(pixel)]++ ;
                    blueValue[Color.blue(pixel)]++;
                    greenValue[Color.green(pixel)]++;
                    grayColor = Color.red(pixel) + Color.green(pixel) + Color.blue(pixel);
                    grayColor /= 3;
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

    private void equalizateImage() {
        //todo : Equalizer
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cGrayValue[0] = grayValue[0];
        for (int i = 1; i < 256; i++) {
            cGrayValue[i] = cGrayValue[i-1] + grayValue[i];
            TValue[i] = cGrayValue[i]*255/(width*height);
        }
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = bitmap.getPixel(i, j);
                int red, green, blue, alpha, pix;
                pix = 0;
                alpha = (int) TValue[Color.alpha(pixel)];
                red = (int) TValue[Color.red(pixel)];
                blue = (int) TValue[Color.blue(pixel)];
                green = (int) TValue[Color.green(pixel)];
                pix = pix | blue;
                pix = pix | (green << 8);
                pix = pix | (red << 16);
                pix = pix | (alpha << 24);
                newBitmap.setPixel(i, j, pix);
            }
        }
        imageView.setImageBitmap(newBitmap);
        // update the histogram
        //setColor();
    }

    private void SelectFeature() {
        final CharSequence[] items ={"Histogram","Equalizer","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Feature");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Histogram")) {
                    setColor();
                } else if (items[which].equals("Equalizer")) {
                    equalizateImage();
                    dialog.dismiss();
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
                } else if (items[which].equals("Gallery")) {
                    Intent photopickerIntent = new Intent(Intent.ACTION_PICK);
                    File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String pictureDirectoryPath = pictureDirectory.getPath();
                    Uri data = Uri.parse(pictureDirectoryPath);
                    photopickerIntent.setDataAndType(data, "image/*");
                    startActivityForResult(photopickerIntent,SELECT_FILE);
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
                imageView.setImageBitmap(bitmap);

            } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
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
