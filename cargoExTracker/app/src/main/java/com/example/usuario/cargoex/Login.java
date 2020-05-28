package com.example.usuario.cargoex;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.cargo.usuario.cargoex.R;
import com.example.usuario.cargoex.entities.Usuario;
import com.example.usuario.cargoex.util.AsyncResponse;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.example.usuario.cargoex.util.SqliteHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Login extends AppCompatActivity implements AsyncResponse {
    private FusedLocationProviderClient client;
    EditText usuario,pass,dv;
    String latitud,longitud;
    Button enter;
    ImageView imageCharge;
    AnimationDrawable cargando;
    SharedPreferences prefs ;
    Intent modal;
    SqliteCertificaciones conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        usuario = (EditText)findViewById(R.id.user);
        pass = (EditText)findViewById(R.id.pass);
        dv=(EditText)findViewById(R.id.dv);
        enter = (Button) findViewById(R.id.enter);
        imageCharge = (ImageView)findViewById(R.id.charge);
        cargando = (AnimationDrawable)imageCharge.getDrawable();
        modal= new Intent(this, Modal.class);
        prefs = getSharedPreferences("Preferencias",Context.MODE_PRIVATE);
        Log.e("db","va a crear db");
        conn = new SqliteCertificaciones(this, "bd_certificaciones", null, R.string.versionDB);
        String procesadoCodigo = prefs.getString("codigo", "");
        String procesadoNombre = prefs.getString("nombre", "");
        String procesadoRut = prefs.getString("rut", "");
        String captura = prefs.getString("captura","");
        String fechaActual = prefs.getString("fecha","");

        Log.e("CODIGO PROCESADO", "Codigo procesado es "+ procesadoCodigo + "rut procesado es"+procesadoRut);

        Log.e("captura",captura.length()+"");

        SimpleDateFormat dateFormat3 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date3 = new Date();
        String fecha3 = dateFormat3.format(date3);
        Log.e("FECHA ACTUAL",fecha3);
        if(getLocationMode()!=3){
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS EN ALTA PRESICION");
            startActivityForResult(modal,2);
        }else {
            if (procesadoNombre.length() > 0 && procesadoCodigo.length() > 0 && procesadoRut.length() > 0 && !procesadoCodigo.equals("6666") && fechaActual.equals(fecha3)&& !procesadoCodigo.equals("0")) {

                Intent intent = new Intent(this, Dashboard.class);
                startActivity(intent);
            }else{
                requestPermission();

            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        if(getLocationMode()!=3){
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS EN ALTA PRESICION");
            startActivityForResult(modal,2);
        }

    }

    public boolean ultimaPosicionAccion(){
        String codigo = prefs.getString("codigo", "");
        Log.e("ULTIMA ACCION","llego al boton ");
        SQLiteDatabase db = conn.getReadableDatabase();
        Log.e("ULTIMA ACCION", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM acciones WHERE id = " + codigo, null);
            Log.e("ULTIMA ACCION", "tamaño de las acciones es " + cursor.getCount());
            cursor.moveToLast();
           if(cursor.getString(2).equals("")|| cursor.getString(2).equals(null)||cursor.getString(2).equals("null")){
               cursor.close();
               db.close();
               return false;
           }else{
               Log.e("ULTIMA ACCION",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
                latitud=cursor.getString(2);
                longitud=cursor.getString(3);
               cursor.close();
               db.close();
               return true;
           }
        } catch (Exception e) {
            Log.e("ULTIMA ACCION","no encontro al consultar");
            db.close();
            return false;
        }
    }
    public boolean ultimaPosicionGestion(){
        String codigo = prefs.getString("codigo", "");
        SQLiteDatabase db = conn.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            cursor.moveToLast();
            if(cursor.getString(4).equals("")|| cursor.getString(4).equals(null)||cursor.getString(4).equals("null")){
                cursor.close();
                db.close();
                return false;
            }else{
                Log.e("ULTIMA ACCION",cursor.getString(0)+" ---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3)+"---"+cursor.getString(4)+"---"+cursor.getString(5));
                latitud=cursor.getString(4);
                longitud=cursor.getString(5);
                cursor.close();
                db.close();
                return true;
            }
        } catch (Exception e) {
            Log.e("ULTIMA ACCION","no encontro al consultar");
            db.close();
            return false;
        }
    }
    public int getLocationMode()
    {
        try {
            return Settings.Secure.getInt(getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void writeProperties(String data,Context context) {
      File file = new File  (Environment.getExternalStorageDirectory(),"movil.properties");
      try{
          FileOutputStream fos = new FileOutputStream(file);
          fos.write(data.getBytes());
          fos.close();
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 ) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        }
    }
    @Override
    public void processFinish(String output,String identificadorServicio){
        if(output.equals("false")){
            modal.putExtra("error", "Rut o clave incorrecta");
            startActivity(modal);
            cargando.stop();
            imageCharge.setVisibility(View.GONE);
            enter.setVisibility(View.VISIBLE);
        }else{
        try {

            JSONObject jsonObject = new JSONObject(output);
            Log.e("status","llego del servicio "+output);
            if (jsonObject.getString("success").compareTo("true") == 0) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    modal.putExtra("error", "DEBES TENER ACTIVO TU GPS");
                    startActivity(modal);
                    cargando.stop();
                    imageCharge.setVisibility(View.GONE);
                    enter.setVisibility(View.VISIBLE);
                    return;
                } else {
                String captura = prefs.getString("captura","");
                Log.e("captura",captura.length()+"");

                Log.e("status","llego aqui");
                //meto jsonobject
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray estados = jsonObject.getJSONArray("estados");
                JSONArray clientes = jsonObject.getJSONArray("clientes");
                Log.e("status","llego aqui"+data.toString());
                String nombre = data.getString("NOMBRE");
                String codigoChofer = data.getString("COD_CHOFER");
                Log.e("status","nombre es "+nombre);
                SimpleDateFormat dateFormat3 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date3 = new Date();
                String fecha3 = dateFormat3.format(date3);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("nombre", nombre);
                editor.putString("codigo",codigoChofer);
                editor.putString("rut",usuario.getText().toString()+"-"+dv.getText().toString().toUpperCase());
                editor.putString("captura", "true");
                editor.putString("fecha", fecha3);
                editor.putString("clientes",clientes.toString());
                editor.putString("estados",estados.toString());
                editor.commit();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                Date date = new Date();
                String fecha = dateFormat.format(date);


                cargando.stop();
                imageCharge.setVisibility(View.GONE);
                enter.setVisibility(View.VISIBLE);


                Intent intent = new Intent(this, Dashboard.class);
             //   intent.putExtra("clientes",clientes.toString());
              //  intent.putExtra("estados",listaOd);

                    startActivity(intent);
                Log.e("sisas","sisas");
            }
            }else {
                modal.putExtra("error", "Rut o clave incorrecta");
                startActivity(modal);
                cargando.stop();
                imageCharge.setVisibility(View.GONE);
                enter.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        }

    }

    private void requestPermission(){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_PHONE_STATE,
        };
        String infoOfProperties = "URL_BASE=http://movil.autentia.cl:8080/AutentiaWS/\nINSTITUTION=CARGOEX\nRUN_INSTITUTION=76284864-3\nENVIROMENT_CODE=PRODUCCION\nFINGERPRINT_READER_CONTROLLER_CLASS=cl.autentia.reader.usb.eikon.EikonManager\nNFC_CONTROLLER_CLASS=cl.autentia.nfc.NativeNFCManager\nHASH_CONFIGURATION=2694fa172e17fb55e4d5a1d8345c05d5109d2fb0";

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
          //  Context context = getApplicationContext();
          //  writeProperties(infoOfProperties,context);
        } /*else{
            Context context = getApplicationContext();
            writeProperties(infoOfProperties,context);
        }
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(Login.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        client.getLastLocation().addOnSuccessListener(Login.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //tomo latitud y longitud
                    latitud = location.getLatitude() + "";
                    longitud = location.getLongitude() + "";
                }
            }
        });  */

    }
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean pruebaInt(String s){
        try{
            int i = Integer.parseInt(s);
            return true;
        }catch (NumberFormatException n){
            return false;
        }
    }
    public boolean numeroVerificador(String s){
        try{
            int i = Integer.parseInt(s);
            return true;
        }catch (NumberFormatException n){
            if(s.equals("K")){
                return true;
            }
            return false;
        }
    }
    public boolean formatoRut(String [] rut){
        Log.e("state","tamaño es  "+ rut.length);
        Log.e("state","lo q hay en lenght -2 es  "+ rut[rut.length-1]);
        int auxMaximo= rut.length-2;
        if(rut[auxMaximo].compareTo("-")!=0){
            return false;
        }

        for (int j = 1; j < auxMaximo; j++) {
            Log.e("state","dato es "+ rut[j]+"llegue i "+j);
            if (!pruebaInt(rut[j])) {
                Log.e("login","va a retornar false por numero no entero ");
                return false;
            }else if(j >=10){
                Log.e("login","va a retornar false por else j >10");
                return false;
            }
        }
        if(rut.length-1<=8){
            return false;
        }
        Log.e("login","va a retornar true");
    return true;
    }
    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }
    public boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    public void accion(View v){
        enter.setVisibility(View.GONE);

        if(!cargando.isRunning()){
            Log.e("loading","entro a cargando");
            imageCharge.setVisibility(View.VISIBLE);
            cargando.start();
        }
        String nombreUsuario = usuario.getText().toString()+"-"+dv.getText().toString().toUpperCase();

        String password = (pass.getText().toString());

        String [] aux = nombreUsuario.split("");
        if(getLocationMode()!=3){
            modal.putExtra("error", "DEBES TENER ACTIVO TU GPS EN ALTA PRESICION");
            cargando.stop();
            enter.setVisibility(View.VISIBLE);
            imageCharge.setVisibility(View.GONE);
            startActivityForResult(modal,2);
        }else if(nombreUsuario.compareTo("")==0 || password.compareTo("")==0 || dv.getText().toString().compareTo("")==0){
            modal.putExtra("error", "Ingrese Rut y Clave");
            startActivity(modal);
            cargando.stop();
            enter.setVisibility(View.VISIBLE);
            imageCharge.setVisibility(View.GONE);
        }else if(!formatoRut(aux)){
            modal.putExtra("error", "Ingrese Formato valido de rut ej: 26208287-3");
            startActivity(modal);
            cargando.stop();
            enter.setVisibility(View.VISIBLE);
            imageCharge.setVisibility(View.GONE);
        }else if (!isNetDisponible() || !isOnlineNet()){
            modal.putExtra("error", "Usted no tiene internet");
            startActivity(modal);
            cargando.stop();
            enter.setVisibility(View.VISIBLE);
            imageCharge.setVisibility(View.GONE);
        }else if(!numeroVerificador(aux[aux.length-1])){

            modal.putExtra("error", "Numero verificador no valido");
            startActivity(modal);
            cargando.stop();
            enter.setVisibility(View.VISIBLE);
            imageCharge.setVisibility(View.GONE);
        }else{
            SimpleTask asyncTask =new SimpleTask();
            asyncTask.delegate = this;
            asyncTask.execute(nombreUsuario,password);
        }


    }

}
 class SimpleTask extends AsyncTask<String, Integer, String> {
     public AsyncResponse delegate = null;


    @Override
    protected String doInBackground(String... params) {
        String respuesta ="";
     Log.e("state","user name"+params[0]+" pass "+params[1]);
     String sql = "http://app.cargoex.cl/app/cargoex/app/login";
     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     StrictMode.setThreadPolicy(policy);
     URL url = null;
     HttpURLConnection conn;
        try {

            JSONObject jsonPrueba = new JSONObject();
            jsonPrueba.put("usuario", params[0]);
            jsonPrueba.put("password", params[1]);
            url = new URL(sql);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(jsonPrueba.toString());
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
           if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                 respuesta = sb.toString();
                br.close();
                return respuesta;
            } else {
                System.out.println(conn.getResponseMessage());
               System.out.println(conn.getInputStream());

               BufferedReader br = new BufferedReader(
                       new InputStreamReader(conn.getInputStream(), "utf-8"));
               String line = null;
               while ((line = br.readLine()) != null) {
                   sb.append(line + "\n");
               }

               respuesta = sb.toString();
               br.close();
               return "false";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respuesta;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        delegate.processFinish(result,"1");
    }

}
