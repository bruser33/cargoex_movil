package com.example.usuario.cargoex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cargo.usuario.cargoex.R;
import com.example.usuario.cargoex.util.AsyncResponse;
import com.example.usuario.cargoex.util.Modal;
import com.example.usuario.cargoex.util.Modal2;
import com.example.usuario.cargoex.util.ModalVersion;
import com.example.usuario.cargoex.util.SqliteCertificaciones;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.Result;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Agente extends AppCompatActivity implements AsyncResponse,ZXingScannerView.ResultHandler  {
    private FusedLocationProviderClient client;
    String latitud,longitud,vista="";
    Intent modal,modalVersion, modal2;
    SharedPreferences prefs ;
    String codigo,versionWeb,versionMovil;
    SqliteCertificaciones conn;
    TextView numero,nombre,fecha,ciudad,sizeTns,sizeTotal ;
    LinearLayout ods;
    String tnActual;
    private ZXingScannerView scanner;
    int contadorOds ;
    ArrayList <String> listaOd;
    int indiceABorrar;
    String regionActual ;
    JSONArray tns = new JSONArray();
    JSONArray tnsTotales = new JSONArray();
    ImageView imageCharge;
    AnimationDrawable cargando;
    boolean cargado = false;
    boolean flagCamara = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agente);
        Log.e("agente","llego a vista");
        numero=(TextView) findViewById(R.id.od);
        ciudad=(TextView) findViewById(R.id.ciudad);
        sizeTns=(TextView) findViewById(R.id.sizetns);
        sizeTotal=(TextView) findViewById(R.id.sizetotal);
        imageCharge = (ImageView)findViewById(R.id.charge);
        cargando = (AnimationDrawable)imageCharge.getDrawable();
        regionActual="";
        modal= new Intent(this, Modal.class);
        modal2 = new Intent(this, Modal2.class);
        modalVersion = new Intent(this, ModalVersion.class);
        tnActual="";
        listaOd= new ArrayList<String>();
        ods=findViewById(R.id.layoutods);
        vista= getIntent().getStringExtra("vista");
        conn = new SqliteCertificaciones(this,"bd_certificaciones",null,R.string.versionDB);
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
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(Agente.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        client.getLastLocation().addOnSuccessListener(Agente.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //tomo latitud y longitud
                    latitud = location.getLatitude() + "";
                    longitud = location.getLongitude() + "";
                }
            }
        });

       // eliminarRegistrosViejos();
        contadorOds=0;
        Log.e("anden","va a usare");
        //imageCharge.setVisibility(View.VISIBLE);
        //cargando.start();
      // this.uso("11597");
        //    Log.e("tamañoantes",listaOd.size()+"");
        // listaOd.add("asdf");
        // Log.e("tamañodespues",listaOd.size()+"");
    }

    public void eliminarRegistrosViejos() {
        Log.e("acciones", "llego a eliminar");

        try {
            SQLiteDatabase db = conn.getWritableDatabase();
            String[] parametros = {"null"};
            db.delete("acciones", "status != ?", parametros);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;

    }

    public void Escanear(View view){
        this.abrirScaner();
    }

    public void abrirScaner(){
        this.scanner = new ZXingScannerView(this);
        this.scanner.setResultHandler(this);
        setContentView(scanner);
        this.flagCamara = true;
        scanner.startCamera();
    }
    @Override
    public void onBackPressed() {
        Log.e("llego aqui", "llego");
        Log.e("llego aqui ","flag camara es "+this.flagCamara+" ");
        //  super.onBackPressed();
        if (this.flagCamara) {
            Log.e("llego aqui", "entro al if");
            setContentView(R.layout.masivos);
            scanner.stopCamera();
            this.flagCamara = false;
            this.cargarLista();
            ciudad=(TextView) findViewById(R.id.ciudad);
            sizeTns=(TextView) findViewById(R.id.sizetns);
            sizeTotal=(TextView) findViewById(R.id.sizetotal);
            ciudad.setText("Región: "+this.regionActual);
            sizeTns.setText("TNS: "+listaOd.size());
            sizeTotal.setText("TOTAL: "+ this.tnsTotales.length());
        } else{
                super.onBackPressed();
            }
      //this.cargarLista();
    }

    public void cargarLista(){
        contadorOds=0;
        /*
        Button verificar =new Button (this);
        verificar.setText("VERIFICAR");
        Drawable d = getResources().getDrawable(R.drawable.action);
        verificar.setBackground(d);
        verificar.setPadding(0,5,0,0);
        verificar.setWidth(40);  */
        Log.e("here", this.listaOd.toString());
        this.ods=findViewById(R.id.layoutods);
        // ods.addView(verificar,contadorOds);
        for(int i=listaOd.size()-1;i>=0;i--){
            Log.e("here","recorrio"+listaOd.get(i));
            final Button textView= new Button(this);
            textView.setText((i+1)+" - "+listaOd.get(i));
            textView.setTextAppearance(this, R.style.item);
            textView.setPadding(0,5,310,0);
            textView.setHeight(70);
            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
            textView.setTypeface(boldTypeface);
            textView.setTextSize(25);
            textView.setId(i);
            if(i%2==0){
                textView.setBackgroundResource(R.drawable.myborder);
            }else{
                textView.setBackgroundResource(R.drawable.mybordergray);
            }
            ods.addView(textView,contadorOds);
            contadorOds++;
        }
    }
    public void uso (String dato){
     Log.e("dato","dato es"+dato) ;
    // nombre.setText(dato);
     Log.e("tamaño en metodo",listaOd.size()+"");
     nombre= (TextView) findViewById(R.id.nombre);
     fecha = (TextView) findViewById(R.id.fecha);
     ods=findViewById(R.id.layoutods);
     String name = prefs.getString("nombre", "");
     nombre.setText(name);
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy ", Locale.getDefault());
     Date date = new Date();
     String fechaHoy = dateFormat.format(date);
     fecha.setText(fechaHoy);

     if(listaOd.contains(dato) ){
            modal.putExtra("error", "ESTA OD YA SE ENCUENTRA ALMACENADA");
            startActivity(modal);
        }else{

         this.tnActual=dato;
         pickingAgente tn = new pickingAgente();
         tn.delegate = (AsyncResponse) this;
         tn.execute(dato);
        }
    }
    public boolean isHere(String dato){
        for(int i =0;i<this.tnsTotales.length();i++){
            try {
                double tnAux = Double.parseDouble(this.tnsTotales.getJSONObject(i).getString("TN"));
                double tnAux2 = Double.parseDouble(dato);
                 Log.e("here", tnAux+"");
                 Log.e("here", tnAux2+"" );
                if(tnAux==tnAux2){
                    Log.e("here", "llego igualdad");
                    return true;

                }else{
                    Log.e("here","no son iguales"+tnAux+" "+tnAux2);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    @Override
    public void handleResult(Result result){
        String dato = result.getText();
        setContentView(R.layout.masivos);
        scanner.stopCamera();
        this.flagCamara = false;
        imageCharge = (ImageView)findViewById(R.id.charge);
        cargando = (AnimationDrawable)imageCharge.getDrawable();
        imageCharge.setVisibility(View.VISIBLE);
        cargando.start();
        if(listaOd.contains(dato) ){
            modal.putExtra("error", "ESTA OD YA SE ENCUENTRA ALMACENADA");
            startActivity(modal);
            this.ciudad=(TextView) findViewById(R.id.ciudad);
            this.sizeTns=(TextView) findViewById(R.id.sizetns);
            this.sizeTotal=(TextView) findViewById(R.id.sizetotal);
            this.ciudad.setText("Región: "+this.regionActual);
            this.sizeTns.setText("TNS: "+listaOd.size());
            this.sizeTotal.setText("TOTAL: "+ this.tnsTotales.length());
            this.cargarLista();
            cargando.stop();
            imageCharge.setVisibility(View.INVISIBLE);
        }else if(!cargado){ this.uso(dato);

        //aqui nueva camara
            //this.abrirScaner();
        }else if(isHere(dato)){
            Log.e("here","si esta aqui");
            listaOd.add(dato);
            this.sizeTns.setText("TNS: "+listaOd.size());
            this.cargarLista();
            for(int i =0;i<this.tnsTotales.length();i++){
                try {
                    double tnAux = Double.parseDouble(this.tnsTotales.getJSONObject(i).getString("TN"));
                    double tnAux2 = Double.parseDouble(dato);
                    //  Log.e("cerrar", tnAux);
                    //  Log.e("cerrar",tnActual );
                    if(tnAux==tnAux2){
                        this.tns.put(this.tnsTotales.getJSONObject(i));
                        Log.e("cerrar", "llego igualdad");

                    }else{
                        Log.e("cerrar","no son iguales"+tnAux+" "+tnAux2);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.ciudad=(TextView) findViewById(R.id.ciudad);
            this.sizeTns=(TextView) findViewById(R.id.sizetns);
            this.sizeTotal=(TextView) findViewById(R.id.sizetotal);
            this.ciudad.setText("Región: "+this.regionActual);
            this.sizeTns.setText("TNS: "+listaOd.size());
            this.sizeTotal.setText("TOTAL: "+ this.tnsTotales.length());
            cargando.stop();
            imageCharge.setVisibility(View.INVISIBLE);
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.ok);
            mediaPlayer.start();
            //aqui otra vez
        }else{
            Log.e("here","no esta aqui");
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.nook);
            mediaPlayer.start();
            this.ciudad=(TextView) findViewById(R.id.ciudad);
            this.sizeTns=(TextView) findViewById(R.id.sizetns);
            this.sizeTotal=(TextView) findViewById(R.id.sizetotal);
            this.ciudad.setText("Región: "+this.regionActual);
            this.sizeTns.setText("TNS: "+listaOd.size());
            this.sizeTotal.setText("TOTAL: "+ this.tnsTotales.length());
            this.cargarLista();
            cargando.stop();
            imageCharge.setVisibility(View.INVISIBLE);
            modal.putExtra("error", "EL TN PICKEADO NO CORRESPONDE ALA REGION");
            startActivity(modal);
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
        //  Log.e("ULTIMA ACCION","llego al boton ");
        SQLiteDatabase db = conn.getReadableDatabase();
        //   Log.e("ULTIMA ACCION", "Va a consultar acciones");
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM certificaciones WHERE codChofer = " + codigo, null);
            //     Log.e("ULTIMA ACCION", "tamaño de las acciones es " + cursor.getCount());
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
    public void cerrarManifiesto(View v) {
        Log.e("cerrar",this.tns.toString());
        if(this.tns.length() == 0){
            modal.putExtra("error", "NO HAY PICKEADO NINGUN TN");
            startActivity(modal);
        }else{
            modal2.putExtra("error", "¿desea cerrar el manifiesto de bodega con "+this.listaOd.size()+" TN para "+this.regionActual+" ?");
            startActivityForResult(modal2, 1);
        }

    }

    public void procesoCerrado(){
        imageCharge.setVisibility(View.VISIBLE);
        cargando.start();
        Log.e("MANI",this.tns.toString());
        EnvioAgente picking = new EnvioAgente();
        picking.delegate = (AsyncResponse) this;
        picking.execute(this.codigo, this.tns,this.nombre.getText().toString(),"pedro.luis.rico43@gmail.com","COMPLETO","SALIDA","SALIDA","SALIDA","",this.fecha.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 ) {
            if (resultCode == RESULT_OK) {
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
                    cargando.stop();
                    imageCharge.setVisibility(View.INVISIBLE);

                    this.finish();
                }
            }
        }else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                String returnString = data.getStringExtra("status");
                if (returnString.equals("true")) {
                    this.procesoCerrado();
                }

            }
        }
        else if (requestCode == 2 ) {
            if(!versionWeb.equals(versionMovil)){
                modalVersion.putExtra("error", "DEBES ACTUALIZAR ALA ULTIMA VERSION");
                startActivityForResult(modalVersion,2);
            }
        }else if (requestCode == 3 ) {
            listaOd.remove(indiceABorrar);
            Log.e("elindicees",indiceABorrar+"");
            ods.removeAllViews();
            this.cargarLista();
        }
    }

    @Override
    public void processFinish(String output,String identificadorServicio) {
        if (output.equals("false")) {
            Log.e("sate", "no hay internet");
        } else {
            if(identificadorServicio.equals("1")) {
                try{
                JSONObject jsonObject = new JSONObject(output);
                    if (jsonObject.getString("success").compareTo("true") == 0 ) {
                        MediaPlayer mediaPlayer;
                        mediaPlayer = MediaPlayer.create(this, R.raw.ok);
                        mediaPlayer.start();
                        JSONArray datos = jsonObject.getJSONArray("datos");
                        if(datos.length()>0){


                        JSONObject objeto = datos.getJSONObject(0);
                        this.ciudad=(TextView) findViewById(R.id.ciudad);
                        this.sizeTns=(TextView) findViewById(R.id.sizetns);
                        this.sizeTotal=(TextView) findViewById(R.id.sizetotal);
                        this.ciudad.setText("Región: "+objeto.getString("CIUDAD_DESTINO"));
                        Log.e("here","destino"+objeto.getString("CIUDAD_DESTINO"));
                        this.regionActual=objeto.getString("CIUDAD_DESTINO");
                        listaOd.add(tnActual);
                        this.tnsTotales = datos;
                        this.sizeTns.setText("TNS: "+listaOd.size());
                        this.sizeTotal.setText("TOTAL: "+ datos.length());
                        cargando.stop();
                        imageCharge.setVisibility(View.INVISIBLE);
                        for(int i =0;i<this.tnsTotales.length();i++){
                            try {
                                double tnAux = Double.parseDouble(this.tnsTotales.getJSONObject(i).getString("TN"));
                                Log.e("msj","actual"+tnActual);
                                double tnAux2 = Double.parseDouble(tnActual);
                              //  Log.e("cerrar", tnAux);
                              //  Log.e("cerrar",tnActual );
                                if(tnAux==tnAux2){
                                    this.tns.put(this.tnsTotales.getJSONObject(i));
                                    Log.e("cerrar", "llego igualdad");

                                }else{
                                    Log.e("cerrar","no son iguales"+tnAux+" "+tnAux2);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        this.cargado=true;
                        this.cargarLista();
                        cargando.stop();
                        imageCharge.setVisibility(View.INVISIBLE);
                        }else{
                            this.cargado=false;
                            mediaPlayer = MediaPlayer.create(this, R.raw.nook);
                            mediaPlayer.start();
                            modal.putExtra("error", "TNS VACIOS");
                            startActivity(modal);
                            cargando.stop();
                            imageCharge.setVisibility(View.INVISIBLE);
                        }
                    }else{
                        MediaPlayer mediaPlayer;
                        mediaPlayer = MediaPlayer.create(this, R.raw.nook);
                        mediaPlayer.start();
                        modal.putExtra("error", "ESTE TN NO ESTA REGISTRADO EN MANIFIESTO DE BODEGA");
                        startActivity(modal);
                        cargando.stop();
                        imageCharge.setVisibility(View.INVISIBLE);
                    }

                    } catch (Exception e) {
                e.printStackTrace();
            }
            }else if(identificadorServicio.equals("2")) {
                modal.putExtra("error", "SE HA CERRADO MANIFIESTO DE FORMA EXITOSA PARA "+ this.regionActual);
                startActivity(modal) ;
                cargando.stop();
                imageCharge.setVisibility(View.INVISIBLE);
              this.finish();
            }

        }
    }


}

class pickingAgente extends AsyncTask<String, Integer, String> {
    public AsyncResponse delegate = null;


    @Override
    protected String doInBackground(String... params) {
        if (!isOnlineNet()) {
            return "false";
        } else {
            Log.e("Version","llego a usar el servicio de verificacion");
            String respuesta = "";
            String sql = "http://app.cargoex.cl/app/cargoex/app/selManifiestoSalidaanden";
            Log.e("Version","PASO POR AQUI1 ");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;
            HttpURLConnection conn;
            Log.e("Version","PASO POR AQUI2 ");
            try {
                JSONObject json = new JSONObject();
                json.put("TN", params[0]);


                url = new URL(sql);
                conn = (HttpURLConnection) url.openConnection();
                Log.e("Version","PASO POR AQUI3 ");

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("version", "antes de llamar al servicio de consulta de version");
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();

                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.e("sincro", "entro al ok");
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
                    Log.e("version", "no entro al ok");
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
                Log.e("sincro", "entro al catch");
                e.printStackTrace();
            }
            return respuesta;
        }
    }

    public boolean isOnlineNet() {

        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onPostExecute(String result) {
        Log.e(" resultado anden es ", result);
        super.onPostExecute(result);
        delegate.processFinish(result,"1");
    }
}

class EnvioAgente extends AsyncTask<Object, Integer, String> {
    public AsyncResponse delegate = null;


    @Override
    protected String doInBackground(Object... params) {

        if (!isOnlineNet()) {
            return "false";
        } else {
            Log.e("verifica","llego a usar el servicio de verificacion");
            String respuesta = "";
            String sql = "http://app.cargoex.cl:5000/cerrarManifiestoAgente";
            Log.e("Verifica","PASO POR AQUI1 ");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = null;
            HttpURLConnection conn;
            Log.e("verifica","PASO POR AQUI2 ");
            try {
                JSONObject json = new JSONObject();
                json.put("idcliente", params[0]);
                json.put("tns", params[1]);
                json.put("nombrecliente", params[2]);
                json.put("correo", params[3]);
                json.put("estado", params[4]);
                json.put("portal", params[5]);
                json.put("chofer", params[6]);
                json.put("codChofer", params[7]);
                json.put("motivo", params[8]);
                json.put("dia", params[9]);
                url = new URL(sql);
                conn = (HttpURLConnection) url.openConnection();
                Log.e("Version","PASO POR AQUI3 ");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-KEY", "55IcsddHxiy2E3q653RpYtb");
                Log.e("version", "antes de llamar al servicio de consulta de version");
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();
                StringBuilder sb = new StringBuilder();
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.e("sincro", "entro al ok");
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
                    Log.e("version", "no entro al ok");
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
                Log.e("sincro", "entro al catch");
                e.printStackTrace();
            }
            return respuesta;
        }
    }
    public boolean isOnlineNet() {

        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onPostExecute(String result) {
        Log.e(" resultado anden es ", result);
        super.onPostExecute(result);
        delegate.processFinish(result,"2");
    }
}