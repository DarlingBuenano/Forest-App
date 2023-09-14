package com.software.forestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class ConteoEspecies extends AppCompatActivity {

    private ImageView img_foto_procesada;
    private TextView txt_detecciones_canelo;
    private TextView txt_detecciones_desconocido;
    private Bitmap imagenBitmap;
    private Uri imgUriResult;
    private NumberFormat format;
    private RequestQueue volleyRequest;
    private static final int TAMAÑO_ENTRADA = 640;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo_especies);

        img_foto_procesada = findViewById(R.id.img_foto_procesada);
        txt_detecciones_canelo = findViewById(R.id.txt_item_detecciones_canelo);
        txt_detecciones_desconocido = findViewById(R.id.txt_item_detecciones_desconocido);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Locale espanol = new Locale("es");
        format = NumberFormat.getPercentInstance(espanol);
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(2);
        Log.d("ConteoEspecies", "onStart");

        imgUriResult = (Uri) getIntent().getExtras().get("imgUriResult");
        InputStream imgStream = null;
        try {
            imgStream = getContentResolver().openInputStream(imgUriResult);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        imagenBitmap = BitmapFactory.decodeStream(imgStream);

        if (imagenBitmap.getWidth() > TAMAÑO_ENTRADA) {
            Log.d("ConteoEspecies", "Imagen excede el tamaño de entrada, por lo que será redimencionada a " + TAMAÑO_ENTRADA);
            Matrix matrix = new Matrix();
            matrix.postRotate(0);

            // Redimencionar imagen a tamaño de entrada
            int originalWigth = imagenBitmap.getWidth();
            int originalHeight = imagenBitmap.getHeight();
            int newWigth = TAMAÑO_ENTRADA;
            int newHeight = Math.round((newWigth * originalHeight) / originalWigth);
            imagenBitmap = Bitmap.createBitmap(imagenBitmap,0,0,newWigth,newHeight,matrix,false);
        }
        else {
            //TODO
        }
        img_foto_procesada.setImageBitmap(imagenBitmap);
        this.volleyRequest = Volley.newRequestQueue(this);
        //obtenerDatosDelModelo();
        ejecutarObjectDetection();
    }

    private void obtenerDatosDelModelo() {
        String url = "http://192.168.56.1:8000/api/nombre-modelo/";
        int method = Request.Method.GET;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                method,
                url,
                null,
                response -> System.out.println(response),
                error -> System.out.println(error));
        volleyRequest.add(jsonObjectRequest);
    }

    private void ejecutarObjectDetection() {
        String url = "http://192.168.56.1:8000/api/procesar-foto/";
        int method = Request.Method.POST;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                method,
                url,
                null,
                response -> {
                    int contador_canelo = 0;
                    int contador_descon = 0;
                    int color = Color.GREEN; //por defecto
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            //confidence, class, name
                            if (jsonObject.getInt("class") == 0) {
                                contador_canelo++;
                                color = Color.YELLOW;
                            }
                            else if (jsonObject.getInt("class") == 1) {
                                contador_descon++;
                                color = Color.BLACK;
                            }
                            dibujarRect(
                                    (float)jsonObject.getDouble("xmin"),
                                    (float)jsonObject.getDouble("ymin"),
                                    (float)jsonObject.getDouble("xmax"),
                                    (float)jsonObject.getDouble("ymax"),
                                    color
                            );
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    img_foto_procesada.setImageBitmap(imagenBitmap);
                    txt_detecciones_canelo.setText("Especie: Canelo\nCantidad: " + contador_canelo);
                    txt_detecciones_desconocido.setText("Especie: Desconocido\nCantidad: " + contador_descon);
                },
                error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public byte[] getBody() {
                ByteArrayOutputStream array = new ByteArrayOutputStream();
                imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 75, array);
                byte[] imagenByte = array.toByteArray();
                return imagenByte;
            }
        };
        //Esto permite alargar la espera y que solo haga 1 sola petición al servidor
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(35000,0,0));
        volleyRequest.add(jsonArrayRequest);
    }

    private void dibujarRect(float left, float top, float right, float bottom, int color) {
        // Crea un Canvas asociado al Bitmap
        Canvas canvas = new Canvas(imagenBitmap);

        // Configura el objeto Paint para dibujar un rectángulo relleno
        Paint paint = new Paint();
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        // Dibuja el rectángulo
        canvas.drawRect(left, top, right, bottom, paint);
    }
}