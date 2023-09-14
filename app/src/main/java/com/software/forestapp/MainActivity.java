package com.software.forestapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    /*private static final int REQUEST_PERMISSION_CAMERA = 100;
    private static final int REQUEST_SELECT_FILE = 1;*/
    private Button btn_subir_foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_subir_foto = findViewById(R.id.btn_subir_foto);
        btn_subir_foto.setOnClickListener(onClicSubirFoto);
    }

    private View.OnClickListener onClicSubirFoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGetImageFromGallery.launch("image/*");
        }
    };

    ActivityResultLauncher<String> mGetImageFromGallery = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        Log.d("MainActivity", "mGetImageFromGallery");
        Intent intent = null;
        intent = new Intent(this, ConteoEspecies.class);
        intent.putExtra("imgUriResult", result);
        startActivity(intent);
    });
}