package com.example.luigi.mnistclassifiertest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    static MultiLayerNetwork net;
    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasView = (CanvasView) findViewById(R.id.canvasView);

        try {
            File netFile = new File(getCacheDir(), "tmpfile.zip");
            InputStream in = getResources().openRawResource(R.raw.net);
            copyInputStreamToFile(in, netFile);
            net = ModelSerializer.restoreMultiLayerNetwork(netFile, true);
            Toast.makeText(this, "Net model loaded successfully.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Button b = (Button) findViewById(R.id.button2);
        b.setOnClickListener(view -> predict());
    }

    public void clearCanvas(View view) {
        canvasView.clearCanvas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.netItem) {
            if (net != null) {
                startActivity(new Intent(this, NetConfActivity.class));
            } else {
                Toast.makeText(this, "Net not loaded.", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void predict() {
        Bitmap bitmap = canvasView.getDrawingCache();
        bitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true);
        bitmap.setHasAlpha(false);
        INDArray image = Nd4j.zeros(28 * 28);
        for (int i = 0; i < bitmap.getHeight() - 1; i++) {
            for (int j = 0; j < bitmap.getWidth() - 1; j++) {
                int pixel = bitmap.getPixel(i, j);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                double gray = (red + green + blue) / (double) 3;
                image.putScalar(j*28+i,(gray/255));
            }
        }
        for (int i = 0; i < image.columns() - 1; i++) {
            if (i % 28 == 0) {
                System.out.print("\n");
            }
            System.out.print(image.getDouble(i) > 0 ? 1 : 0 + " ");
        }
        INDArray result = net.output(image);
        int num = 0;
        double prob = 0;
        for (int i = 0; i < result.columns() - 1; i++) {
            double tmp = result.getDouble(i);
            if (tmp > prob) {
                prob = tmp;
                num = i;
            }
        }
        System.out.println(num + ", " + (prob * 100) + "%");
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText(num + ", " + (prob * 100) + "%");
        canvasView.destroyDrawingCache();
    }
}