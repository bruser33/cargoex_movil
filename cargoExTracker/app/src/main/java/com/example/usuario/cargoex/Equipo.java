package com.example.usuario.cargoex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cargo.usuario.cargoex.R;
import com.example.usuario.cargoex.util.AsyncResponse;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.ModalVersion;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.Result;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Equipo extends AppCompatActivity   {
    private FusedLocationProviderClient client;
    String latitud,longitud,vista="";
    Intent modal,modalVersion;
    SharedPreferences prefs ;
    String codigo,versionWeb,versionMovil;
    SqliteCertificaciones conn;
    TextView numero,nombre,fecha ;
    LinearLayout ods;
    private ZXingScannerView scanner;
    int contadorOds ;
    ArrayList <String> listaOd;
    int indiceABorrar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equipo);
        numero=(TextView) findViewById(R.id.od);

        //pongo nombre
        nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        codigo=prefs.getString("codigo", "");
        nombre.setText(name);
        //pongo fecha actual en la vista
        fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);

        //requestPermission();

    }



}


