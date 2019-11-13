package com.example.usuario.cargoex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.cargo.usuario.cargoex.R;

import com.example.usuario.cargoex.util.Modal;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConfimacionRetiro extends AppCompatActivity {
//    String numero;
    SharedPreferences prefs ;
    Intent modal;
    ArrayList<String> lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmacionretiro);
        modal= new Intent(this, Modal.class);
        //pongo nombre
        TextView nombre= (TextView) findViewById(R.id.nombre);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        String name = prefs.getString("nombre", "");
        nombre.setText(name);
        //pongo fecha actual en la vista
        TextView fecha = (TextView) findViewById(R.id.fecha);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
        Date date = new Date();
        String fechaHoy = dateFormat.format(date);
        fecha.setText(fechaHoy);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("procesado", "false");
        editor.putString("path1", "false");
        editor.putString("path2","false");
        editor.putString("path3","false");
        editor.commit();

        lista=getIntent().getStringArrayListExtra("lista");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 ) {
            if (resultCode == RESULT_OK) {
                //delete folder
                File files = new File(Environment.getExternalStorageDirectory().getPath()+"/Cargoex");
                try {
                    FileUtils.deleteDirectory(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // get String data from Intent
                String returnString = data.getStringExtra("procesado");
                if(returnString.equals("true")){
                    Intent output = new Intent();
                    output.putExtra("procesado", "true");
                    setResult(RESULT_OK, output);
                    this.finish();
                }
                // set text view with string
            }
        }else if (requestCode == 666 ) {
            if (resultCode == RESULT_OK) {
                //delete folder
                File files = new File(Environment.getExternalStorageDirectory().getPath()+"/Cargoex");
                try {
                    FileUtils.deleteDirectory(files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // get String data from Intent
                String returnString = data.getStringExtra("procesado");
                String signal= data.getStringExtra("signal");
                String rutIngresado= data.getStringExtra("rutIngresado");
                String dvIngresado= data.getStringExtra("dvIngresado");
                String emailIngresado= data.getStringExtra("emailIngresado");
                String telefonoIngresado= data.getStringExtra("telefonoIngresado");
                if (signal.equals("false")){
                    Intent intent = new Intent(this, DatosRetiroNormal.class);
                    intent.putExtra("lista",lista);
                    intent.putExtra("rutIngresado",rutIngresado);
                    intent.putExtra("dvIngresado",dvIngresado);
                    intent.putExtra("emailIngresado",emailIngresado);
                    intent.putExtra("telefonoIngresado",telefonoIngresado);
                    startActivityForResult(intent,777);
                }else if(returnString.equals("true")){
                    Intent output = new Intent();
                    output.putExtra("procesado", "true");
                    setResult(RESULT_OK, output);
                    this.finish();
                }
            }
        }
    }

    public void normal(View v){
        Intent intent = new Intent(this, DatosRetiroNormal.class);
      //  intent.putExtra("od",numero);
        intent.putExtra("lista",lista);

        startActivityForResult(intent,777);
    }
    public void biometria(View v){
        if (!isNetDisponible() ){
            modal.putExtra("error", "Usted no tiene internet, su intento sera contado por favor ingrese del modo sin huella");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("errorh", 902);
            editor.commit();
            startActivity(modal);

        }else{
            Intent intent = new Intent(this, DatosRetiroHuella.class);
         //   intent.putExtra("od",numero);
            intent.putExtra("lista",lista);
            startActivityForResult(intent,666);
        }
    }
    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    public Boolean isOnlineNet() {

        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
