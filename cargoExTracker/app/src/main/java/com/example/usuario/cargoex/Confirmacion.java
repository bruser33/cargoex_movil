package com.example.usuario.cargoex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.cargo.usuario.cargoex.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Confirmacion extends AppCompatActivity {
    String numero;
    SharedPreferences prefs ;
    ArrayList <String> listaOd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmacion);
        //pongo nombre
        TextView nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        nombre.setText(name);
        //pongo fecha actual en la vista
        TextView fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
        //pongood
        TextView od = (TextView)findViewById(R.id.od);
        numero = getIntent().getStringExtra("od");
        listaOd=getIntent().getStringArrayListExtra("lista");
    //    Log.e("lista","tamaño es "+listaOd.size() + listaOd.get(0)+" - "+listaOd.get(1));
        if(listaOd.size()==1){
            numero = listaOd.get(0);
            od.setText("OD: "+numero);
        }else{
            od.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 ) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String returnString = data.getStringExtra("procesado");
                if(returnString.equals("true")){
                    File files = new File(Environment.getExternalStorageDirectory().getPath()+"/Cargoex");
                    try {
                        FileUtils.deleteDirectory(files);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent output = new Intent();
                    output.putExtra("procesado", "true");
                    setResult(RESULT_OK, output);
                    this.finish();
                }
                // set text view with string
            }
        } else if (requestCode == 666 ) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String returnString = data.getStringExtra("procesado");
                if(returnString.equals("true")){
                    File files = new File(Environment.getExternalStorageDirectory().getPath()+"/Cargoex");
                    try {
                        FileUtils.deleteDirectory(files);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent output = new Intent();
                    output.putExtra("procesado", "true");
                    setResult(RESULT_OK, output);
                    this.finish();
                }
                // set text view with string
            }
        }
    }

    public void no(View v){
        Intent intent = new Intent(this, Devoluciones.class);
        intent.putExtra("od",numero);
        intent.putExtra("vista","retiro");
        intent.putExtra("lista",listaOd);
        startActivityForResult(intent,777);
    }
    public void si(View v){
        Intent intent = new Intent(this, ConfimacionRetiro.class);
        intent.putExtra("od",numero);
        intent.putExtra("lista",listaOd);
        startActivityForResult(intent,666);
    }

    }
